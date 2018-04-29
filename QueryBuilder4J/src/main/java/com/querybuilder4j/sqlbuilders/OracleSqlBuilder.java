package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.sqlbuilders.statements.SelectStatement;

public class OracleSqlBuilder extends SqlBuilder {

    public OracleSqlBuilder() {
        beginningDelimiter = '"';
        endingDelimter = '"';

        typeMappings.put("integer", false);
        typeMappings.put("float", false);
        typeMappings.put("unsigned integer", false);
        typeMappings.put("number", false);
        typeMappings.put("char", true);
        typeMappings.put("long", false);
        typeMappings.put("nchar", true);
        typeMappings.put("nvarchar2", true);
        typeMappings.put("varchar2", true);
        typeMappings.put("date", true);
        typeMappings.put("timestamp", true);
        typeMappings.put("timestamp with local time zone", true);
        typeMappings.put("timestamp with time zone", true);
        typeMappings.put("interval year to month", true);
        typeMappings.put("interval day to second", true);
    }

    @Override
    public String buildSql(SelectStatement query) throws Exception {
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

            if (sql.toString().contains(" WHERE ")) {
                sql.append(" AND ").append(createLimitClause(query.getLimit()));
            } else {
                sql.append(" WHERE ").append(createLimitClause(query.getLimit()));
            }

            sql.append(createGroupByClause(query.getColumns()));
            sql.append(createOrderByClause(query.getColumns(), query.isAscending()));
            sql.append(createOffsetClause(query.getOffset()));

            return sql.toString();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    protected StringBuilder createLimitClause(Long limit) throws IllegalArgumentException {
        if (limit == null) return null;
        return new StringBuilder(" ROWNUM < ").append(limit);
    }

    @Override
    protected StringBuilder createOffsetClause(Long offset) throws IllegalArgumentException {
        if (offset == null) return null;
        return new StringBuilder(" OFFSET ").append(offset).append(" ROWS ");
    }
}
