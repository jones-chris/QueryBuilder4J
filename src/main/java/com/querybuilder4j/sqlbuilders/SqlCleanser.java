package com.querybuilder4j.sqlbuilders;


import com.querybuilder4j.statements.Criteria;

import java.sql.Types;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SqlCleanser {
    // These characters are the only characters that should be escaped because they can be expected to be in query criteria.
    //  Ex:  SELECT * FROM restaurants WHERE name = 'Tiffany''s';
    private static final Character[] charsNeedingEscaping = new Character[] {'\''};

    // SQL arithmetic, bitwise, comparison, and compund operators per https://www.w3schools.com/sql/sql_operators.asp
    // If any of these strings are contained in SelectStatement, then return false.
    private static final String[] reservedOperators = new String[] {"+", "-", "*", "/", "&", "|", "^", "=", ">", "<", "!=",
            "<>", ">=", "<=", "+=", "-=", "*=", "/=", "%=", "&=", "^-=", "|*="
    };

    // If any of these characters are contained in SelectStatement, then return false.
    private static final String[] forbiddenMarks = new String[] {";", "`", "\""};

    // Ansi keywords per https://docs.snowflake.net/manuals/sql-reference/reserved-keywords.html.  I did not include 'TRUE' and
    //   'FALSE' from the link's list because those are valid SelectStatement filters.
    // If any of these strings are contained in SelectStatement, then return false.
    // Did not include "AND", because this is a common word in filters.
    private static final String[] ansiKeywords = new String[] {"ALL", "ALTER", "ANY", "AS", "ASC", "BETWEEN",
            "BY", "CASE", "CAST", "CHECK", "CLUSTER", "COLUMN", "CONNECT", "CREATE", "CROSS", "CURRENT_DATE",
            "CURRENT_ROLE", "CURRENT_USER", "CURRENT_TIME", "CURRENT_TIMESTAMP", "DELETE", "DESC", "DISTINCT",
            "DROP", "ELSE", "EXCLUSIVE", "EXISTS", "FOR", "FROM", "FULL", "GRANT", "GROUP",
            "HAVING", "IDENTIFIED", "ILIKE", "IMMEDIATE", "IN", "INCREMENT", "INNER", "INSERT", "INTERSECT",
            "INTO", "IS", "JOIN", "LATERAL", "LEFT", "LIKE", "LOCK", "LONG", "MAXEXTENTS", "MINUS", "MODIFY",
            "NATURAL", "NOT", "NULL", "OF", "ON", "OPTION", "OR", "REGEXP", "RENAME", "REVOKE", "RIGHT",
            "RLIKE", "ROW", "ROWS", "SAMPLE", "SELECT", "SET", "SOME", "START", "TABLE", "TABLESAMPLE",
            "THEN", "TO", "TRIGGER", "UNION", "UNIQUE", "UPDATE", "USING", "VALUES", "VIEW", "WHEN",
            "WHENEVER", "WHERE", "WITH"
    };

    public static String escape(String sql) {

        for (Character c : charsNeedingEscaping) {
            sql = sql.replaceAll(c.toString(), c.toString() + c.toString());
        }

        return sql;

    }

    public static boolean sqlIsClean(String str) {

        String upperCaseStr = str.toUpperCase();
        for (String opr : reservedOperators) {
            if (upperCaseStr.contains(opr)) {
                return false;
            }
        }

        for (String mark : forbiddenMarks) {
            if (upperCaseStr.contains(mark)) {
                return false;
            }
        }

        // "\\b%s\\b" worked
        for (String keyword : ansiKeywords) {
            Pattern pattern = Pattern.compile(String.format("(^|\\W)\\Q%s\\E\\W", keyword), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(upperCaseStr);
            if (matcher.find()) {
                return false;
            }
        }

        return true;

    }

    public static boolean sqlIsClean(Criteria criteria) throws IllegalArgumentException {
        //todo:  don't pass params and subqueries into sqlIsClean().
        //todo:  also make Criteria's filter field a String again instead of Object since we're not using SubQuery class anymore.
        if (criteria.getFilter() != null) {
            //tring[] filterItems = criteria.getFilter().split(",");

//            for (String filterItem : filterItems) {
//                Pattern patternSubQuery = Pattern.compile("^subquery[0-9]+");
//                Pattern patternParam = Pattern.compile("^\\$param:");
//                Matcher matcherSubQuery = patternSubQuery.matcher(filterItem);
//                Matcher matcherParam = patternParam.matcher(filterItem);
//
//                // If filter does NOT contain a param or subquery, then check that string is clean using sqlIsClean().
//                if (! matcherSubQuery.find() && ! matcherParam.find()) {
                if (! isSubQueryOrParam(criteria.filter)) {
                    boolean filterIsClean = sqlIsClean(criteria.getFilter());
                    if (! filterIsClean) return false;
                }
//            }
        }

        return true;
    }

    /**
     * Tests whether a String (s) can be parsed into a numerical or boolean type successfully based on the sqlType parameter.
     * This method is used to prevent SQL Injection on columns that would not be wrapped in single quotes in the WHERE clause
     * of the SQL statement.  For example:
     *
     *     SELECT * FROM students WHERE age > ?
     *
     * If the column, Age, has a database type of Integer, and I passed " '' OR 1=1 --", then the input would not be wrapped in
     * single quotes and the SQL Injection attack would be successful.  However, if I passed the input into this method, it would
     * not parse into an Java integer and the method would return false.
     *
     * Therefore, this assures is that the input String (s) is indeed numeric or boolean and is safe to not be wrapped in single
     * quotes.
     *
     * @param s
     * @param sqlType
     * @return boolean
     */
    public static boolean canParseNonQuotedFilter(String s, int sqlType) throws Exception {
        if (s == null) {
            return true;
        }

        if (! isSubQueryOrParam(s)) {
            try {
                if (sqlType == Types.BIGINT || sqlType == Types.DOUBLE || sqlType == Types.NUMERIC || sqlType == Types.ROWID || sqlType == Types.REAL) {
                    Double.parseDouble(s);
                    return true;
                } else if (sqlType == Types.BIT || sqlType == Types.INTEGER || sqlType == Types.SMALLINT || sqlType == Types.TINYINT) {
                    Integer.parseInt(s);
                    return true;
                } else if (sqlType == Types.BOOLEAN) {
                    Boolean.parseBoolean(s);
                    return true;
                } else if (sqlType == Types.FLOAT) {
                    Float.parseFloat(s);
                    return true;
                } else {
                    throw new Exception(String.format("Did not recognize SQL Type:  %s", sqlType));
                }
            } catch (NumberFormatException ex) {
                return false;
            }
        }

        return true;
    }

    private static boolean isSubQueryOrParam(String s) {
        if (s.length() == 0) {
            return true;
        }

        char firstChar = s.charAt(0);
        return (firstChar == '$' || firstChar == '@');
    }
}
