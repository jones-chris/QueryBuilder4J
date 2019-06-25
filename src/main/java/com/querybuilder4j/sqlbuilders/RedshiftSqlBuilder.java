package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.statements.SelectStatement;

import java.util.Properties;

public class RedshiftSqlBuilder extends SqlBuilder {

    public RedshiftSqlBuilder(SelectStatement stmt, Properties properties) throws Exception {
        super(stmt, properties);
        beginningDelimiter = '"';
        endingDelimter = '"';
    }

    @Override
    public String buildSql() throws Exception {
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

        sql.append(createGroupByClause(stmt.getColumns()));
        sql.append(createOrderByClause(stmt.getColumns(), stmt.isAscending()));
        sql.append(createLimitClause(stmt.getLimit()));
        sql.append(createOffsetClause(stmt.getOffset()));

        return sql.toString();
    }

}
