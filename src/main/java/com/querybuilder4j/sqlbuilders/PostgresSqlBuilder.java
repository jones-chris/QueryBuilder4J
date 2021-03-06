package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.statements.SelectStatement;

public class PostgresSqlBuilder extends SqlBuilder {

    public PostgresSqlBuilder(SelectStatement stmt) throws Exception {
        super(stmt);
        beginningDelimiter = '"';
        endingDelimter = '"';
    }

    @Override
    public String buildSql() throws Exception {
        StringBuilder sql = new StringBuilder();

        // Select
        StringBuilder select = createSelectClause(stmt.isDistinct(), stmt.getColumns());
        if (select != null)
            sql.append(select);

        // From
        StringBuilder from = createFromClause(stmt.getTable());
        if (from != null)
            sql.append(from);

        // Joins
        StringBuilder joins = createJoinClause(stmt.getJoins());
        if (joins != null)
            sql.append(joins);

        // Where
        StringBuilder where = createWhereClause(stmt.getCriteria());
        if (where != null)
            sql.append(where);

        // Suppress Nulls (part of Where clause)
        if (stmt.isSuppressNulls()) {
            if (sql.toString().contains(" WHERE ")) {
                sql.append(" AND ").append(createSuppressNullsClause(stmt.getColumns()));
            } else {
                sql.append(" WHERE ").append(createSuppressNullsClause(stmt.getColumns()));
            }
        }

        // Group By
        if (stmt.isGroupBy()) sql.append(createGroupByClause(stmt.getColumns()));

        // Order By
        if (stmt.isOrderBy()) sql.append(createOrderByClause(stmt.getColumns(), stmt.isAscending()));

        // Limit
        sql.append(createLimitClause(stmt.getLimit()));

        // Offset
        sql.append(createOffsetClause(stmt.getOffset()));

        return sql.toString();
    }

}
