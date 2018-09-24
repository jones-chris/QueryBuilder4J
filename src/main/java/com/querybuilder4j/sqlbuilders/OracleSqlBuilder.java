package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.sqlbuilders.statements.SelectStatement;

public class OracleSqlBuilder extends SqlBuilder {

    public OracleSqlBuilder() {
        beginningDelimiter = '"';
        endingDelimter = '"';
    }

    @Override
    public String buildSql(SelectStatement query) throws Exception {
//        tableSchema = query.getTableSchema();

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
