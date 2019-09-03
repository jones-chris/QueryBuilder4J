package com.querybuilder4j.sqlbuilders;


import com.querybuilder4j.config.*;
import com.querybuilder4j.databasemetadata.QueryTemplateDao;
import com.querybuilder4j.exceptions.BadSqlException;
import com.querybuilder4j.statements.*;
import com.querybuilder4j.validators.SelectStatementValidatorImpl;

import java.util.*;

import static com.querybuilder4j.sqlbuilders.SqlCleanser.escape;

/**
 * This class uses a SelectStatement to generate a SELECT SQL string.
 */
public abstract class SqlBuilder {

    /**
     * The character to begin wrapping the table and column in a SQL statement.  For example, PostgreSQL uses a double quote
     * to wrap the table and column in a SELECT SQL statement like so:  SELECT "employees"."name" FROM "employees".  MySQL
     * uses back ticks like so:  SELECT `employees`.`name` from `employees`.
     */
    protected char beginningDelimiter;

    /**
     * The character to end wrapping the table and column in a SQL statement.  For example, PostgreSQL uses a double quote
     * to wrap the table and column in a SELECT SQL statement like so:  SELECT "employees"."name" FROM "employees".  MySQL
     * uses back ticks like so:  SELECT `employees`.`name` from `employees`.
     */
    protected char endingDelimter;

    /**
     * The SelectStatement that encapsulates the data to generate the SELECT SQL string.
     */
    protected SelectStatement stmt;

    /**
     * A Map of the stmt's subqueries with the key being the subquery id (subquery0, subquery1, etc) and the value being
     * the subquery deserialized into a SelectStatement object.
     */
    protected Map<String, SelectStatement> unbuiltSubQueries = new HashMap<>();

    /**
     * A Map of the stmt's subqueries with the key being the subquery id (subquery0, subquery1, etc) and the value being
     * the SELECT SQL string generated from the SelectStatement object in the subQueries field of this class.
     */
    protected Map<String, String> builtSubQueries = new HashMap<>();

    /**
     * The class that will be used to retrieve subqueries, if they exist in the stmt.
     */
    protected QueryTemplateDao queryTemplateDao;

    public SqlBuilder(SelectStatement stmt) throws Exception {
        this.stmt = stmt;
        if (stmt.getQueryTemplateDao() != null) { this.queryTemplateDao = stmt.getQueryTemplateDao(); } //todo:  is this needed?  The queryTemplateDao can just be called from the stmt.

        // First, get all SelectStatements that are listed in subqueries.  Later we will replace the params in each subquery.
        // TODO:  this eager loads the subqueries.  It may be beneficial to consider having a class boolean field for lazy loading.
        if (stmt.getSubQueries().size() != 0 && queryTemplateDao != null) {
            this.stmt.getSubQueries().forEach((subQueryId, subQueryCall) -> {
                String subQueryName = subQueryCall.substring(0, subQueryCall.indexOf("("));
                SelectStatement queryTemplate = queryTemplateDao.getQueryTemplateByName(subQueryName);

                if (queryTemplate == null) {
                    throw new RuntimeException(String.format("Could not find subquery named %s in SqlBuilder's queryTemplateDao", subQueryName));
                } else {
                    this.unbuiltSubQueries.put(subQueryId, queryTemplate);
                }
            });
        }

        buildSubQueries();
    }

    public abstract String buildSql() throws Exception;

    /**
     * Creates the SELECT clause of a SELECT SQL statement.
     *
     * @param distinct
     * @param columns
     * @return StringBuilder
     */
    protected StringBuilder createSelectClause(boolean distinct, List<Column> columns) throws Exception {
        String startSql = (distinct) ? "SELECT DISTINCT " : "SELECT ";
        StringBuilder sql = new StringBuilder(startSql);
        for (Column column : columns) {
            String columnSql = column.toSql(beginningDelimiter, endingDelimter);

//            String table = column.split("\\.")[Constants.TABLE_INDEX];
//            String columnName = column.split("\\.")[Constants.COLUMN_INDEX];
//            String columnSql = String.format("%s%s%s.%s%s%s",
//                    beginningDelimiter, escape(table), endingDelimter,
//                    beginningDelimiter, escape(columnName), endingDelimter);
            sql.append(columnSql)
                    .append(", ");

            // if column as alias, then format
//            if (column.hasAlias()) {
//                String[] tableAndColumn = column.getDatabaseName().split("\\.");
//                sql.append(String.format("%s%s%s.%s%s%s AS %s%s%s, ",
//                        beginningDelimiter, escape(tableAndColumn[Constants.TABLE_INDEX]), endingDelimter,
//                        beginningDelimiter, escape(tableAndColumn[Constants.COLUMN_INDEX]), beginningDelimiter,
//                        beginningDelimiter, escape(column.getAlias()), endingDelimter));
//            } else {
//                String[] tableAndColumn = column.getDatabaseName().split("\\.");
//                sql.append(String.format("%s%s%s.%s%s%s, ",
//                        beginningDelimiter, escape(tableAndColumn[Constants.TABLE_INDEX]), endingDelimter,
//                        beginningDelimiter, escape(tableAndColumn[Constants.COLUMN_INDEX]), beginningDelimiter));
//            }
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
                String[] parentJoinTableAndColumn = join.getParentJoinColumns().get(j).split("\\.");
                String[] targetJoinTableAndColumn = join.getTargetJoinColumns().get(j).split("\\.");

                //Format string in the form of " [ON/AND] `table1`.`column1` = `table2`.`column2` ", assuming the database
                // type is MySql.
                sb.append(String.format(" %s %s%s%s.%s%s%s = %s%s%s.%s%s%s ",
                        conjunction,
                        beginningDelimiter, parentJoinTableAndColumn[0], endingDelimter,
                        beginningDelimiter, parentJoinTableAndColumn[1], endingDelimter,
                        beginningDelimiter, targetJoinTableAndColumn[0], endingDelimter,
                        beginningDelimiter, targetJoinTableAndColumn[1], endingDelimter));
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

            for (Criteria criterion : criteria) {
                // Clone the criterion in case the criterion is reused - such as in a desktop app.  This method changes
                // the criterion, so it makes a clone of the criterion and makes changes to the clone so that the original
                // criterion is unchanged.
                Criteria criteriaClone = (Criteria) criterion.clone();

                if (criteriaClone.getId() == 0) {
                    criteriaClone.conjunction = null;
                }

                if (criteriaClone.operator.equals(Operator.isNull) || criteriaClone.operator.equals(Operator.isNotNull)) {
                    criteriaClone.filter = null;
                    String criteriaSql = criteriaClone.toSql(beginningDelimiter, endingDelimter);
                    sql.append(String.format(" %s ", criteriaSql));
                    continue;
                } else {
                    // Now that we know the operator expects a filter, check if the filter is null.  If so, make it an
                    // empty string.
                    if (criteriaClone.filter == null) { criteriaClone.filter = ""; }
                }

                // The criteria's filter should be the subquery id that can be retrieved from builtSubQueries.
                String[] args = criteriaClone.filter.split(",");
                String[] newArgs = args.clone();
                for (int i=0; i<args.length; i++) {
                    String arg = args[i];

                    if (argIsSubQuery(arg)) {
                        String subquery = builtSubQueries.get(arg);

                        if (subquery == null) { throw new RuntimeException("Could not find subquery with name:  " + arg); }

                        newArgs[i] = "(" + subquery + ")";
                    } else {
                        arg = escape(arg);

                        String[] tableAndColumn = criteriaClone.column.split("\\.");
                        String table = tableAndColumn[Constants.TABLE_INDEX];
                        String column = tableAndColumn[Constants.COLUMN_INDEX];
                        Map<String, Map<String, Integer>> tableColumnTypes = this.stmt.getDatabaseMetaData()
                                .getTablesMetaData()
                                .getTableColumnsTypes();

                        boolean shouldHaveQuotes = SelectStatementValidatorImpl.isColumnQuoted(table, column, tableColumnTypes); //todo:  make this a method in a Util class?
                        if (shouldHaveQuotes) { arg = "'" + escape(arg) + "'"; }

                        newArgs[i] = arg;
                    }
                }
                criteriaClone.filter = String.join(",", newArgs);

                // If the filter is 1) IN or NOT IN and 2) the first char is "(" or the last char is ")", then wrap in
                // parenthesises.
                if ((criteriaClone.operator.equals(Operator.in) || criteriaClone.operator.equals(Operator.notIn)) &&
                    (criteriaClone.filter.charAt(0) != '(' || criteriaClone.filter.charAt(criteriaClone.filter.length()-1) != ')')) {
                    criteriaClone.filter = "(" + criteriaClone.filter + ")";
                }

                String criteriaSql = criteriaClone.toSql(beginningDelimiter, endingDelimter);
                sql.append(criteriaSql).append(" ");
            }
        }

        return sql;
    }

    /**
     * Creates the GROUP BY clause of a SELECT SQL statement.
     *
     * @param columns
     * @return StringBuilder
     */
    protected StringBuilder createGroupByClause(List<Column> columns) throws Exception {
        StringBuilder sql = new StringBuilder(" GROUP BY ");

        for (Column column : columns) {
            sql.append(column.toSql(beginningDelimiter, endingDelimter))
                    .append(", ");

//            String[] tableAndColumn = column.split("\\.");
//            sql.append(String.format("%s%s%s.%s%s%s, ",
//                                      beginningDelimiter, escape(tableAndColumn[Constants.TABLE_INDEX]), endingDelimter,
//                                      beginningDelimiter, escape(tableAndColumn[Constants.COLUMN_INDEX]), endingDelimter));
        }

        return sql.delete(sql.length() - 2, sql.length()).append(" ");
    }

    /**
     * Creates the ORDER BY clause of a SELECT SQL statement.
     *
     * @param columns
     * @param ascending
     * @return StringBuilder
     */
    protected StringBuilder createOrderByClause(List<Column> columns, boolean ascending) throws Exception {
        StringBuilder sql = new StringBuilder(" ORDER BY ");

        for (Column column : columns) {
            sql.append(column.toSql(beginningDelimiter, endingDelimter))
                    .append(", ");

//            String[] tableAndColumn = column.split("\\.");
//            sql.append(String.format("%s%s%s.%s%s%s, ",
//                    beginningDelimiter, escape(tableAndColumn[Constants.TABLE_INDEX]), endingDelimter,
//                    beginningDelimiter, escape(tableAndColumn[Constants.COLUMN_INDEX]), endingDelimter));
        }

        sql.delete(sql.length() - 2, sql.length()).append(" ");
        return (ascending) ? sql.append(" ASC ") : sql.append(" DESC ");
    }

    /**
     * Creates the LIMIT clause of a SELECT SQL statement.
     *
     * @param limit
     * @return StringBuilder
     */
    protected StringBuilder createLimitClause(Long limit) throws IllegalArgumentException {
        if (limit == null) {
            return new StringBuilder();
        }

        return new StringBuilder(String.format(" LIMIT %s ", limit));
    }

    /**
     * Creates the OFFSET clause of a SELECT SQL statement.
     *
     * @param offset
     * @return StringBuilder
     */
    protected StringBuilder createOffsetClause(Long offset) throws IllegalArgumentException {
        if (offset == null) {
            return new StringBuilder();
        }

        return new StringBuilder(String.format(" OFFSET %s ", offset));
    }

    /**
     * Creates a WHERE clause condition that all columns in the columns parameter cannot be null.  This condition
     * should is used to not return records where all selected columns have a null value.
     *
     * @param columns
     * @return StringBuilder
     */
    protected StringBuilder createSuppressNullsClause(List<Column> columns) throws Exception {
        StringBuilder sql = new StringBuilder();

        for (int i=0; i<columns.size(); i++) {
//            String[] tableAndColumn = columns.get(i).split("\\.");
            Column column = columns.get(i);
            if (i == 0) {
                sql.append("(")
                        .append(column.toSql(beginningDelimiter, endingDelimter))
                        .append(" IS NOT NULL ");
//                sql.append(String.format(" (%s%s%s.%s%s%s IS NOT NULL ",
//                                            beginningDelimiter, tableAndColumn[Constants.TABLE_INDEX], endingDelimter,
//                                            beginningDelimiter, tableAndColumn[Constants.COLUMN_INDEX], endingDelimter));
            } else {
                sql.append(" OR ")
                        .append(column.toSql(beginningDelimiter, endingDelimter))
                        .append(" IS NOT NULL ");
//                sql.append(String.format(" OR %s%s%s.%s%s%s IS NOT NULL ",
//                                           beginningDelimiter, tableAndColumn[Constants.TABLE_INDEX], endingDelimter,
//                                           beginningDelimiter, tableAndColumn[Constants.COLUMN_INDEX], endingDelimter));
            }
        }

        return sql.append(") ");
    }

    /**
     * This method controls building subqueries.
     *
     * The overall flow is that each subquery in this.stmt.subQueries, which contains the
     * raw query name call and arguments, is retrieved using this.queryTemplateDao and deserialized into a SelectStatement,
     * which is added to this.unbuiltSubQueries to await being built.
     *
     * Then, each subquery in this.unbuiltSubQueries is
     * built by calling the toSql() method on each subquery because they are each a SelectStatement object.  When a subquery is
     * built, the resulting SELECT SQL string is added to this.builtSubQueries.
     *
     * Lastly, this.builtSubQueries is referenced by the this.createWhereClause() method to create the WHERE clause of the
     * SELECT SQL string.
     *
     * @throws Exception
     */
    protected void buildSubQueries() throws Exception {
        while (! allSubQueriesAreBuilt()) {
            for (Map.Entry<String, String> subQuery : this.stmt.getSubQueries().entrySet()) {
                String subQueryId = subQuery.getKey();
                String subQueryName = subQuery.getValue().substring(0, subQuery.getValue().indexOf("("));
                String[] subQueryArgs = subQuery.getValue().substring(subQuery.getValue().indexOf("(") + 1, subQuery.getValue().indexOf(")")).split(";");

                // If there are no args, then there will be one element in subQueryArgs and it will be an empty string.
                if (subQueryArgs.length == 1 && subQueryArgs[0].equals("")) {
                    subQueryArgs = new String[0];
                }

                if (! builtSubQueries.containsKey(subQueryId)) {
                    // run query if subQuery has no args
                    if (subQueryArgs.length == 0) {
                        SelectStatement queryTemplate = unbuiltSubQueries.get(subQueryId);
                        String sql = queryTemplate.toSql(this.stmt.getDatabaseMetaData().getProperties());
                        builtSubQueries.put(subQueryId, sql);
                    } else { // else subquery has args
                        // test if it is a lowest level query by using contains("subquery")
                        if (! argsContainSubQuery(subQueryArgs)) {
                            String builtSubQuery = buildSubQuery(subQueryId, subQueryName, subQueryArgs);
                            builtSubQueries.put(subQueryId, builtSubQuery);
                        } else {
                            for (String arg : subQueryArgs) {
                                // determine if arg is a subquery
                                if (argIsSubQuery(arg)) {
                                    if (builtSubQueries.containsKey(arg)) {
                                        // subquery has already been built.
                                        break;
                                    } else {
                                        // subquery has NOT already been built.
                                        String builtSubQuery = buildSubQuery(subQueryId, subQueryName, subQueryArgs);
                                        builtSubQueries.put(subQueryId, builtSubQuery);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    /**
     * Determines if all subqueries are built.
     * @return boolean
     */
    private boolean allSubQueriesAreBuilt() {
        for (String subquery : unbuiltSubQueries.keySet()) {
            if (! builtSubQueries.containsKey(subquery)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Tests if a String is a '$', which is the subquery qb4j expression.  If the String "$" is not at index 0 in the String, then
     * false.  Otherwise, true.
     * @param arg
     * @return boolean
     */
    public static boolean argIsSubQuery(String arg) {
        if (arg == null || arg.isEmpty()) {
            return false;
        } else {
            return 0 == arg.toLowerCase().indexOf("$");
        }
    }

    private String buildSubQuery(String subQueryId, String subQueryName, String[] subQueryArgs) throws Exception {
        SelectStatement stmt = unbuiltSubQueries.get(subQueryId);
        if (stmt != null) {
            stmt.setCriteriaArguments(getSubQueryArgs(subQueryArgs));
            stmt.setQueryTemplateDao(this.queryTemplateDao);
            stmt.setSubQueries(getRelevantSubQueries(subQueryArgs));
            return stmt.toSql(this.stmt.getDatabaseMetaData().getProperties());
        } else {
            String message = String.format("Could not find statement object with name:  %s", subQueryName);
            throw new Exception(message);
        }
    }

    /**
     * Gets all of the subqueries that match the subQueryArgs.  The resulted Map is intended to be used to set a child
     * SelectStatement's subqueries property so that SQL can be generated correctly.
     *
     * @param subQueryArgs
     * @return Map<String, String>
     */
    private Map<String, String> getRelevantSubQueries(String[] subQueryArgs) {
        Map<String, String> relevantSubQueries = new HashMap<>();
        for (String paramAndArg : subQueryArgs) {
            String arg = paramAndArg.split("=")[1];
            if (argIsSubQuery(arg)) {
                String subQueryCall = this.stmt.getSubQueries().get(arg);
                relevantSubQueries.put(arg, subQueryCall);
            }
        }
        return relevantSubQueries;
    }

    /**
     * Determines if an arg is a subquery.
     *
     * @param args
     * @return boolean
     */
    private boolean argsContainSubQuery(String[] args) {
        for (String arg : args) {
            if (arg.length() >= 8 && arg.substring(0,8).equals("subquery")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a Map of a subquery's arguments with the keys being the parameters and the values being the arguments.
     *
     * @param argsArray
     * @return Map<String, String>
     * @throws Exception
     */
    private Map<String, String> getSubQueryArgs(String[] argsArray) throws Exception {
        Map<String, String> args = new HashMap<>();
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

}
