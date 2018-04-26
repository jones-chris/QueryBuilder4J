package com.querybuilder4j.sqlbuilders.statements;

import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.sqlbuilders.AbstractSqlBuilder;

import static com.querybuilder4j.config.SqlBuilderFactory.buildSqlBuilder;

public class DeleteStatement extends Statement {
    private String table;

    @Override
    public String toSql() {
        try {
            String databaseTypeProp = properties.getProperty("databaseType");
            DatabaseType databaseType = Enum.valueOf(DatabaseType.class, databaseTypeProp) ;
            AbstractSqlBuilder sqlBuilder = buildSqlBuilder(databaseType);
            return sqlBuilder.buildSql(this);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
