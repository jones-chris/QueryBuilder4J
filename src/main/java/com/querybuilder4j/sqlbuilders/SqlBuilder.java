package com.querybuilder4j.sqlbuilders;


import com.querybuilder4j.SubQueryParser;
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
     * The class responsible for parsing subqueries.
     */
    protected SubQueryParser subQueryParser;


    public SqlBuilder(SelectStatement stmt) throws Exception {
        this.stmt = stmt;
        this.subQueryParser = new SubQueryParser(this.stmt);
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

            sql.append(columnSql)
                    .append(", ");
        }

        return sql.delete(sql.length() - 2, sql.length()).append(" ");
    }

    /**
     * Creates the FROM clause of a SELECT SQL statement.
     *
     * @param table
     * @return
     * @throws IllegalArgumentException
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

                    if (SubQueryParser.argIsSubQuery(arg)) {
                        String subquery = subQueryParser.getBuiltSubQueries().get(arg);

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
            Column column = columns.get(i);
            if (i == 0) {
                sql.append("(")
                        .append(column.toSql(beginningDelimiter, endingDelimter))
                        .append(" IS NOT NULL ");
            } else {
                sql.append(" OR ")
                        .append(column.toSql(beginningDelimiter, endingDelimter))
                        .append(" IS NOT NULL ");
            }
        }

        return sql.append(") ");
    }

}
