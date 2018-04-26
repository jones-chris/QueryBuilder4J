package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.sqlbuilders.statements.DeleteStatement;
import com.querybuilder4j.sqlbuilders.statements.InsertStatement;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;
import com.querybuilder4j.sqlbuilders.statements.UpdateStatement;

public class RedshiftSqlBuilder extends AbstractSqlBuilder {

    public RedshiftSqlBuilder() {
        beginningDelimiter = '"';
        endingDelimter = '"';

        typeMappings.put("sql_bit", false);
        typeMappings.put("sql_bigint", false);
        typeMappings.put("sql_integer", false);
        typeMappings.put("sql_decimal", false);
        typeMappings.put("sql_double", false);
        typeMappings.put("sql_numeric", false);
        typeMappings.put("sql_real", false);
        typeMappings.put("sql_smallint", false);
        typeMappings.put("sql_char", true);
        typeMappings.put("sql_long_varchar", true);
        typeMappings.put("sql_wchar", true);
        typeMappings.put("sql_wlongvarchar", true);
        typeMappings.put("sql_wvarchar", true);
        typeMappings.put("sql_type_times", true);
        typeMappings.put("sql_type_timestamp", true);
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
    public String buildSql(UpdateStatement query) throws Exception {
        return null;
    }

    @Override
    public String buildSql(DeleteStatement query) throws Exception {
        return null;
    }
}
