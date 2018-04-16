package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.sqlbuilders.statements.DeleteStatement;
import com.querybuilder4j.sqlbuilders.statements.InsertStatement;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;
import com.querybuilder4j.sqlbuilders.statements.UpdateStatement;

public class MySqlSqlBuilder extends AbstractSqlBuilder {

    public MySqlSqlBuilder() {
        beginningDelimiter = '`';
        endingDelimter = '`';

        typeMappings.put("bool", false);
        typeMappings.put("boolean", false);
        typeMappings.put("tinyint", false);
        typeMappings.put("tinyint unsigned", false);
        typeMappings.put("smallint", false);
        typeMappings.put("year", false);
        typeMappings.put("int", false);
        typeMappings.put("smallint unsigned", false);
        typeMappings.put("mediumint", false);
        typeMappings.put("bigint", false);
        typeMappings.put("int unsigned", false);
        typeMappings.put("integer unsigned", false);
        typeMappings.put("float", false);
        typeMappings.put("double", false);
        typeMappings.put("real", false);
        typeMappings.put("decimal", false);
        typeMappings.put("numeric", false);
        typeMappings.put("dec", false);
        typeMappings.put("fixed", false);
        typeMappings.put("bigint unsigned", false);
        typeMappings.put("float unsigned", false);
        typeMappings.put("double unsigned", false);
        typeMappings.put("serial", false);
        typeMappings.put("date", true);
        typeMappings.put("timestamp", true);
        typeMappings.put("datetime", true);
        typeMappings.put("datetimeoffset", true);
        typeMappings.put("time", true);
        typeMappings.put("char", true);
        typeMappings.put("varchar", true);
        typeMappings.put("tinytext", true);
        typeMappings.put("text", true);
        typeMappings.put("mediumtext", true);
        typeMappings.put("longtext", true);
        typeMappings.put("set", true);
        typeMappings.put("enum", true);
        typeMappings.put("nchar", true);
        typeMappings.put("national char", true);
        typeMappings.put("nvarchar", true);
        typeMappings.put("national varchar", true);
        typeMappings.put("character varying", true);
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

            sql.append(createGroupByClause(query.getColumns()));
            sql.append(createOrderByClause(query.getColumns(), query.isAscending()));
            sql.append(createLimitClause(query.getLimit()));
            sql.append(createOffsetClause(query.getOffset()));

            return sql.toString();

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public String buildSql(InsertStatement query) {
        return null;
    }

    @Override
    public String buildSql(UpdateStatement query) {
        return null;
    }

    @Override
    public String buildSql(DeleteStatement query) {
        return null;
    }
}
