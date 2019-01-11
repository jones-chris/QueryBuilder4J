package com.querybuilder4j.sqlbuilders.statements;

public class SubQuery {

    private final String sql;


    public SubQuery(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}
