package com.querybuilder4j.sqlbuilders.statements;

import java.sql.ResultSetMetaData;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class Statement {
    protected String queryName;
    protected Properties properties;
    protected ResultSetMetaData tableSchema;
    private SortedSet<Criteria> criteria = new TreeSet<>();

    public abstract String buildSql();

    public void addParenthesisToCriteria() {
        // add logic to add/remove parenthesis to criteria
    }

}
