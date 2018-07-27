package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.sqlbuilders.statements.SelectStatement;

public class SqliteSqlBuilder extends SqlBuilder {

    public SqliteSqlBuilder() {
        beginningDelimiter = '"';
        endingDelimter = '"';
    }

    @Override
    public String buildSql(SelectStatement query) throws Exception {
        tableSchema = query.getTableSchema();

        StringBuilder sql = new StringBuilder("");

        StringBuilder select = createSelectClause(query.isDistinct(), query.getColumns());
        if (select != null)
            sql.append(select);

        StringBuilder from = createFromClause(query.getTable());
        if (from != null)
            sql.append(from);

        StringBuilder where = createWhereClause(query.getCriteria());
        if (where != null)
            sql.append(where);


        if (query.isSuppressNulls()) {
            if (sql.toString().contains(" WHERE ")) {
                sql.append(" AND ").append(createSuppressNullsClause(query.getColumns()));
            } else {
                sql.append(" WHERE ").append(createSuppressNullsClause(query.getColumns()));
            }
        }

        if (query.isGroupBy()) sql.append(createGroupByClause(query.getColumns()));

        if (query.isOrderBy()) sql.append(createOrderByClause(query.getColumns(), query.isAscending()));

        sql.append(createLimitClause(query.getLimit()));
        sql.append(createOffsetClause(query.getOffset()));

        return sql.toString();
    }

}
