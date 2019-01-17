package com.querybuilder4j.sqlbuilders;


import com.querybuilder4j.config.Conjunction;
import com.querybuilder4j.config.Operator;
import com.querybuilder4j.config.Parenthesis;
import com.querybuilder4j.exceptions.BadSqlException;
import com.querybuilder4j.exceptions.ColumnNameNotFoundException;
import com.querybuilder4j.exceptions.DataTypeNotFoundException;
import com.querybuilder4j.exceptions.EmptyCollectionException;
import com.querybuilder4j.sqlbuilders.statements.*;
import com.querybuilder4j.sqlbuilders.dao.*;

import java.sql.Types;
import java.util.*;

import static com.querybuilder4j.sqlbuilders.SqlCleanser.escape;
import static com.querybuilder4j.sqlbuilders.SqlCleanser.sqlIsClean;
import static java.util.Optional.ofNullable;

public abstract class SqlBuilder {
    protected final static Map<Integer, Boolean> typeMappings = new HashMap<>();
    protected char beginningDelimiter;
    protected char endingDelimter;
    protected Map<String, Map<String, Integer>> tableSchemas = new HashMap<>();
    protected final Properties properties;
    protected SelectStatement stmt;
    protected Map<String, String> subQueries = new HashMap<>();
    protected Map<String, Object> parsedSubQueries = new HashMap<>();
    //protected Map<String, String> sqlParameterMap = new HashMap<>();
    protected final int TABLE_INDEX = 0;
    protected final int COLUMN_INDEX = 1;
    //protected int namedParameterCount = 0;
    protected QueryTemplateDao queryTemplateDao;

    static {
        typeMappings.put(Types.ARRAY, true);                     //ARRAY
        typeMappings.put(Types.BIGINT, false);                   //BIGINT
        typeMappings.put(Types.BINARY, true);                    //BINARY
        typeMappings.put(Types.BIT, false);                      //BIT
        typeMappings.put(Types.BLOB, true);                      //BLOB
        typeMappings.put(Types.BOOLEAN, false);                  //BOOLEAN
        typeMappings.put(Types.CHAR, true);                      //CHAR
        typeMappings.put(Types.CLOB, true);                      //CLOB
        //typeMappings.put(Types.DATALINK, false);                 //DATALINK
        typeMappings.put(Types.DATE, true);                      //DATE
        typeMappings.put(Types.DECIMAL, true);                   //DECIMAL
        typeMappings.put(Types.DISTINCT, true);                  //DISTINCT
        typeMappings.put(Types.DOUBLE, false);                   //DOUBLE
        typeMappings.put(Types.FLOAT, false);                    //FLOAT
        typeMappings.put(Types.INTEGER, false);                  //INTEGER
        typeMappings.put(Types.JAVA_OBJECT, true);               //JAVA_OBJECT
        typeMappings.put(Types.LONGNVARCHAR, true);              //LONGNVARCHAR
        typeMappings.put(Types.LONGVARBINARY, true);             //LONGVARBINARY
        typeMappings.put(Types.LONGVARCHAR, true);               //LONGVARCHAR
        typeMappings.put(Types.NCHAR, true);                     //NCHAR
        typeMappings.put(Types.NCLOB, true);                     //NCLOB
        typeMappings.put(Types.NULL, true);                      //NULL
        typeMappings.put(Types.NUMERIC, false);                  //NUMERIC
        typeMappings.put(Types.NVARCHAR, true);                  //NVARCHAR
        typeMappings.put(Types.OTHER, true);                     //OTHER
        typeMappings.put(Types.REAL, false);                     //REAL
        typeMappings.put(Types.REF, true);                       //REF
        typeMappings.put(Types.REF_CURSOR, true);                //REF_CURSOR
        typeMappings.put(Types.ROWID, false);                    //ROWID
        typeMappings.put(Types.SMALLINT, false);                 //SMALLINT
        typeMappings.put(Types.SQLXML, true);                    //SQLXML
        typeMappings.put(Types.STRUCT, true);                    //STRUCT
        typeMappings.put(Types.TIME, true);                      //TIME
        typeMappings.put(Types.TIME_WITH_TIMEZONE, true);        //TIME_WITH_TIMEZONE
        typeMappings.put(Types.TIMESTAMP, true);                 //TIMESTAMP
        typeMappings.put(Types.TIMESTAMP_WITH_TIMEZONE, true);   //TIMESTAMP_WITH_TIMEZONE
        typeMappings.put(Types.TINYINT, false);                  //TINYINT
        typeMappings.put(Types.VARBINARY, true);                 //VARBINARY
        typeMappings.put(Types.VARCHAR, true);                   //VARCHAR
    }

    public SqlBuilder(SelectStatement stmt, Map<String, String> subQueries,
                      Properties properties, QueryTemplateDao queryTemplateDao) throws Exception {
        this.stmt = stmt;
        this.properties = properties;

        if (subQueries != null) {
            this.subQueries = subQueries;
        }

        if (queryTemplateDao != null) {
            this.queryTemplateDao = queryTemplateDao;
        }

        if (! criteriaAreValid()) {
            throw new Exception("A criteria is not valid");
        }

        if (statementIsValid()) {
            parseSubqueries();
        }
    }

    public abstract String buildSql() throws Exception;

    /**
     * Creates the SELECT clause of a SELECT SQL statement.
     *
     * @param distinct
     * @param columns
     * @return StringBuilder
     */
    protected StringBuilder createSelectClause(boolean distinct, List<String> columns) {
        String startSql = (distinct) ? "SELECT DISTINCT " : "SELECT ";
        StringBuilder sql = new StringBuilder(startSql);
        for (String column : columns) {
            String[] tableAndColumn = column.split("\\.");
            sql.append(String.format("%s%s%s.%s%s%s, ",
                    beginningDelimiter, escape(tableAndColumn[TABLE_INDEX]), endingDelimter,
                    beginningDelimiter, escape(tableAndColumn[COLUMN_INDEX]), beginningDelimiter));
        }
        return sql.delete(sql.length() - 2, sql.length()).append(" ");
    }

    /**
     * Creates the FROM clause of a SELECT SQL statement.
     *
     * @param table
     * @return
     * @throws IllegalArgumentException
     * @throws BadSqlException
     */
    protected StringBuilder createFromClause(String table) {
        String s = String.format(" FROM %s%s%s ", beginningDelimiter, escape(table), endingDelimter);
        return new StringBuilder(s);
    }

    /**
     * Creates the JOIN clause of a SELECT SQL statement.
     *
     * @param joins
     * @return
     * @throws IllegalArgumentException
     */
    protected StringBuilder createJoinClause(List<Join> joins) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<joins.size(); i++) {
            Join join = joins.get(i);

            if (join.getParentJoinColumns().size() != join.getTargetJoinColumns().size()) {
                final String joinColumnsSizeDiffMessage = "The parent and target join columns have differing number of elements.";
                throw new RuntimeException(joinColumnsSizeDiffMessage);
            }

            sb.append(join.getJoinType().toString());
            sb.append(String.format(" %s%s%s ", beginningDelimiter, join.getTargetTable(), endingDelimter));

            for (int j=0; j<join.getParentJoinColumns().size(); j++) {
                String conjunction = (j == 0) ? "ON" : "AND";

                //Format string in the form of " [ON/AND] `table1`.`column1` = `table2`.`column2` ", assuming the database
                // type is MySql.
                sb.append(String.format(" %s %S%s%s.%s%s%s = %s%s%s.%s%s%s ",
                        conjunction,
                        beginningDelimiter, join.getParentTable(), endingDelimter,
                        beginningDelimiter, join.getParentJoinColumns().get(j), endingDelimter,
                        beginningDelimiter, join.getTargetTable(), endingDelimter,
                        beginningDelimiter, join.getTargetJoinColumns().get(j), endingDelimter));
            }

        }

        return sb;
    }

    /**
     * Creates the WHERE clause of a SQL CRUD statement.
     *
     * @param criteria
     * @return
     * @throws Exception
     */
    protected StringBuilder createWhereClause(List<Criteria> criteria) throws Exception {
        StringBuilder sql = new StringBuilder();

        if (criteria.size() != 0) {
            sql.append(" WHERE ");

            for (Criteria crit : criteria) {
                // clone criteria
                Criteria criteriaClone = (Criteria) crit.clone();

                if (criteriaClone.getId() == 0) criteriaClone.conjunction = null;

                if (criteriaClone.operator.equals(Operator.isNull) || criteriaClone.operator.equals(Operator.isNotNull)) {
                    criteriaClone.filter = null;
                    sql.append(String.format(" %s ", stringifyCriteria(criteriaClone)));
                    continue;
                }

                // Now that we know that the criteria's operator is not 'isNull' or 'isNotNull', we can assume that the
                // criteria's filter is needed.  Therefore, we should check if the filter is null or an empty string.
                // If so, throw an exception.
//                if (criteriaClone.filter == null || criteriaClone.filter.equals("")) {
//                    throw new BadSqlException("The criteria has a null or empty filter, " +
//                            "but the operator is not \"IsNull\" or \"IsNotNull\"");
//                }

                // If the criteria's filter is a SubQuery, then generate the SelectStatement's
                // parameterized SQL string.
                if (argIsSubQuery(criteriaClone.filter.toString())) {
                    // the criteria's filter should be the subquery id that can be retrieved from parsedSubQueries.
                    //SubQuery parameterizedSubQuerySql = (SubQuery) parsedSubQueries.get(criteriaClone.filter.toString());
                    String subquery = parsedSubQueries.get(criteriaClone.filter.toString()).toString();
                    criteriaClone.filter = "(" + subquery + ")";
                    sql.append(stringifyCriteria(criteriaClone)).append(" ");
                    continue;
                }

                String[] tableAndColumn = criteriaClone.column.split("\\.");
                int columnDataType = getColumnDataType(tableAndColumn[TABLE_INDEX], tableAndColumn[COLUMN_INDEX]);
                boolean shouldHaveQuotes = isColumnQuoted(columnDataType);
                if (criteriaClone.operator.equals(Operator.in) || criteriaClone.operator.equals(Operator.notIn)) {
                    if (shouldHaveQuotes) {
                        wrapFilterInQuotes(criteriaClone);
                    } else {
                        criteriaClone.filter = "(" + escape(criteriaClone.filter.toString()) + ")";
                    }
//                    sql.append(stringifyCriteria(criteriaClone)).append(" ");
                } else {
                    if (shouldHaveQuotes) {
                        criteriaClone.filter = "'" + escape(criteriaClone.filter.toString()) + "'";
                    } else {
                        criteriaClone.filter = escape(criteriaClone.filter.toString());
                    }
//                    sql.append(stringifyCriteria(criteriaClone)).append(" ");
                }
                sql.append(stringifyCriteria(criteriaClone)).append(" ");
            }
        }
        return sql;
    }

    /**
     * Creates the GROUP BY clause of a SELECT SQL statement.
     *
     * @param columns
     * @return
     * @throws IllegalArgumentException
     * @throws EmptyCollectionException
     * @throws BadSqlException
     */
    protected StringBuilder createGroupByClause(List<String> columns) {
        StringBuilder sql = new StringBuilder(" GROUP BY ");

        for (String column : columns) {
            String[] tableAndColumn = column.split("\\.");
            sql.append(String.format("%s%s%s.%s%s%s, ",
                                      beginningDelimiter, escape(tableAndColumn[TABLE_INDEX]), endingDelimter,
                                      beginningDelimiter, escape(tableAndColumn[COLUMN_INDEX]), endingDelimter));
        }

        return sql.delete(sql.length() - 2, sql.length()).append(" ");
    }

    /**
     * Creates the ORDER BY clause of a SELECT SQL statement.
     *
     * @param columns
     * @param ascending
     * @return
     * @throws IllegalArgumentException
     * @throws EmptyCollectionException
     * @throws BadSqlException
     */
    protected StringBuilder createOrderByClause(List<String> columns, boolean ascending) {
        StringBuilder sql = new StringBuilder(" ORDER BY ");

        for (String column : columns) {
            String[] tableAndColumn = column.split("\\.");
            sql.append(String.format("%s%s%s.%s%s%s, ",
                    beginningDelimiter, escape(tableAndColumn[TABLE_INDEX]), endingDelimter,
                    beginningDelimiter, escape(tableAndColumn[COLUMN_INDEX]), endingDelimter));
        }

        sql.delete(sql.length() - 2, sql.length()).append(" ");
        //return (ascending) ? sql.append(" ASC ").Replace("  ", " ") : sql.Append(" DESC ").Replace("  ", " ");
        return (ascending) ? sql.append(" ASC ") : sql.append(" DESC ");
    }

    /**
     * Creates the LIMIT clause of a SELECT SQL statement.
     *
     * @param limit
     * @return
     * @throws IllegalArgumentException
     * @throws BadSqlException
     */
    protected StringBuilder createLimitClause(Long limit) throws IllegalArgumentException, BadSqlException {
        return new StringBuilder(String.format(" LIMIT %s ", limit));
    }

    /**
     * Creates the OFFSET clause of a SELECT SQL statement.
     *
     * @param offset
     * @return
     * @throws IllegalArgumentException
     * @throws BadSqlException
     */
    protected StringBuilder createOffsetClause(Long offset) throws IllegalArgumentException, BadSqlException {
        return new StringBuilder(String.format(" OFFSET %s ", offset));
    }

    /**
     * Creates a WHERE clause condition that all columns in the columns parameter cannot be null.  This condition
     * should is used to not return records where all selected columns have a null value.
     *
     * @param columns
     * @return StringBuilder
     */
    protected StringBuilder createSuppressNullsClause(List<String> columns) {
        StringBuilder sql = new StringBuilder();

        for (int i = 0; i < columns.size(); i++) {
            String[] tableAndColumn = columns.get(i).split("\\.");
            if (i == 0) {
                sql.append(String.format(" (%s%s%s.%s%s%s IS NOT NULL ",
                                            beginningDelimiter, tableAndColumn[TABLE_INDEX], endingDelimter,
                                            beginningDelimiter, tableAndColumn[COLUMN_INDEX], endingDelimter));
            } else {
                sql.append(String.format(" OR %s%s%s.%s%s%s IS NOT NULL ",
                                           beginningDelimiter, tableAndColumn[TABLE_INDEX], endingDelimter,
                                           beginningDelimiter, tableAndColumn[COLUMN_INDEX], endingDelimter));
            }
        }

        return sql.append(") ");
    }

    /**
     * Gets the SQL JDBC Type for the table and column parameters.
     *
     * @param table
     * @param columnName
     * @return
     * @throws ColumnNameNotFoundException
     */
    private int getColumnDataType(String table, String columnName) throws ColumnNameNotFoundException {
        Integer dataType = tableSchemas.get(table).get(columnName);

        if (dataType == null) {
            throw new ColumnNameNotFoundException(String.format("Could not find column, %s", columnName));
        } else {
            return dataType;
        }
    }

    /**
     * Gets a boolean from the typeMappings class field associated with the SQL JDBC Types parameter, which is an int.
     * The typeMappings field will return true if the SQL JDBC Types parameter should be quoted in a WHERE SQL clause and
     * false if it should not be quoted.
     *
     * For example, the VARCHAR Type will return true, because it should be wrapped in single quotes in a WHERE SQL condition.
     * On the other hand, the INTEGER Type will return false, because it should NOT be wrapped in single quotes in a WHERE SQL condition.
     *
     * @param columnDataType
     * @return
     * @throws DataTypeNotFoundException
     */
    private boolean isColumnQuoted(int columnDataType) throws DataTypeNotFoundException {
        Boolean isQuoted = typeMappings.get(columnDataType);
        if (isQuoted == null) {
            throw new DataTypeNotFoundException(String.format("Data type, %s, is not recognized", columnDataType));
        } else {
            return isQuoted;
        }
    }

    /**
     * Wraps the criteria's filter in quotes.  If the criteria's filter is a list of items, the function assumes the list is
     * comma separated and therefore splits on a comma (,).
     *
     * @param criteria
     */
    private void wrapFilterInQuotes(Criteria criteria) {
        if (criteria.filter instanceof String == false) {
            throw new RuntimeException("The criteria filter must be of type String when calling this method.");
        }
        String[] originalFilters = criteria.filter.toString().split(",");
        String[] newFilters = new String[originalFilters.length];

        for (int i=0; i<originalFilters.length; i++) {
            newFilters[i] = String.format("'%s'", escape(originalFilters[i]));
        }

        criteria.filter = "(" + String.join(",", newFilters) + ")";
    }

    /**
     * Returns the SQL string of the criteria that is passed into the function.
     *
     * For example, it may return the string:  "AND "table1"."column1" = 'test'".
     *
     * @param criteria
     * @return
     */
    private String stringifyCriteria(Criteria criteria) {
        String endParenthesisString = "";
        if (criteria.endParenthesis != null) {
            for (Parenthesis paren : criteria.endParenthesis) {
                endParenthesisString += paren;
            }
        }

        String[] tableAndColumn = criteria.column.split("\\.");
        return String.format(" %s %s%s%s%s.%s%s%s %s %s%s ",
                ofNullable(criteria.conjunction).orElse(Conjunction.Empty),
                ofNullable(criteria.frontParenthesis).orElse(Parenthesis.Empty),
                beginningDelimiter, escape(tableAndColumn[0]), endingDelimter,
                beginningDelimiter, escape(tableAndColumn[1]), endingDelimter,
                criteria.operator,
                ofNullable(criteria.filter).orElse(""),
                endParenthesisString);

//        String[] tableAndColumn = criteria.column.split("\\.");
//        //if operator is IN or NOT IN
//        if (criteria.operator.equals(Operator.in) || criteria.operator.equals(Operator.notIn)) {
//            // This is the format of the criteria:  " %s %s%s%s%s.%s%s%s %s %s%s "
//            StringBuilder sb = new StringBuilder();
//            sb.append(" ").append(ofNullable(criteria.conjunction).orElse(Conjunction.Empty)).append(" ");
//            sb.append(" ").append(ofNullable(criteria.frontParenthesis).orElse(Parenthesis.Empty)).append(" ");
//            sb.append(beginningDelimiter).append(escape(tableAndColumn[0])).append(endingDelimter).append(".");
//            sb.append(beginningDelimiter).append(escape(tableAndColumn[1])).append(endingDelimter);
//            sb.append(" ").append(criteria.operator).append(" ("); // Opening parenthesis begins the IN or NOT IN list.
//
//            String[] filters = criteria.filter.toString().split(",");
//            for (String filter : filters) {
//                sb.append(":").append("filter").append(namedParameterCount).append(",");
//                namedParameterCount++;
//            }
//            // Remove trailing comma.
//            sb.deleteCharAt(sb.length()-1);
//            sb.append(")"); // Ends the IN or NOT IN list.
//            sb.append(endParenthesisString);
//
//            return sb.toString();
//        } else {
//            String filterNamedParameter = (criteria.operator.equals(Operator.isNull) || criteria.operator.equals(Operator.isNotNull)) ? "" : ":filter" + namedParameterCount;
//
//            String s = String.format(" %s %s%s%s%s.%s%s%s %s %s%s ",
//                       ofNullable(criteria.conjunction).orElse(Conjunction.Empty),
//                       ofNullable(criteria.frontParenthesis).orElse(Parenthesis.Empty),
//                       beginningDelimiter, escape(tableAndColumn[0]), endingDelimter,
//                       beginningDelimiter, escape(tableAndColumn[1]), endingDelimter,
//                       criteria.operator,
//                       filterNamedParameter,
//                       //":filter" + namedParameterCount,
//                       //ofNullable(criteria.filter).orElse(""),
//                       endParenthesisString);
//
//            namedParameterCount++;
//            return s;
//        }
    }

    /**
     * Gets all table schemas for the tables included in the columns and criteria parameters.
     * The function assumes that the columns are in the [table.column] format.
     */
    private void setTableSchemas() {
        MetaDataDaoImpl metaDataDao = new MetaDataDaoImpl(properties);

        Map<String, Map<String, Integer>> stmtTableSchemas = new HashMap<>();
        for (String col : stmt.getColumns()) {
            String[] tableAndColumn = col.split("\\.");
            if (tableAndColumn.length != 2) {
                throw new RuntimeException("A column needs to be in the format [table.column].  The ill-formatted column was " + col);
            }
            String table = tableAndColumn[0];
            String column = tableAndColumn[1];
            if (! stmtTableSchemas.containsKey(table)) {
                Map<String, Integer> tableSchema = metaDataDao.getTableSchema(table, column);
                stmtTableSchemas.put(table, tableSchema);
            }
        }

        for (Criteria crit : stmt.getCriteria()) {
            String[] tableAndColumn = crit.column.split("\\.");
            if (tableAndColumn.length != 2) {
                throw new RuntimeException("A column needs to be in the format [table.column].  The ill-formatted column was " + crit.column);
            }
            String table = tableAndColumn[0];
            String column = tableAndColumn[1];
            if (! stmtTableSchemas.containsKey(table)) {
                Map<String, Integer> tableSchema = metaDataDao.getTableSchema(table, column);
                stmtTableSchemas.put(table, tableSchema);
            }
        }

        this.tableSchemas = stmtTableSchemas;
    }

    /*
    Example parameterized sql

     */

    private void parseSubqueries() throws Exception {
        while (! allSubQueriesAreParsed()) {
            for (Map.Entry<String, String> subQuery : subQueries.entrySet()) {
                String subQueryId = subQuery.getKey();
                String subQueryName = subQuery.getValue().substring(0, subQuery.getValue().indexOf("("));
                String[] subQueryArgs = subQuery.getValue().substring(subQuery.getValue().indexOf("(") + 1, subQuery.getValue().indexOf(")")).split(",");

                // If there are no args, then there will be one element in subQueryArgs and it will be an empty string.
                if (subQueryArgs.length == 1 && subQueryArgs[0].equals("")) {
                    subQueryArgs = new String[0];
                }

                if (! parsedSubQueries.containsKey(subQueryId)) {
                    // run query if subQuery has no args
                    if (subQueryArgs.length == 0) {
                        SelectStatement queryTemplate = queryTemplateDao.getQueryTemplateByName(subQueryName);
                        String sql = queryTemplate.toSql(properties);
                        //String sql = buildSql(queryTemplate);
                        parsedSubQueries.put(subQueryId, sql);
                    } else { // else subquery has args
                        // test if it is a lowest level query by using contains("subquery")
                        if (! argsContainSubQuery(subQueryArgs)) {
                            String parsedSubQuery = buildSubQuery(subQueryId, subQueryName, subQueryArgs);
                            parsedSubQueries.put(subQueryId, parsedSubQuery);
                        } else {
                            for (String arg : subQueryArgs) {
                                // determine if arg is a subquery
                                if (argIsSubQuery(arg)) {
                                    if (parsedSubQueries.containsKey(arg)) {
                                        // subquery has already been parsed.
                                        break;
                                    } else {
                                        // subquery has NOT already been parsed.
                                        String parsedSubQuery = buildSubQuery(subQueryId, subQueryName, subQueryArgs);
                                        parsedSubQueries.put(subQueryId, parsedSubQuery);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean allSubQueriesAreParsed() {
        for (String subquery : subQueries.keySet()) {
            if (! parsedSubQueries.containsKey(subquery)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Tests if a String is a subquery qb4j expression.  If the String "subquery" is not at index 0 in the String, then
     * false.  Otherwise, true.
     * @param arg
     * @return
     */
    private boolean argIsSubQuery(String arg) {
        int begIndexOfSubQuery = arg.toLowerCase().indexOf("subquery");
        return begIndexOfSubQuery == 0;
    }

    private String buildSubQuery(String subQueryId, String subQueryName, String[] subQueryArgs) throws Exception {
        SelectStatement stmt = queryTemplateDao.getQueryTemplateByName(subQueryName);
        if (stmt != null) {
            stmt.setCriteriaArguments(getSubQueryArgs(subQueryArgs));
            return stmt.toSql(properties);
        } else {
            String message = String.format("Could not find statement object in database with name:  %s", subQueryName);
            throw new Exception(message);
        }
    }

//    private void addSqlParameters(Map<String, Object> sqlParameterSource) {
//        for (String key : sqlParameterSource.keySet()) {
//            sqlParameterMap.put(key, sqlParameterSource.get(key).toString());
//            namedParameterCount++;
//        }
//    }

    private boolean argsContainSubQuery(String[] args) {
        for (String arg : args) {
            if (arg.length() >= 8 && arg.substring(0,8).equals("subquery")) {
                return true;
            }
        }

        return false;
    }

    private Map<String, String> getSubQueryArgs(String[] argsArray) throws Exception {
        Map<String, String> args = new HashMap<>();
        //String[] argsAsArray = argsAsString.substring(argsAsString.indexOf("(") + 1, argsAsString.indexOf(")")).split(",");
        for (String paramNameAndArgString : argsArray) {
            if (! paramNameAndArgString.contains("=")) {
                String message = String.format("'%s' is not formatted properly.  It should be 'paramName=argument", paramNameAndArgString);
                throw new Exception(message);
            } else {
                String[] paramAndArgArray = paramNameAndArgString.split("=");
                args.put(paramAndArgArray[0], paramAndArgArray[1]);
            }
        }

        return args;
    }

    /**
     * Tests whether each column in the columns parameter has a table and column that can be found in the target database.
     * This method assumes that each column is in the format of [table.column].  After splitting the column on a period (.),
     * the method will return false if the resulting array does not have exactly 2 elements (a table and a column).
     *
     * @return boolean
     */
    private boolean statementTablesAndColumnsAreLegit() {
        boolean tableIsLegit = true;
        boolean columnIsLegit = true;

        if (tableSchemas.isEmpty()) {
            setTableSchemas();
        }

        // create list of statement's SELECT columns and WHERE columns.
        List<String> columns = new ArrayList<>(this.stmt.getColumns());
        this.stmt.getCriteria().forEach((criterion) -> columns.add(criterion.getColumn()));

        for (String column : this.stmt.getColumns()) {
            String[] tableAndColumn = column.split("\\.");

            // Check that the tableAndColumn variable has two elements.  The column format should be [table.column].
            if (tableAndColumn.length != 2) return false;

            // Now that we know that the tableAndColumn variable has 2 elements, test if the table and column can be found
            // in the database.
            tableIsLegit = tableSchemas.containsKey(tableAndColumn[TABLE_INDEX]);
            if (! tableIsLegit) return false;

            columnIsLegit = tableSchemas.get(tableAndColumn[TABLE_INDEX]).containsKey(tableAndColumn[COLUMN_INDEX]);
            if (! columnIsLegit) return false;

        }

        // check that statement's table is legit.
        if (! tableSchemas.containsKey(this.stmt.getTable())) return false;

        return true;
    }

    private boolean criteriaAreValid() throws Exception {
        for (Criteria criterion : this.stmt.getCriteria()) {
            if (! criterion.isValid()) {
                return false;
            }

            String BAD_SQL_EXCEPTION_MESSAGE = "%s failed to be clean SQL";
            if (! sqlIsClean(criterion)) {
                throw new Exception(String.format(BAD_SQL_EXCEPTION_MESSAGE, criterion));
            }

            // Now that we know that the criteria's operator is not 'isNull' or 'isNotNull', we can assume that the
            // criteria's filter is needed.  Therefore, we should check if the filter is null or an empty string.
            // If so, throw an exception.
            if ((! criterion.operator.equals(Operator.isNull) || ! criterion.operator.equals(Operator.isNotNull)) &&
                (criterion.filter == null || criterion.filter.equals(""))) {
                throw new Exception("The criteria has a null or empty filter, but the operator is not \"IsNull\" or \"IsNotNull\"");
            }

            if (tableSchemas.isEmpty()) {
                setTableSchemas();
            }

            String[] tableAndColumn = criterion.column.split("\\.");
            int columnDataType = getColumnDataType(tableAndColumn[TABLE_INDEX], tableAndColumn[COLUMN_INDEX]);
            boolean shouldHaveQuotes = isColumnQuoted(columnDataType);
            if (! shouldHaveQuotes) {
                if (! canParseNonQuotedFilter(criterion.filter.toString(), columnDataType)) {
                    throw new Exception(String.format(BAD_SQL_EXCEPTION_MESSAGE, criterion));
                }
            }
        }

        return true;
    }

    private boolean statementIsValid() throws Exception {
        if (this.stmt.getColumns() == null) {
            throw new IllegalArgumentException("Columns parameter is null");
        }
        if (this.stmt.getColumns().size() == 0) {
            throw new EmptyCollectionException("Columns parameter is empty");
        }
        if (this.stmt.getTable() == null || this.stmt.getTable().equals("")) {
            throw new IllegalArgumentException("The Table parameter cannot be null or an empty string.");
        }
        if (this.stmt.getJoins() == null) {
            throw new IllegalArgumentException("Joins parameter is null");
        }
        if (this.stmt.getCriteria() == null) {
            throw new IllegalArgumentException("The criteria parameter is null");
        }

        // Test that the columns in the stmt's columns and criteria fields can be found in the target database.
        if (! statementTablesAndColumnsAreLegit()) {
            throw new Exception("One of the columns in either the SelectStatement's columns or criteria fields is " +
                    "either is not in the correct [table.column] format or a column's table name or column name could not be " +
                    "found in the database.");
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
    private boolean canParseNonQuotedFilter(String s, int sqlType) throws Exception {
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

}
