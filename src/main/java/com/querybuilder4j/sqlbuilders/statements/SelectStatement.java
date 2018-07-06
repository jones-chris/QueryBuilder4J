package com.querybuilder4j.sqlbuilders.statements;


import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.config.Parenthesis;
import com.querybuilder4j.sqlbuilders.SqlBuilder;

import java.sql.ResultSet;
import java.util.*;

import static com.querybuilder4j.config.Parenthesis.EndParenthesis;
import static com.querybuilder4j.config.Parenthesis.FrontParenthesis;
import static com.querybuilder4j.config.SqlBuilderFactory.buildSqlBuilder;

public class SelectStatement {
    private String name = "";
    private DatabaseType databaseType;
    private ResultSet tableSchema;
    private List<String> columns = new ArrayList<>();
    private String table = "";
    private List<Criteria> criteria = new ArrayList<>();
    private boolean distinct;
    private boolean groupBy;
    private boolean orderBy;
    private Long limit = 10L;
    private boolean ascending;
    private Long offset = 0L;
    private boolean suppressNulls;


    public SelectStatement() {}

    public SelectStatement(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    public SelectStatement(DatabaseType databaseType, String name) {
        this.databaseType = databaseType;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    public ResultSet getTableSchema() {
        return tableSchema;
    }

    public void setTableSchema(ResultSet tableSchema) {
        this.tableSchema = tableSchema;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public List<Criteria> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<Criteria> criteria) {
        this.criteria = criteria;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public SelectStatement setDistinct(boolean distinct) {
        this.distinct = distinct;
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

    public boolean addCriteria(Criteria criteria) {
        boolean success = this.criteria.add(criteria);
        if (success) {
            Collections.sort(this.criteria);
            clearParenthesisFromCriteria();
            addParenthesisToCriteria();
            return true;
        }

        return false;

    }

    public boolean addCriteria(List<Criteria> criteria) {
        boolean success = this.criteria.addAll(criteria);
        if (success) {
            Collections.sort(this.criteria);
            clearParenthesisFromCriteria();
            addParenthesisToCriteria();
            return true;
        }

        return false;
    }

    public boolean removeCriteria(Criteria criteria) {
        boolean success = this.criteria.remove(criteria);
        if (success) {
            Collections.sort(this.criteria);
            clearParenthesisFromCriteria();
            addParenthesisToCriteria();
            return true;
        }

        return false;
    }

    private void clearParenthesisFromCriteria() {
        if (criteria.size() > 0) {
            criteria.forEach( (x) -> {
                x.frontParenthesis = Parenthesis.Empty;
                x.endParenthesis.clear();
            });
        }
    }

    private void addParenthesisToCriteria() {
        List<Criteria> criteriaList = new ArrayList<>(criteria);
        if (criteria.size() > 0) {
            for (int i=0; i<criteria.size(); i++) {

                Criteria theCriteria = criteriaList.get(i);

                if (isCriteriaAParent(theCriteria.getId())) {
                    theCriteria.frontParenthesis = FrontParenthesis;
                }
                // if this is the last index of the criteria with this parent id, then add end parenthesis.
                else if (getLastIndexOfCriteriaAsParent(theCriteria.parentId) == i) {
                    theCriteria.endParenthesis.add(EndParenthesis);
                }

            }

            // Determine if any remaining closing parenthesis are needed at end of criteria.  This only applies to criteria
            // sets that end on a child criteria.
            StringBuilder s = new StringBuilder();
            for (Criteria crit : criteria) {
                s.append(crit.toString());
            }

            char[] chars = s.toString().toCharArray();
            int numOfBegParen = 0;
            int numOfEndParen = 0;

            for (Character c : chars) {
                if (c.equals('(')) {
                    numOfBegParen++;
                } else if (c.equals(')')) {
                    numOfEndParen++;
                }
            }

            int parenDiff = numOfBegParen - numOfEndParen;
            if (parenDiff > 0) {
                for (int i=0; i<parenDiff; i++) {
                    criteria.get(criteria.size() - 1).endParenthesis.add(EndParenthesis);
                    //criteria.last().endParenthesis.add(EndParenthesis);
                }
            }
        }

    }

    private int getLastIndexOfCriteriaAsParent(Integer parentId) {
        if (parentId == null) return -1;
        List<Criteria> criteriaList = new ArrayList<>(criteria);
        for (int i=criteriaList.size()-1; i>=0; i--) {
            if (criteriaList.get(i).parentId == null) continue;
            if (criteriaList.get(i).parentId.equals(parentId)) return i;
        }
        return -1;
    }

    private boolean isCriteriaAParent(int id) {
        List<Criteria> criteriaList = new ArrayList<>(criteria);
        for (Criteria crit : criteriaList) {
            if (crit.parentId == null) continue;
            if (crit.parentId == id) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        try {
            Collections.sort(this.criteria);
            clearParenthesisFromCriteria();
            addParenthesisToCriteria();
            SqlBuilder sqlBuilder = buildSqlBuilder(databaseType);
            return sqlBuilder.buildSql(this);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
