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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.querybuilder4j.sqlbuilders.SqlCleanser.escape;
import static com.querybuilder4j.sqlbuilders.SqlCleanser.sqlIsClean;
import static java.util.Optional.ofNullable;

public abstract class SqlBuilder {
    protected static Map<Integer, Boolean> typeMappings = new HashMap<>();
    protected char beginningDelimiter;
    protected char endingDelimter;
    protected Map<String, Map<String, Integer>> tableSchemas;
    protected SelectStatement stmt;
    private final String BAD_SQL_EXCEPTION_MESSAGE = "%s failed to be clean SQL";

    static {
        typeMappings.put(Types.ARRAY, true);                     //ARRAY
        typeMappings.put(Types.BIGINT, false);                   //BIGINT
        typeMappings.put(Types.BINARY, true);                    //BINARY
        typeMappings.put(Types.BIT, false);                      //BIT
        typeMappings.put(Types.BLOB, true);                      //BLOB
        typeMappings.put(Types.BOOLEAN, false);                  //BOOLEAN
        typeMappings.put(Types.CHAR, true);                      //CHAR
        typeMappings.put(Types.CLOB, true);                      //CLOB
        typeMappings.put(Types.DATALINK, false);                 //DATALINK
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


    public SqlBuilder(SelectStatement stmt, Properties properties) {
        this.stmt = stmt;

        // Get all relevant table schemas.
        MetaDataDaoImpl metaDataDao = new MetaDataDaoImpl(properties);
        Map<String, Map<String, Integer>> whereClauseTableSchemas = new HashMap<>();
        for (Criteria criteria : this.stmt.getCriteria()) {
            String table = criteria.column.split("\\.")[0];
            String column = criteria.column.split("\\.")[1];
            if (! whereClauseTableSchemas.containsKey(table)) {
                Map<String, Integer> tableSchema = metaDataDao.getTableSchema(table, column);
                whereClauseTableSchemas.put(table, tableSchema);
            }
        }
        this.stmt.setTableSchemas(whereClauseTableSchemas);
    }

    public abstract String buildSql(SelectStatement query) throws Exception;

    protected StringBuilder createSelectClause(boolean distinct, List<String> columns)
            throws IllegalArgumentException, EmptyCollectionException, BadSqlException {
        if (columns == null) throw new IllegalArgumentException("Columns parameter is null");

        if (columns.size() == 0) throw new EmptyCollectionException("Columns parameter is empty");

        final int TABLE_INDEX = 0;
        final int COLUMN_INDEX = 1;

        boolean tableIsClean = true;
        boolean columnIsClean = true;
        for (String column : columns) {
            String[] tableAndColumn = column.split("\\.");
            if (tableAndColumn.length != 2) {
                throw new EmptyCollectionException(String.format("The column, %s, needs to be in the format [table.column]", column));
            }
            tableIsClean = sqlIsClean(tableAndColumn[TABLE_INDEX]);
            columnIsClean = sqlIsClean(tableAndColumn[COLUMN_INDEX]);
            if (! tableIsClean || ! columnIsClean) {
                throw new BadSqlException(column + " failed to be clean SQL");
            }
        }

        String startSql = (distinct) ? "SELECT DISTINCT " : "SELECT ";
        StringBuilder sql = new StringBuilder(startSql);
        for (String column : columns) {
            String[] tableAndColumn = column.split("\\.");
            sql.append(String.format("%s%s%s.%s%s%s, ",
                    beginningDelimiter, escape(tableAndColumn[TABLE_INDEX]), endingDelimter,
                    beginningDelimiter, escape(tableAndColumn[COLUMN_INDEX]), beginningDelimiter));
        }
        sql = sql.delete(sql.length() - 2, sql.length()).append(" ");
        return sql;
    }

    protected StringBuilder createFromClause(String table) throws IllegalArgumentException, BadSqlException {
        if (table == null) throw new IllegalArgumentException("table parameter is null");

        if (table.equals("")) throw new IllegalArgumentException("The table argument is an empty string");

        if (! sqlIsClean(table)) throw new BadSqlException(table + " failed to be clean SQL");

        String s = String.format(" FROM %s%s%s ", beginningDelimiter, escape(table), endingDelimter);
        return new StringBuilder(s);
    }

    protected StringBuilder createJoinClause(List<Join> joins) throws IllegalArgumentException {

        if (joins == null) throw new IllegalArgumentException("joins parameter is null");

        if (joins.size() == 0) return null;

        StringBuilder sb = new StringBuilder("");
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

        if (criteria == null) throw new IllegalArgumentException("The criteria parameter is null");

//        boolean criteriaAreClean = true;
//        for (Criteria crit : criteria) {
//            criteriaAreClean = sqlIsClean(crit);
//            if (! criteriaAreClean) throw new BadSqlException(crit + " failed to be clean SQL");
//        }

        if (criteria.size() == 0) {
            return null;
        } else {
            StringBuilder sql = new StringBuilder(" WHERE ");

            for (Criteria crit : criteria) {
                if (! sqlIsClean(crit)) throw new BadSqlException(crit + " failed to be clean SQL");

                // clone criteria
                try {
                    Criteria criteriaClone = (Criteria) crit.clone();
                    if (! criteriaClone.isValid()) throw new BadSqlException(String.format("The criteria in position %d is not valid"));

                    if (criteriaClone.getId() == 0) criteriaClone.conjunction = null;

                    if (criteriaClone.operator.equals(Operator.isNull) || criteriaClone.operator.equals(Operator.isNotNull)) {
                        criteriaClone.filter = null;
                        if (! sqlIsClean(criteriaClone)) {
                            throw new BadSqlException(String.format(BAD_SQL_EXCEPTION_MESSAGE, criteriaClone));
                        }
                        sql.append(String.format(" %s ", stringifyCriteria(criteriaClone)));
                        continue;
                    }

                    // Now that we know that the criteria's operator is not 'isNull' or 'isNotNull', we can assume that the
                    // criteria's filter is needed.  Therefore, we should check if the filter is null or an empty string.
                    // If so, throw an exception.
                    if (criteriaClone.filter == null || criteriaClone.filter.equals("")) {
                        throw new BadSqlException(String.format("The criteria in position %d has a null or empty filter, but the operator is not \"IsNull\" or \"IsNotNull\""));
                    }

//                        if (criteriaClone.isFilterSubquery()) {
//                            criteriaClone.filter = escapeAndRemove(criteriaClone.filter);
//                            sql.append(String.format(" %s ", criteriaClone.toString()));
//                            continue;
//                        }

                    String[] tableAndColumn = criteriaClone.column.split("\\.");
                    boolean shouldHaveQuotes = isColumnQuoted(getColumnDataType(tableAndColumn[0], tableAndColumn[1]));
                    if (criteriaClone.operator.equals(Operator.in) || criteriaClone.operator.equals(Operator.notIn)) {
                        if (shouldHaveQuotes) {
                            wrapFilterInQuotes(criteriaClone);
                        } else {
                            criteriaClone.filter = "(" + escape(criteriaClone.filter) + ")";
                        }

                        if (! sqlIsClean(criteriaClone)) {
                            throw new BadSqlException(String.format(BAD_SQL_EXCEPTION_MESSAGE, criteriaClone));
                        }
                        sql.append(stringifyCriteria(criteriaClone)).append(" ");
                    } else {
                        criteriaClone.filter = (shouldHaveQuotes) ? "'" + escape(criteriaClone.filter) + "'" : escape(criteriaClone.filter);
                        if (! sqlIsClean(criteriaClone)) {
                            throw new BadSqlException(String.format(BAD_SQL_EXCEPTION_MESSAGE, criteriaClone));
                        }
                        sql.append(stringifyCriteria(criteriaClone)).append(" ");
                    }

                } catch (CloneNotSupportedException ex) {
                    throw new Exception(ex.getMessage());
                }
            }
            return sql;
        }

    }

    protected StringBuilder createGroupByClause(List<String> columns) throws IllegalArgumentException, EmptyCollectionException, BadSqlException {
        if (columns == null) throw new IllegalArgumentException("The columns parameter is null");

        boolean columnsAreClean = true;
        for (String column : columns) {
            columnsAreClean = sqlIsClean(column);
            if (! columnsAreClean) throw new BadSqlException(column + " failed to be clean SQL");
        }

        if (columns.size() == 0) {
            throw new EmptyCollectionException("The columns parameter is empty");
        } else {
            StringBuilder sql = new StringBuilder(" GROUP BY ");

            for (String column : columns) {
                sql.append(String.format("%s%s%s, ", beginningDelimiter, escape(column), endingDelimter));
            }

            sql.delete(sql.length() - 2, sql.length()).append(" ");
            return sql;
        }
    }

    protected StringBuilder createOrderByClause(List<String> columns, boolean ascending) throws IllegalArgumentException, EmptyCollectionException, BadSqlException {
        if (columns == null) throw new IllegalArgumentException("The columns parameter is null");

        boolean columnsAreClean = true;
        for (String column : columns) {
            columnsAreClean = sqlIsClean(column);
            if (! columnsAreClean) throw new BadSqlException(column + " failed to be clean SQL");
        }

        if (columns.size() == 0) {
            throw new EmptyCollectionException("The columns paramter is empty");
        } else {
            StringBuilder sql = new StringBuilder(" ORDER BY ");

            for (String column : columns) {
                sql.append(String.format("%s%s%s, ", beginningDelimiter, escape(column), endingDelimter));
            }

            sql.delete(sql.length() - 2, sql.length()).append(" ");
            //return (ascending) ? sql.append(" ASC ").Replace("  ", " ") : sql.Append(" DESC ").Replace("  ", " ");
            return (ascending) ? sql.append(" ASC ") : sql.append(" DESC ");
        }
    }

    protected StringBuilder createLimitClause(Long limit) throws IllegalArgumentException, BadSqlException {
        if (! sqlIsClean(limit.toString())) throw new BadSqlException(limit.toString() + " failed to be clean SQL");

        if (limit == null) {
            throw new IllegalArgumentException("The limit parameter is null");
        } else {
            return new StringBuilder(String.format(" LIMIT %s ", limit));
        }
    }

    protected StringBuilder createOffsetClause(Long offset) throws IllegalArgumentException, BadSqlException {
        if (! sqlIsClean(offset.toString())) throw new BadSqlException(offset.toString() + " failed to be clean SQL");

        if (offset == null) {
            throw new IllegalArgumentException("The offset parameter was null");
        } else {
            return new StringBuilder(String.format(" OFFSET %s ", offset));
        }
    }

    protected StringBuilder createSuppressNullsClause(List<String> columns)
            throws IllegalArgumentException, EmptyCollectionException, BadSqlException {
        if (columns == null) throw new IllegalArgumentException("The columns parameter is null");

        boolean columnsAreClean = true;
        for (String column : columns) {
            columnsAreClean = sqlIsClean(column);
            if (! columnsAreClean) throw new BadSqlException(column + " failed to be clean SQL");
        }

        if (columns.size() == 0) {
            throw new EmptyCollectionException("The columns paramter is empty");
        } else {
            StringBuilder sql = new StringBuilder();

            for (int i = 0; i < columns.size(); i++) {
                if (i == 0) {
                    sql.append(String.format(" (%s%s%s IS NOT NULL ", beginningDelimiter, columns.get(i), endingDelimter));
                } else {
                    sql.append(String.format(" OR %s%s%s IS NOT NULL ", beginningDelimiter, columns.get(i), endingDelimter));
                }
            }

            return sql.append(") ");
        }
    }

    private int getColumnDataType(String table, String columnName) throws ColumnNameNotFoundException {
        Integer dataType = tableSchemas.get(table).get(columnName);

        if (dataType == null) {
            throw new ColumnNameNotFoundException(String.format("Could not find column, %s", columnName));
        } else {
            return dataType;
        }
    }

    private boolean isColumnQuoted(int columnDataType) throws DataTypeNotFoundException {
        Boolean isQuoted = typeMappings.get(columnDataType);
        if (isQuoted == null) {
            throw new DataTypeNotFoundException(String.format("Data type, %s, is not recognized", columnDataType));
        } else {
            return isQuoted;
        }
    }

    private void wrapFilterInQuotes(Criteria criteria) {
        String[] originalFilters = criteria.filter.split(",");
        String[] newFilters = new String[originalFilters.length];

        for (int i=0; i<originalFilters.length; i++) {
            newFilters[i] = String.format("'%s'", escape(originalFilters[i]));
        }

        criteria.filter = "(" + String.join(",", newFilters) + ")";
    }

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

    }

}
