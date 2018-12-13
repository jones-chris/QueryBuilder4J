package com.querybuilder4j.sqlbuilders.statements;


import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.config.Operator;
import com.querybuilder4j.config.Parenthesis;
import com.querybuilder4j.sqlbuilders.SqlBuilder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.*;

import static com.querybuilder4j.config.Parenthesis.EndParenthesis;
import static com.querybuilder4j.config.Parenthesis.FrontParenthesis;
import static com.querybuilder4j.config.SqlBuilderFactory.buildSqlBuilder;

public class SelectStatement {
    private String name = "";
    private DatabaseType databaseType;
    private List<String> columns = new ArrayList<>();
    private String table = "";
    private List<Criteria> criteria = new ArrayList<>();
    private List<Join> joins = new ArrayList<>();
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

    public List<Join> getJoins() {
        return joins;
    }

    public void setJoins(List<Join> joins) {
        this.joins = joins;
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
            //   lists that end on a child criteria.
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

    public String toSql(Properties properties) {
        try {
            databaseType = Enum.valueOf(DatabaseType.class, properties.getProperty("databaseType"));
            Collections.sort(this.criteria);
            clearParenthesisFromCriteria();
            addParenthesisToCriteria();
            SqlBuilder sqlBuilder = buildSqlBuilder(databaseType, this, properties);
            return sqlBuilder.buildSql(this);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public SqlParameterSource getSqlParameterMap() {
        int namedParameterCount = 0;
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();

        for (Criteria crit : criteria) {
            if (crit.operator.equals(Operator.isNull) || crit.operator.equals(Operator.isNotNull)) {
                namedParameterCount++;
                continue;
            }

            if (crit.operator.equals(Operator.in) || crit.operator.equals(Operator.notIn)) {
                String[] filters = crit.filter.split(",");
                for (String filter : filters) {
                    String paramName = "filter" + namedParameterCount;
                    namedParameters.addValue(paramName, filter);
                    namedParameterCount++;
                }
            } else {
                String paramName = "filter" + namedParameterCount;
                namedParameters.addValue(paramName, crit.filter);
                namedParameterCount++;
            }
        }

        return namedParameters;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");

        sb.append(String.format("Name:  %s | ", name));

        sb.append(String.format("Database Type:  %s | ", databaseType));

        columns.forEach(col -> sb.append(String.format("Column:  %s | ", col)));

        sb.append(String.format("Table:  %s | ", table));

        sb.append("Criteria:  ");
        criteria.forEach(crit -> {
            sb.append(String.format("Id:  %d ", crit.getId()));
            sb.append(String.format("ParentId:  %d ", crit.getParentId()));
            sb.append(String.format("Conjunction:  %s ", crit.getConjunction()));
            sb.append(String.format("Front Parenthesis:  %s ", crit.getFrontParenthesis()));
            sb.append(String.format("Column:  %s ", crit.getColumn()));
            sb.append(String.format("Operator:  %s ", crit.getOperator()));
            sb.append(String.format("Filter:  %s ", crit.getFilter()));
            sb.append("End Parenthesis:  ");
            crit.endParenthesis.forEach(paren -> sb.append(paren));
            sb.append(" | ");
        });

        sb.append(String.format("Distinct:  %s | ", distinct));

        sb.append(String.format("Group By:  %s | ", groupBy));

        sb.append(String.format("Order By:  %s | ", orderBy));

        sb.append(String.format("Limit:  %d | ", limit));

        sb.append(String.format("Ascending:  %s | ", ascending));

        sb.append(String.format("Offset:  %d | ", offset));

        sb.append(String.format("Suppress Nulls:  %s | ", suppressNulls));

        return sb.toString();
    }

}
