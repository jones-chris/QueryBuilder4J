package com.querybuilder4j.sqlbuilders;


import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.config.Operator;
import com.querybuilder4j.exceptions.BadSqlException;
import com.querybuilder4j.exceptions.ColumnNameNotFoundException;
import com.querybuilder4j.exceptions.DataTypeNotFoundException;
import com.querybuilder4j.exceptions.EmptyCollectionException;
import com.querybuilder4j.sqlbuilders.statements.*;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import static com.querybuilder4j.sqlbuilders.SqlCleanser.escapeAndRemove;

public abstract class AbstractSqlBuilder {
    protected Map<String, Boolean> typeMappings = new HashMap<>();
    protected char beginningDelimiter;
    protected char endingDelimter;
    protected ResultSetMetaData tableSchema;
    public static Map<DatabaseType, String> availableTables = new HashMap<>();

    static {
        availableTables.put(DatabaseType.MySql,      "SELECT table_name FROM information_schema.table_privileges WHERE grantee = '{0}' AND privilege_type = 'SELECT';");
        availableTables.put(DatabaseType.Oracle,     "SELECT table_name FROM dba_tab_privs WHERE grantee ='{0}' AND privilege = 'SELECT';");
        availableTables.put(DatabaseType.PostgreSQL, "SELECT table_name FROM information_schema.table_privileges WHERE grantee = '{0}' AND privilege_type = 'SELECT';");
        availableTables.put(DatabaseType.Redshift,   "SELECT table_name FROM information_schema.table_privileges WHERE grantee = '{0}' AND privilege_type = 'SELECT';");
        availableTables.put(DatabaseType.Sqlite,     "SELECT tbl_name FROM sqlite_master where type ='table' OR type ='view';");
        availableTables.put(DatabaseType.SqlServer,  "SELECT table_name FROM sp_table_privileges WHERE grantee = '{0}' AND privilege = 'SELECT';");
    }


    public AbstractSqlBuilder() {}

    public abstract String buildSql(SelectStatement query) throws Exception;
    public abstract String buildSql(InsertStatement query) throws Exception;
    public abstract String buildSql(UpdateStatement query) throws Exception;
    public abstract String buildSql(DeleteStatement query) throws Exception;

    protected StringBuilder createSelectClause(boolean distinct, List<String> columns)
            throws IllegalArgumentException, EmptyCollectionException {
        if (columns == null) throw new IllegalArgumentException("Columns parameter is null");

        if (columns.size() == 0) throw new EmptyCollectionException("Columns parameter is empty");

        String startSql = (distinct) ? "SELECT DISTINCT " : "SELECT ";
        StringBuilder sql = new StringBuilder(startSql);
        for (String column : columns)
        {
            sql.append(String.format("%s%s%s, ", beginningDelimiter, escapeAndRemove(column), beginningDelimiter));
        }
        sql = sql.delete(sql.length() - 2, 2).append(" ");
        //return sql.replace("  ", " ");
        return sql;
    }

    protected StringBuilder createFromClause(String table) throws IllegalArgumentException {
        if (table == null) throw new IllegalArgumentException("table parameter is null");

        if (table.equals("")) throw new IllegalArgumentException("The table argument is an empty string");

        String s = String.format(" FROM %s%s%s ", beginningDelimiter, escapeAndRemove(table), endingDelimter);
        StringBuilder sql = new StringBuilder(s);
        //return sql.Replace("  ", " ");
        return sql;
    }

    protected StringBuilder createWhereClause(SortedSet<Criteria> criteria) throws Exception {
        if (criteria == null) throw new IllegalArgumentException("The criteria parameter is null");

        if (criteria.size() == 0) {
            throw new EmptyCollectionException("The criteria parameter is empty");
        } else {
            StringBuilder sql = new StringBuilder(" WHERE ");

            for (Criteria crit : criteria) {
                // clone criteria
                try {
                    Criteria criteriaClone = (Criteria) crit.clone();

                    if (criteriaClone.getId() == 0) criteriaClone.conjunction = null;

                    if (criteriaClone.isValid()) {
                        if (criteriaClone.operator.equals(Operator.isNull) || criteriaClone.operator.equals(Operator.isNotNull)) {
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
                                criteriaClone.filter = "(" + escapeAndRemove(criteriaClone.filter) + ")";
                            }

                            sql.append(criteriaClone.toString()).append(" ");
                        } else {
                            criteriaClone.filter = (shouldHaveQuotes) ? "'" + escapeAndRemove(criteriaClone.filter) + "'" : escapeAndRemove(criteriaClone.filter);
                            sql.append(criteria.toString()).append(" ");
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

    protected StringBuilder createGroupByClause(List<String> columns) throws IllegalArgumentException, EmptyCollectionException{
        if (columns == null) throw new IllegalArgumentException("The columns parameter is null");

        if (columns.size() == 0) {
            throw new EmptyCollectionException("The columns parameter is empty");
        } else {
            StringBuilder sql = new StringBuilder(" GROUP BY ");

            for (String column : columns) {
                sql.append(String.format("%s%s%s, ", beginningDelimiter, escapeAndRemove(column), endingDelimter));
            }

            sql.delete(sql.length() - 2, 2).append(" ");
            //return sql.Replace("  ", " ");
            return sql;
        }
    }

    protected StringBuilder createOrderByClause(List<String> columns, boolean ascending)
            throws IllegalArgumentException, EmptyCollectionException{
        if (columns == null) throw new IllegalArgumentException("The columns parameter is null");

        if (columns.size() == 0) {
            throw new EmptyCollectionException("The columns paramter is empty");
        } else {
            StringBuilder sql = new StringBuilder(" ORDER BY ");

            for (String column : columns) {
                sql.append(String.format("%s%s%s, ", beginningDelimiter, escapeAndRemove(column), endingDelimter));
            }

            sql.delete(sql.length() - 2, 2).append(" ");
            //return (ascending) ? sql.append(" ASC ").Replace("  ", " ") : sql.Append(" DESC ").Replace("  ", " ");
            return (ascending) ? sql.append(" ASC ") : sql.append(" DESC ");
        }
    }

    protected StringBuilder createLimitClause(Long limit) throws IllegalArgumentException {
        if (limit == null) {
            throw new IllegalArgumentException("The limit parameter is null");
        } else {
            return new StringBuilder(String.format(" LIMIT %s ", limit));
        }
    }

    protected StringBuilder createOffsetClause(Long offset) throws IllegalArgumentException {
        if (offset == null) {
            throw new IllegalArgumentException("The offset parameter was null");
        } else {
            return new StringBuilder(String.format(" OFFSET %s ", offset));
        }
    }

    protected StringBuilder createSuppressNullsClause(List<String> columns)
            throws IllegalArgumentException, EmptyCollectionException{
        if (columns == null) throw new IllegalArgumentException("The columns parameter is null");

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

    protected StringBuilder createInsertTableClause(String table) {
        String tableWithDelims = String.format("%s%s%s", beginningDelimiter, table, endingDelimter);
        return new StringBuilder("INSERT INTO ").append(tableWithDelims).append(" ");
    }

    protected StringBuilder createInsertColumnsClause(List<String> columns) throws SQLException, ColumnNameNotFoundException, DataTypeNotFoundException {
        StringBuilder s = new StringBuilder(" (");

        columns.forEach(column -> s.append(beginningDelimiter).append(columns).append(endingDelimter).append(','));

        return s.append(") ");
    }

    protected StringBuilder createInsertValuesClause(List<String> values) throws SQLException, ColumnNameNotFoundException, DataTypeNotFoundException {
        StringBuilder s = new StringBuilder("VALUES (");

        for (String value : values) {

            String dataType = getColumnDataType(value);

            if (isColumnQuoted(dataType)) {
                s.append('\'').append(value).append('\'').append(',');
            } else {
                s.append(value).append(',');
            }
        }

        return s.append(") ");

    }

    protected StringBuilder createUpdateTableClause(String table) {
        String tableWithDelims = String.format("%s%s%s", beginningDelimiter, table, endingDelimter);
        return new StringBuilder("UPDATE ").append(tableWithDelims).append(" ");
    }

    protected StringBuilder createUpdateSetClause(List<String> columns, List<String> values) throws SQLException, ColumnNameNotFoundException, DataTypeNotFoundException {
        // check that both lists have same number of elements
        if (columns.size() != values.size()) throw new RuntimeException("The Columns and Values collections have different sizes.");

        // loop thru each item (using index)
        StringBuilder s = new StringBuilder(" SET ");
        for (int i=0; i<columns.size(); i++) {
            String column = columns.get(i);
            String value = values.get(i);
            s.append(beginningDelimiter).append(column).append(endingDelimter).append(" = ");

            String dataType = getColumnDataType(value);

            if (isColumnQuoted(dataType)) {
                s.append('\'').append(value).append('\'').append(',');
            } else {
                s.append(value).append(',');
            }
        }

        // remove trialing comma
        s.deleteCharAt(s.length() - 1);

        // return stringbuilder
        return s;
    }

    protected StringBuilder createDeleteTableClause(String table) {
        String tableWithDelims = String.format("%s%s%s", beginningDelimiter, table, endingDelimter);
        return new StringBuilder("DELETE FROM ").append(tableWithDelims).append(" ");
    }

    private String getColumnDataType(String columnName) throws SQLException, ColumnNameNotFoundException {
        for (int i=0; i<tableSchema.getColumnCount(); i++) {
            if (tableSchema.getColumnName(i).equals(columnName)) {
                return tableSchema.getColumnTypeName(i);
            }
        }

        throw new ColumnNameNotFoundException(String.format("Could not find column, %s", columnName));
    }

    private boolean isColumnQuoted(String columnDataType) throws DataTypeNotFoundException {
        Boolean isQuoted = typeMappings.get(columnDataType.toLowerCase());
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
            newFilters[i] = String.format("'%s'", escapeAndRemove(originalFilters[i]));
        }

        criteria.filter = "(" + String.join("", newFilters) + ")";
    }

}
