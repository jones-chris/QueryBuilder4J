package com.querybuilder4j.sqlbuilders.statements;


import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SelectStatement {
    private String queryName;
    private Properties properties;
    private ResultSetMetaData tableSchema;
    private boolean distinct;
    private List<String> columns;
    private String table;
    private List<Criteria> criteria;
    private boolean groupBy;
    private boolean orderBy;
    private Long limit;
    private boolean ascending;
    private Long offset;
    private boolean suppressNulls;

    public SelectStatement() {}

    public SelectStatement(String queryName, Properties properties) {
        this.queryName = queryName;
        this.properties = properties;
        tableSchema = null;
        distinct = false;
        columns = new ArrayList<>();
        table = null;
        criteria = new ArrayList<>();
        groupBy = false;
        orderBy = false;
        limit = null;
        ascending = false;
        offset = null;
        suppressNulls = false;
    }

    public String getQueryName() {
        return queryName;
    }

    public SelectStatement setQueryName(String queryName) {
        this.queryName = queryName;
        return this;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public ResultSetMetaData getTableSchema() {
        return tableSchema;
    }

    public SelectStatement setTableSchema(ResultSetMetaData tableSchema) {
        this.tableSchema = tableSchema;
        return this;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public SelectStatement setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public List<String> getColumns() {
        return columns;
    }

    public SelectStatement setColumns(List<String> columns) {
        this.columns = columns;
        return this;
    }

    public String getTable() {
        return table;
    }

    public SelectStatement setTable(String table) {
        this.table = table;
        return this;
    }

    public List<Criteria> getCriteria() {
        return criteria;
    }

    public SelectStatement setCriteria(List<Criteria> criteria) {
        this.criteria = criteria;
        return this;
    }

    public boolean isGroupBy() {
        return groupBy;
    }

    public SelectStatement setGroupBy(boolean groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public boolean isOrderBy() {
        return orderBy;
    }

    public SelectStatement setOrderBy(boolean orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public long getLimit() {
        return limit;
    }

    public SelectStatement setLimit(long limit) {
        this.limit = limit;
        return this;
    }

    public boolean isAscending() {
        return ascending;
    }

    public SelectStatement setAscending(boolean ascending) {
        this.ascending = ascending;
        return this;
    }

    public long getOffset() {
        return offset;
    }

    public SelectStatement setOffset(long offset) {
        this.offset = offset;
        return this;
    }

    public boolean isSuppressNulls() {
        return suppressNulls;
    }

    public SelectStatement setSuppressNulls(boolean suppressNulls) {
        this.suppressNulls = suppressNulls;
        return this;
    }

    public boolean addCriteria(Criteria criteria) {
        try {
            this.criteria.add(criteria);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean addCriteria(List<Criteria> criteria) {
            return this.criteria.addAll(criteria);
    }

    public boolean removeCriteria(int index) {
        try {
            this.criteria.remove(index);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     *
     * @param column
     * @return List of Criteria.  The List will be empty if no matches were found.
     */
    public List<Criteria> findAllCriteriaByColumn(String column) {
        List<Criteria> matchingCriteria = new ArrayList<>();

        this.criteria.forEach(criteria -> {
            if (criteria.column.equals(column)) matchingCriteria.add(criteria);
        });

        return matchingCriteria;
    }

    public boolean criteriaExistsForColumn(String column) {
        for (Criteria criteria : this.criteria) {
            if (criteria.column.equals(column)) return true;
        }

        return false;
    }

    public boolean removeMatchingCriteria(String column) {
        return this.criteria.removeIf(criteria -> criteria.column.equals(column));
    }

    public boolean replaceAllMatchingCriteria(Criteria criteria) {
        try {
            this.criteria.removeIf(x -> x.column.equals(criteria.column));
            return addCriteria(criteria);
        } catch (Exception ex) {
            return false;
        }
    }

}
