package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.sqlbuilders.statements.DeleteStatement;
import com.querybuilder4j.sqlbuilders.statements.InsertStatement;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;
import com.querybuilder4j.sqlbuilders.statements.UpdateStatement;

public class PostgresSqlBuilder extends AbstractSqlBuilder {

    public PostgresSqlBuilder() {
        beginningDelimiter = '"';
        endingDelimter = '"';

        typeMappings.put("bool", false);
        typeMappings.put("int2", false);
        typeMappings.put("int4", false);
        typeMappings.put("int8", false);
        typeMappings.put("float4", false);
        typeMappings.put("float8", false);
        typeMappings.put("numeric", false);
        typeMappings.put("money", false);
        typeMappings.put("text", true);
        typeMappings.put("varchar", true);
        typeMappings.put("bpchar", true);
        typeMappings.put("citext", true);
        typeMappings.put("json", true);
        typeMappings.put("jsonb", true);
        typeMappings.put("date", true);
        typeMappings.put("interval", true);
        typeMappings.put("timestamptz", true);
        typeMappings.put("time", true);
        typeMappings.put("timetz", true);
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
    public String buildSql(InsertStatement query) throws Exception {
        return null;
    }

    @Override
    public String buildSql(UpdateStatement query)throws Exception  {
        return null;
    }

    @Override
    public String buildSql(DeleteStatement query) throws Exception {
        return null;
    }
}
