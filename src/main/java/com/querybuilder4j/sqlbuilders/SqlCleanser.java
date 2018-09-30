package com.querybuilder4j.sqlbuilders;


import com.querybuilder4j.config.Parenthesis;
import com.querybuilder4j.sqlbuilders.statements.Criteria;

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

        boolean conjunctionIsClean = true;
        if (criteria.getConjunction() != null) {
           conjunctionIsClean = sqlIsClean(criteria.getConjunction().toString());
        }

        boolean frontParenIsClean = true;
        if (criteria.getFrontParenthesis() != null) {
           frontParenIsClean = sqlIsClean(criteria.getFrontParenthesis().toString());
        }

        boolean columnIsClean = true;
        if (criteria.getColumn() != null) {
            String[] tableAndColumn = criteria.column.split("\\.");
            if (tableAndColumn.length != 2) {
                throw new IllegalArgumentException("A criteria's column field needs to be in the format of [table.column].  " +
                        "Here is the criteria object that failed:  " + criteria);
            }
            final int TABLE_INDEX = 0;
            final int COLUMN_INDEX = 1;
            columnIsClean = sqlIsClean(tableAndColumn[TABLE_INDEX]) &&
                            sqlIsClean(tableAndColumn[COLUMN_INDEX]);
        }

        boolean filterIsClean = true;
        if (criteria.getFilter() != null) {
            filterIsClean = sqlIsClean(criteria.getFilter());
        }

        boolean endParenIsClean = true;
        for (Parenthesis paren : criteria.getEndParenthesis()) {
            endParenIsClean = sqlIsClean(paren.toString());
            if (! endParenIsClean) {
                break;
            }
        }

        return conjunctionIsClean && frontParenIsClean && columnIsClean  && filterIsClean && endParenIsClean;
    }
}
