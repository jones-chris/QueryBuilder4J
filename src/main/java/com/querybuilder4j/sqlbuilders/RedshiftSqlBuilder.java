package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.sqlbuilders.dao.QueryTemplateDao;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;

import java.util.Map;
import java.util.Properties;

public class RedshiftSqlBuilder extends SqlBuilder {

    public RedshiftSqlBuilder(SelectStatement stmt, Map<String, String> subQueries,
                              Properties properties, QueryTemplateDao queryTemplateDao) throws Exception {
        super(stmt, subQueries, properties, queryTemplateDao);
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
