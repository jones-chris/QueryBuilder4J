package com.querybuilder4j.sqlbuilders.statements;

import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.sqlbuilders.AbstractSqlBuilder;

import java.util.List;

import static com.querybuilder4j.config.SqlBuilderFactory.buildSqlBuilder;

public class UpdateStatement extends Statement {
    private String table;
    private List<String> columns;
    private List<String> values;

    public UpdateStatement() {}

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

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
