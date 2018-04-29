package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.sqlbuilders.statements.SelectStatement;

public class SqlServerSqlBuilder extends SqlBuilder {

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
        try {
            StringBuilder sql = new StringBuilder("");
            sql.append(createSelectClause(query.isDistinct(), query.getColumns()));
            sql.append(createFromClause(query.getTable()));
            sql.append(createWhereClause(query.getCriteria()));

            if (sql.toString().contains(" WHERE ")) {
                if (query.isSuppressNulls()) {
                    sql.append(" AND " ).append(createSuppressNullsClause(query.getColumns()));
                }
            }
            else {
                if (query.isSuppressNulls()) {
                    sql.append(" WHERE ").append(createSuppressNullsClause(query.getColumns()));
                }
            }

            sql.append(createGroupByClause(query.getColumns()));
            sql.append(createOrderByClause(query.getColumns(), query.isAscending()));
            sql.append(createOffsetClause(query.getOffset()));
            sql.append(createFetchClause(query.getLimit()));
            return sql.toString();
        }
        catch (Exception e) {
            throw e;
        }
    }

    @Override
    protected StringBuilder createOffsetClause(Long offset) throws IllegalArgumentException {
        return (offset == null) ? null : new StringBuilder(" OFFSET ").append(offset).append(" ROWS ");
    }

    private StringBuilder createFetchClause(Long limit) {
        return (limit == null) ? null : new StringBuilder(" FETCH NEXT ").append(limit).append(" ROWS ONLY ");
    }
}
