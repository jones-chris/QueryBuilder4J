package com.querybuilder4j.sqlbuilders;


import com.querybuilder4j.config.Operator;
import com.querybuilder4j.exceptions.BadSqlException;
import com.querybuilder4j.exceptions.ColumnNameNotFoundException;
import com.querybuilder4j.exceptions.DataTypeNotFoundException;
import com.querybuilder4j.exceptions.EmptyCollectionException;
import com.querybuilder4j.sqlbuilders.statements.*;

import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.querybuilder4j.sqlbuilders.SqlCleanser.escape;
import static com.querybuilder4j.sqlbuilders.SqlCleanser.sqlIsClean;

public abstract class SqlBuilder {
    protected static Map<Integer, Boolean> typeMappings = new HashMap<>();
    protected char beginningDelimiter;
    protected char endingDelimter;
    protected Map<String, Integer> tableSchema;

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


    public SqlBuilder() {}

    public abstract String buildSql(SelectStatement query) throws Exception;

    protected StringBuilder createSelectClause(boolean distinct, List<String> columns)
            throws IllegalArgumentException, EmptyCollectionException, BadSqlException {
        if (columns == null) throw new IllegalArgumentException("Columns parameter is null");

        if (columns.size() == 0) throw new EmptyCollectionException("Columns parameter is empty");

        boolean columnsAreClean = true;
        for (String column : columns) {
            columnsAreClean = sqlIsClean(column);
            if (! columnsAreClean) throw new BadSqlException(column + " failed to be clean SQL");
        }

        String startSql = (distinct) ? "SELECT DISTINCT " : "SELECT ";
        StringBuilder sql = new StringBuilder(startSql);
        for (String column : columns) {
            sql.append(String.format("%s%s%s, ", beginningDelimiter, escape(column), beginningDelimiter));
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

    protected StringBuilder createWhereClause(List<Criteria> criteria) throws Exception {
        if (criteria == null) throw new IllegalArgumentException("The criteria parameter is null");

        boolean criteriaAreClean = true;
        for (Criteria crit : criteria) {
            criteriaAreClean = sqlIsClean(crit);
            if (! criteriaAreClean) throw new BadSqlException(crit + " failed to be clean SQL");
        }

        if (criteria.size() == 0) {
            return null;
        } else {
            StringBuilder sql = new StringBuilder(" WHERE ");

            for (Criteria crit : criteria) {
                // clone criteria
                try {
                    Criteria criteriaClone = (Criteria) crit.clone();

                    if (criteriaClone.getId() == 0) criteriaClone.conjunction = null;

                    if (criteriaClone.isValid()) {
                        if (criteriaClone.operator.equals(Operator.isNull) || criteriaClone.operator.equals(Operator.isNotNull)) {
                            criteriaClone.filter = null;
                            sql.append(String.format(" %s ", criteriaClone.toString()));
                            continue;
                        }

                        if (criteriaClone.filter == null || criteriaClone.filter.equals("")) {
                            throw new BadSqlException(String.format("The criteria in position %d has a null or empty filter, but the operator is not \"IsNull\" or \"IsNotNull\""));
                        }

//                        if (criteriaClone.isFilterSubquery()) {
//                            criteriaClone.filter = escapeAndRemove(criteriaClone.filter);
//                            sql.append(String.format(" %s ", criteriaClone.toString()));
//                            continue;
//                        }

                        boolean shouldHaveQuotes = isColumnQuoted(getColumnDataType(criteriaClone.column));
                        if (criteriaClone.operator.equals(Operator.in) || criteriaClone.operator.equals(Operator.notIn)) {
                            if (shouldHaveQuotes) {
                                wrapFilterInQuotes(criteriaClone);
                            } else {
                                criteriaClone.filter = "(" + escape(criteriaClone.filter) + ")";
                            }

                            sql.append(criteriaClone.toString()).append(" ");
                        } else {
                            criteriaClone.filter = (shouldHaveQuotes) ? "'" + escape(criteriaClone.filter) + "'" : escape(criteriaClone.filter);
                            sql.append(criteriaClone.toString()).append(" ");
                        }

                    } else {
                        throw new BadSqlException(String.format("The criteria in position %d is not valid"));
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

    private int getColumnDataType(String columnName) throws SQLException, ColumnNameNotFoundException {
        Integer dataType = tableSchema.get(columnName);

        if (dataType == null) {
            throw new ColumnNameNotFoundException(String.format("Could not find column, %s", columnName));
        } else {
            return dataType;
        }
    }

    private boolean isColumnQuoted(int columnDataType) throws DataTypeNotFoundException {
        Boolean isQuoted = typeMappings.get(columnDataType);
        if (isQuoted == null) {
            throw new DataTypeNotFoundException(String.format("Data type, %s, not recognized", columnDataType));
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

        criteria.filter = "(" + String.join("", newFilters) + ")";
    }

}
