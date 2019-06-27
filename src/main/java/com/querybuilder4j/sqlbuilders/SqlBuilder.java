package com.querybuilder4j.sqlbuilders;


import com.querybuilder4j.config.*;
import com.querybuilder4j.databasemetadata.QueryTemplateDao;
import com.querybuilder4j.exceptions.BadSqlException;
import com.querybuilder4j.statements.Criteria;
import com.querybuilder4j.statements.Join;
import com.querybuilder4j.statements.SelectStatement;
import com.querybuilder4j.validators.SelectStatementValidatorImpl;

import java.util.*;

import static com.querybuilder4j.sqlbuilders.SqlCleanser.escape;
import static com.querybuilder4j.sqlbuilders.SqlCleanser.sqlIsClean;

/**
 * This class uses a SelectStatement to generate a SELECT SQL string.
 */
public abstract class SqlBuilder {

//    /**
//     * A Map with the keys being JDBC Types and the values being booleans.  The boolean values represent whether filters
//     * with the given JDBC type should be quoted.  For example, if a column's JDBC type is INTEGER, then the value in the
//     * typeMappings field is false, because integers do not need to be wrapped in quotes in a SQL WHERE clause.  On the
//     * other hand, if a column's JDBC type is NVARCHAR, then the value in the typeMappings field is true, because varchar's
//     * do not need to be wrapped in quotes in a SQL WHERE clause.
//     */
//    protected final static Map<Integer, Boolean> typeMappings = new HashMap<>();

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

//    /**
//     * A Map with the values being the stmt's table columns and the values being their JDBC types.
//     */
//    //TODO:  pass tableSchemas to subqueries so that the metadata does not have to be fetched from the database again.
//    protected Map<String, Map<String, Integer>> tableSchemas = new HashMap<>();

//    /**
//     * The database connection properties.  The first three properties below are required and the last two are optional.
//     * 1) Database URL
//     * 2) JDCB driver class
//     *      ex) org.sqlite.JDBC
//     * 3) QueryBuilder4J DatabaseType
//     *      ex) Sqlite
//     * 4) Database username (optional)
//     * 5) Database password (optional)
//     */
//    protected final Properties properties;

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

//    /**
//     * A constant to be used after a column is split on "." and the resulting array is [table_name, column_name].  In
//     * such an array, index 0 returns table_name.
//     */
//    protected final int TABLE_INDEX = 0;
//
//    /**
//     * A constant to be used after a column is split on "." and the resulting array is [table_name, column_name].  In
//     * such an array, index 1 returns column_name.
//     */
//    protected final int COLUMN_INDEX = 1;

    /**
     * The class that will be used to retrieve subqueries, if they exist in the stmt.
     */
    protected QueryTemplateDao queryTemplateDao;

//    static {
//        typeMappings.put(Types.ARRAY, true);
//        typeMappings.put(Types.BIGINT, false);
//        typeMappings.put(Types.BINARY, true);
//        typeMappings.put(Types.BIT, false);
//        typeMappings.put(Types.BLOB, true);
//        typeMappings.put(Types.BOOLEAN, false);
//        typeMappings.put(Types.CHAR, true);
//        typeMappings.put(Types.CLOB, true);
//        //typeMappings.put(Types.DATALINK, false);
//        typeMappings.put(Types.DATE, true);
//        typeMappings.put(Types.DECIMAL, true);
//        typeMappings.put(Types.DISTINCT, true);
//        typeMappings.put(Types.DOUBLE, false);
//        typeMappings.put(Types.FLOAT, false);
//        typeMappings.put(Types.INTEGER, false);
//        typeMappings.put(Types.JAVA_OBJECT, true);
//        typeMappings.put(Types.LONGNVARCHAR, true);
//        typeMappings.put(Types.LONGVARBINARY, true);
//        typeMappings.put(Types.LONGVARCHAR, true);
//        typeMappings.put(Types.NCHAR, true);
//        typeMappings.put(Types.NCLOB, true);
//        typeMappings.put(Types.NULL, true);
//        typeMappings.put(Types.NUMERIC, false);
//        typeMappings.put(Types.NVARCHAR, true);
//        typeMappings.put(Types.OTHER, true);
//        typeMappings.put(Types.REAL, false);
//        typeMappings.put(Types.REF, true);
//        typeMappings.put(Types.REF_CURSOR, true);
//        typeMappings.put(Types.ROWID, false);
//        typeMappings.put(Types.SMALLINT, false);
//        typeMappings.put(Types.SQLXML, true);
//        typeMappings.put(Types.STRUCT, true);
//        typeMappings.put(Types.TIME, true);
//        typeMappings.put(Types.TIME_WITH_TIMEZONE, true);
//        typeMappings.put(Types.TIMESTAMP, true);
//        typeMappings.put(Types.TIMESTAMP_WITH_TIMEZONE, true);
//        typeMappings.put(Types.TINYINT, false);
//        typeMappings.put(Types.VARBINARY, true);
//        typeMappings.put(Types.VARCHAR, true);
//    }

    public SqlBuilder(SelectStatement stmt) throws Exception {
        this.stmt = stmt;
        if (stmt.getQueryTemplateDao() != null) { this.queryTemplateDao = stmt.getQueryTemplateDao(); } //todo:  is this needed?  The queryTemplateDao can just be called from the stmt.

//        if (! criteriaAreValid()) { throw new Exception("A criteria is not valid"); }

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

//        if (statementIsValid()) {
        buildSubQueries();
//        } else {
//            throw new RuntimeException("The statement was not valid");
//        }
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
                    beginningDelimiter, escape(tableAndColumn[Constants.TABLE_INDEX]), endingDelimter,
                    beginningDelimiter, escape(tableAndColumn[Constants.COLUMN_INDEX]), beginningDelimiter));
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
    protected StringBuilder createGroupByClause(List<String> columns) {
        StringBuilder sql = new StringBuilder(" GROUP BY ");

        for (String column : columns) {
            String[] tableAndColumn = column.split("\\.");
            sql.append(String.format("%s%s%s.%s%s%s, ",
                                      beginningDelimiter, escape(tableAndColumn[Constants.TABLE_INDEX]), endingDelimter,
                                      beginningDelimiter, escape(tableAndColumn[Constants.COLUMN_INDEX]), endingDelimter));
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
    protected StringBuilder createOrderByClause(List<String> columns, boolean ascending) {
        StringBuilder sql = new StringBuilder(" ORDER BY ");

        for (String column : columns) {
            String[] tableAndColumn = column.split("\\.");
            sql.append(String.format("%s%s%s.%s%s%s, ",
                    beginningDelimiter, escape(tableAndColumn[Constants.TABLE_INDEX]), endingDelimter,
                    beginningDelimiter, escape(tableAndColumn[Constants.COLUMN_INDEX]), endingDelimter));
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
    protected StringBuilder createSuppressNullsClause(List<String> columns) {
        StringBuilder sql = new StringBuilder();

        for (int i=0; i<columns.size(); i++) {
            String[] tableAndColumn = columns.get(i).split("\\.");
            if (i == 0) {
                sql.append(String.format(" (%s%s%s.%s%s%s IS NOT NULL ",
                                            beginningDelimiter, tableAndColumn[Constants.TABLE_INDEX], endingDelimter,
                                            beginningDelimiter, tableAndColumn[Constants.COLUMN_INDEX], endingDelimter));
            } else {
                sql.append(String.format(" OR %s%s%s.%s%s%s IS NOT NULL ",
                                           beginningDelimiter, tableAndColumn[Constants.TABLE_INDEX], endingDelimter,
                                           beginningDelimiter, tableAndColumn[Constants.COLUMN_INDEX], endingDelimter));
            }
        }

        return sql.append(") ");
    }

//    /**
//     * Gets the SQL JDBC Type for the table and column parameters.
//     *
//     * @param table
//     * @param columnName
//     * @return int
//     * @throws ColumnNameNotFoundException
//     */
//    private int getColumnDataType(String table, String columnName) throws ColumnNameNotFoundException {
//        Integer dataType = tableSchemas.get(table).get(columnName);
//
//        if (dataType == null) {
//            throw new ColumnNameNotFoundException(String.format("Could not find column, %s", columnName));
//        } else {
//            return dataType;
//        }
//    }

//    /**
//     *
//     * First, gets the SQL JDBC Type for the table and column parameters.  Then, gets a boolean from the typeMappings
//     * class field associated with the SQL JDBC Types parameter, which is an int.  The typeMappings field will return
//     * true if the SQL JDBC Types parameter should be quoted in a WHERE SQL clause and false if it should not be quoted.
//     *
//     * For example, the VARCHAR Type will return true, because it should be wrapped in single quotes in a WHERE SQL condition.
//     * On the other hand, the INTEGER Type will return false, because it should NOT be wrapped in single quotes in a WHERE SQL condition.
//     *
//     * @param table
//     * @param columnName
//     * @return boolean
//     * @throws DataTypeNotFoundException
//     * @throws ColumnNameNotFoundException
//     */
//    private boolean isColumnQuoted(String table, String columnName) throws DataTypeNotFoundException, ColumnNameNotFoundException {
//        Integer dataType = getColumnDataType(table, columnName);
//
//        Boolean isQuoted = typeMappings.get(dataType);
//
//        if (isQuoted == null) { throw new DataTypeNotFoundException(String.format("Data type, %s, is not recognized", dataType)); }
//
//        return isQuoted;
//    }

//    /**
//     * Gets all table schemas for the tables included in the columns and criteria parameters.
//     * The function assumes that the columns are in the [table.column] format.
//     */
//    private void setTableSchemas() {
//        MetaDataDaoImpl metaDataDao = new MetaDataDaoImpl(properties);
//
//        // Create list with all columns in it - from both columns and criteria collections.
//        List<String> allColumns = new ArrayList<>(stmt.getColumns());
//        stmt.getCriteria().forEach((criterion) -> allColumns.add(criterion.getColumn()));
//
//        for (String col : allColumns){
//            String[] tableAndColumn = col.split("\\.");
//
//            if (tableAndColumn.length != 2) {
//                throw new RuntimeException("A column needs to be in the format [table.column].  The ill-formatted column was " + col);
//            }
//
//            String table = tableAndColumn[0];
//            String column = tableAndColumn[1];
//            if (! this.tableSchemas.containsKey(table)) {
//                Map<String, Integer> tableSchema = metaDataDao.getTableSchema(table, column);
//                this.tableSchemas.put(table, tableSchema);
//            }
//        }
//    }

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

//    /**
//     * Tests whether each column in the columns parameter has a table and column that can be found in the target database.
//     * This method assumes that each column is in the format of [table.column].  After splitting the column on a period (.),
//     * the method will return false if the resulting array does not have exactly 2 elements (a table and a column).
//     *
//     * @return boolean
//     */
//    private boolean statementTablesAndColumnsAreLegit() {
//        boolean tableIsLegit;
//        boolean columnIsLegit;
//
//        if (tableSchemas.isEmpty()) {
//            setTableSchemas();
//        }
//
//        // Create list of statement's SELECT columns and WHERE columns.
//        List<String> columns = new ArrayList<>(this.stmt.getColumns());
//        this.stmt.getCriteria().forEach((criterion) -> columns.add(criterion.getColumn()));
//
//        for (String column : this.stmt.getColumns()) {
//            String[] tableAndColumn = column.split("\\.");
//
//            // Check that the tableAndColumn variable has two elements.  The column format should be [table.column].
//            if (tableAndColumn.length != 2) return false;
//
//            // Now that we know that the tableAndColumn variable has 2 elements, test if the table and column can be found
//            // in the database.
//            tableIsLegit = tableSchemas.containsKey(tableAndColumn[TABLE_INDEX]);
//            if (! tableIsLegit) return false;
//
//            columnIsLegit = tableSchemas.get(tableAndColumn[TABLE_INDEX]).containsKey(tableAndColumn[COLUMN_INDEX]);
//            if (! columnIsLegit) return false;
//
//        }
//
//        // Check that statement's table is legit.
//        if (! tableSchemas.containsKey(this.stmt.getTable())) return false; //todo:  simplify this.
//
//        return true;
//    }

//    /**
//     * Determines if the stmt's criteria are valid.
//     *
//     * @return boolean
//     * @throws Exception
//     */
//    private boolean criteriaAreValid() throws Exception {
//        if (tableSchemas.isEmpty()) { setTableSchemas(); }
//
//        for (Criteria criterion : this.stmt.getCriteria()) {
//            if (! criterion.isValid()) { return false; }
//
//            if (! sqlIsClean(criterion)) { throw new Exception(String.format("%s failed to be clean SQL", criterion)); }
//
//            // Now that we know that the criteria's operator is not 'isNull' or 'isNotNull', we can assume that the
//            // criteria's filter is needed.  Therefore, we should check if the filter is null or an empty string.
//            // If so, throw an exception.
//            if (! criterion.operator.equals(Operator.isNull)) {
//                if (! criterion.operator.equals(Operator.isNotNull)) {
//                    if (criterion.filter != null) {
//                        String[] tableAndColumn = criterion.column.split("\\.");
//                        boolean shouldHaveQuotes = isColumnQuoted(tableAndColumn[TABLE_INDEX], tableAndColumn[COLUMN_INDEX]);
//
//                        if (! shouldHaveQuotes && criterion.filter != null) {
//                            String[] filters = criterion.filter.split(",");
//                            for (String filter : filters) {
//                                int columnDataType = getColumnDataType(tableAndColumn[TABLE_INDEX], tableAndColumn[COLUMN_INDEX]);
//                                if (! SqlCleanser.canParseNonQuotedFilter(filter, columnDataType)) {
//                                    throw new Exception(String.format("The criteria's filter is not an number type, but the column is:  %s", criterion));
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        return true;
//    }

//    /**
//     * Determines if the stmt is valid.
//     *
//     * @return boolean
//     * @throws Exception
//     */
//    private boolean statementIsValid() throws Exception {
//        if (this.stmt.getColumns() == null) {
//            throw new IllegalArgumentException("Columns parameter is null");
//        }
//        if (this.stmt.getColumns().size() == 0) {
//            throw new EmptyCollectionException("Columns parameter is empty");
//        }
//        if (this.stmt.getTable() == null || this.stmt.getTable().equals("")) {
//            throw new IllegalArgumentException("The Table parameter cannot be null or an empty string.");
//        }
//        if (this.stmt.getJoins() == null) {
//            throw new IllegalArgumentException("Joins parameter is null");
//        }
//        if (this.stmt.getCriteria() == null) {
//            throw new IllegalArgumentException("The criteria parameter is null");
//        }
//
//        // Test that the columns in the stmt's columns and criteria fields can be found in the target database.
//        if (! statementTablesAndColumnsAreLegit()) {
//            throw new Exception("One of the columns in either the SelectStatement's columns or criteria fields is " +
//                    "either is not in the correct [table.column] format or a column's table name or column name could not be " +
//                    "found in the database.");
//        }
//
//        return true;
//    }

}
