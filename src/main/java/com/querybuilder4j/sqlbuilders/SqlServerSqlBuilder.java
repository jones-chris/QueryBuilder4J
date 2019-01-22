package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.sqlbuilders.dao.QueryTemplateDao;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;

import java.util.Map;
import java.util.Properties;

public class SqlServerSqlBuilder extends SqlBuilder {

    public SqlServerSqlBuilder(SelectStatement stmt, Properties properties) throws Exception {
        super(stmt, properties);
        beginningDelimiter = '[';
        endingDelimter = ']';
    }

    @Override
    public String buildSql() throws Exception {
        StringBuilder sql = new StringBuilder("");
        sql.append(createSelectClause(stmt.isDistinct(), stmt.getColumns()));
        sql.append(createFromClause(stmt.getTable()));
        sql.append(createWhereClause(stmt.getCriteria()));

        if (sql.toString().contains(" WHERE ")) {
            if (stmt.isSuppressNulls()) {
                sql.append(" AND " ).append(createSuppressNullsClause(stmt.getColumns()));
            }
        }
        else {
            if (stmt.isSuppressNulls()) {
                sql.append(" WHERE ").append(createSuppressNullsClause(stmt.getColumns()));
            }
        }

        sql.append(createGroupByClause(stmt.getColumns()));
        sql.append(createOrderByClause(stmt.getColumns(), stmt.isAscending()));
        sql.append(createOffsetClause(stmt.getOffset()));
        sql.append(createFetchClause(stmt.getLimit()));
        return sql.toString();
    }

    @Override
    protected StringBuilder createOffsetClause(Long offset) throws IllegalArgumentException {
        return (offset == null) ? null : new StringBuilder(" OFFSET ").append(offset).append(" ROWS ");
    }

    private StringBuilder createFetchClause(Long limit) {
        return (limit == null) ? null : new StringBuilder(" FETCH NEXT ").append(limit).append(" ROWS ONLY ");
    }
}
