package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.sqlbuilders.statements.SelectStatement;

import java.util.Properties;

public class SqlServerSqlBuilder extends SqlBuilder {

    public SqlServerSqlBuilder(SelectStatement stmt, Properties properties) {
        super(stmt, properties);
        beginningDelimiter = '[';
        endingDelimter = ']';
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
