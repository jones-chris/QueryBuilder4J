package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.statements.SelectStatement;

import java.util.Properties;

public class OracleSqlBuilder extends SqlBuilder {

    public OracleSqlBuilder(SelectStatement stmt, Properties properties) throws Exception {
        super(stmt, properties);
        beginningDelimiter = '"';
        endingDelimter = '"';
    }

    @Override
    public String buildSql() throws Exception {

        try {
            StringBuilder sql = new StringBuilder("");
            sql.append(createSelectClause(stmt.isDistinct(), stmt.getColumns()));
            sql.append(createFromClause(stmt.getTable()));
            sql.append(createWhereClause(stmt.getCriteria()));

            if (stmt.isSuppressNulls()) {
                if (sql.toString().contains(" WHERE ")) {
                    sql.append(" AND ").append(createSuppressNullsClause(stmt.getColumns()));
                } else {
                    sql.append(" WHERE ").append(createSuppressNullsClause(stmt.getColumns()));
                }
            }

            if (sql.toString().contains(" WHERE ")) {
                sql.append(" AND ").append(createLimitClause(stmt.getLimit()));
            } else {
                sql.append(" WHERE ").append(createLimitClause(stmt.getLimit()));
            }

            sql.append(createGroupByClause(stmt.getColumns()));
            sql.append(createOrderByClause(stmt.getColumns(), stmt.isAscending()));
            sql.append(createOffsetClause(stmt.getOffset()));

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
