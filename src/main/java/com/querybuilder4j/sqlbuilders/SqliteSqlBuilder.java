package com.querybuilder4j.sqlbuilders;


import com.querybuilder4j.sqlbuilders.statements.SelectStatement;

import java.util.Properties;

public class SqliteSqlBuilder extends SqlBuilder {

    public SqliteSqlBuilder(SelectStatement stmt, Properties properties) {
        super(stmt, properties);
        beginningDelimiter = '"';
        endingDelimter = '"';
    }

    @Override
    public String buildSql(SelectStatement query) throws Exception {

        tableSchemas = query.getTableSchemas();

        StringBuilder sql = new StringBuilder("");

        // Select
        StringBuilder select = createSelectClause(query.isDistinct(), query.getColumns());
        if (select != null)
            sql.append(select);

        // From
        StringBuilder from = createFromClause(query.getTable());
        if (from != null)
            sql.append(from);

        // Joins
        StringBuilder joins = createJoinClause(query.getJoins());
        if (joins != null)
            sql.append(joins);

        // Where
        StringBuilder where = createWhereClause(query.getCriteria());
        if (where != null)
            sql.append(where);

        // Suppress Null (part of Where clause)
        if (query.isSuppressNulls()) {
            if (sql.toString().contains(" WHERE ")) {
                sql.append(" AND ").append(createSuppressNullsClause(query.getColumns()));
            } else {
                sql.append(" WHERE ").append(createSuppressNullsClause(query.getColumns()));
            }
        }

        // Group By
        if (query.isGroupBy()) sql.append(createGroupByClause(query.getColumns()));

        // Order By
        if (query.isOrderBy()) sql.append(createOrderByClause(query.getColumns(), query.isAscending()));

        // Limit
        sql.append(createLimitClause(query.getLimit()));

        // Offset
        sql.append(createOffsetClause(query.getOffset()));

        return sql.toString();

    }

}
