package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.sqlbuilders.statements.SelectStatement;

public class SqliteSqlBuilder extends SqlBuilder {

    public SqliteSqlBuilder() {
        beginningDelimiter = '"';
        endingDelimter = '"';

//        typeMappings.put("boolean", false);
//        typeMappings.put("smallint", false);
//        typeMappings.put("int16", false);
//        typeMappings.put("int", false);
//        typeMappings.put("int32", false);
//        typeMappings.put("integer", false);
//        typeMappings.put("int64", false);
//        typeMappings.put("real", false);
//        typeMappings.put("numeric", false);
//        typeMappings.put("decimal", false);
//        typeMappings.put("money", false);
//        typeMappings.put("currency", false);
//        typeMappings.put("date", true);
//        typeMappings.put("time", true);
//        typeMappings.put("datetime", true);
//        typeMappings.put("smalldate", true);
//        typeMappings.put("datetimeoffset", true);
//        typeMappings.put("text", true);
//        typeMappings.put("ntext", true);
//        typeMappings.put("char", true);
//        typeMappings.put("nchar", true);
//        typeMappings.put("varchar", true);
//        typeMappings.put("nvarchar", true);
//        typeMappings.put("string", true);
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

}
