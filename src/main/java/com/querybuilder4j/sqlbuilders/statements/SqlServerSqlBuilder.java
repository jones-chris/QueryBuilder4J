package com.querybuilder4j.sqlbuilders.statements;

import com.querybuilder4j.sqlbuilders.AbstractSqlBuilder;

public class SqlServerSqlBuilder extends AbstractSqlBuilder {

    public SqlServerSqlBuilder() {
        beginningDelimiter = '[';
        endingDelimter = ']';

        typeMappings.put("bit", false);
        typeMappings.put("bigint", false);
        typeMappings.put("decimal", false);
        typeMappings.put("float", false);
        typeMappings.put("int", false);
        typeMappings.put("money", false);
        typeMappings.put("real", false);
        typeMappings.put("smallint", false);
        typeMappings.put("smallmoney", false);
        typeMappings.put("numeric", false);
        typeMappings.put("char", true);
        typeMappings.put("text", true);
        typeMappings.put("varchar", true);
        typeMappings.put("ntext", true);
        typeMappings.put("nchar", true);
        typeMappings.put("nvarchar", true);
        typeMappings.put("VARCHAR2", true);
        typeMappings.put("date", true);
        typeMappings.put("datetime", true);
        typeMappings.put("datetime2", true);
        typeMappings.put("datetimeoffset", true);
        typeMappings.put("smalldatetime", true);
        typeMappings.put("time", true);
        typeMappings.put("timestamp", true);
    }

    @Override
    public String buildSql(SelectStatement query) throws Exception {
        query.setOrderBy(true); // must set Order By to true so that OFFSET and FETCH can be used.

        tableSchema = query.getTableSchema();

        try {
            StringBuilder sql = new StringBuilder("");
            sql.append(createSelectClause(query.isDistinct(), query.getColumns()));
            sql.append(createFromClause(query.getTable()));
            sql.append(createWhereClause(query.getCriteria()));

            if (query.isSuppressNulls()) {
                if (sql.toString().contains(" WHERE ")) {
                    sql.append(" AND ").append(createSuppressNullsClause(query.getColumns()));
                } else {
                    sql.append(" WHERE ").append(createSuppressNullsClause(query.getColumns()));
                }
            }

            sql.append(createGroupByClause(query.getColumns()));
            sql.append(createOrderByClause(query.getColumns(), query.isAscending()));
            sql.append(createOffsetClause(query.getOffset()));
            sql.append(createFetchClause(query.getLimit()));

            return sql.toString();

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public String buildSql(InsertStatement query) {
        return null;
    }

    @Override
    public String buildSql(UpdateStatement query) {
        return null;
    }

    @Override
    public String buildSql(DeleteStatement query) {
        return null;
    }

    @Override
    protected StringBuilder createOffsetClause(Long offset) throws IllegalArgumentException {
        if (offset == null) return null;
        return new StringBuilder(String.format(" OFFSET %s ROWS ", offset));
    }

    private StringBuilder createFetchClause(Long limit) {
        if (limit == null) return null;
        return new StringBuilder(String.format(" FETCH NEXT %s ROWS ONLY ", limit));
    }
}
