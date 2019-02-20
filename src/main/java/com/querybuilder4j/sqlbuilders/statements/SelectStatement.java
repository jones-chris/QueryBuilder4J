package com.querybuilder4j.sqlbuilders.statements;


import com.google.gson.Gson;
import com.querybuilder4j.config.*;
import com.querybuilder4j.exceptions.NoMatchingParameterException;
import com.querybuilder4j.sqlbuilders.SqlBuilder;
import com.querybuilder4j.sqlbuilders.dao.QueryTemplateDao;

import java.util.*;

import static com.querybuilder4j.config.Parenthesis.EndParenthesis;
import static com.querybuilder4j.config.Parenthesis.FrontParenthesis;
import static com.querybuilder4j.sqlbuilders.statements.Join.JoinType.LEFT_EXCLUDING;
import static com.querybuilder4j.sqlbuilders.statements.Join.JoinType.RIGHT_EXCLUDING;
import static com.querybuilder4j.sqlbuilders.statements.Join.JoinType.FULL_OUTER_EXCLUDING;

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
    private Long limit = null;
    private boolean ascending;
    private Long offset = null;
    private boolean suppressNulls;
    private Map<String, String> subQueries = new HashMap<>();
    private QueryTemplateDao queryTemplateDao;

    /**
     * The query's criteria runtime arguments.  The key is the name of the parameter to find in the query criteria.  The
     * value is what will be passed into the query criteria.
     */
    private Map<String, String> criteriaArguments = new HashMap<>();

    /**
     * The query's criteria parameters.  The key is the name of the parameter to find in the query criteria.  The value is
     * a description of the parameter that can be referenced by developers or in the application UI.
     */
    private Map<String, String> criteriaParameters = new HashMap<>();


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

    public Long getLimit() {
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

    public Long getOffset() {
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

    public Map<String, String> getSubQueries() {
        return subQueries;
    }

    public void setSubQueries(Map<String, String> subQueries) {
        this.subQueries = subQueries;
    }

    public QueryTemplateDao getQueryTemplateDao() {
        return queryTemplateDao;
    }

    public void setQueryTemplateDao(QueryTemplateDao queryTemplateDao) {
        this.queryTemplateDao = queryTemplateDao;
    }

    public Map<String, String> getCriteriaArguments() {
        return criteriaArguments;
    }

    public void setCriteriaArguments(Map<String, String> criteriaArguments) {
        this.criteriaArguments = criteriaArguments;
    }

    public Map<String, String> getCriteriaParameters() {
        return criteriaParameters;
    }

    public void setCriteriaParameters(Map<String, String> criteraParameters) {
        this.criteriaParameters = criteraParameters;
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

//    public void prepStatementToBecomeSQL() throws Exception {
//        Collections.sort(this.criteria);
//        clearParenthesisFromCriteria();
//        addParenthesisToCriteria();
//        replaceParameters();
//    }

//    /**
//     * Call this before using a SqlBuilder class to build a SQL statement from a SelectStatement.
//     */
//    public void prepareStatement() {
//        try {
//            Collections.sort(this.criteria);
//            clearParenthesisFromCriteria();
//            addParenthesisToCriteria();
//            replaceParameters();
//        } catch (Exception e) {
//            throw new RuntimeException(e.getMessage());
//        }
//    }

    public String toSql(Properties properties) {
        try {
            databaseType = Enum.valueOf(DatabaseType.class, properties.getProperty("databaseType"));
            addExcludingJoinCriteria();
            Collections.sort(this.criteria);
            clearParenthesisFromCriteria();
            addParenthesisToCriteria();
            replaceParameters();
            SqlBuilder sqlBuilder = SqlBuilderFactory.buildSqlBuilder(databaseType, this, properties);
            return sqlBuilder.buildSql();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

//    public Map<String, Object> getSqlParameterMap(int startingIndex) {
//        //int namedParameterCount = 0;
//        int namedParameterCount = startingIndex;
//        Map<String, Object> namedParameters = new HashMap<>();
//
//        for (Criteria crit : criteria) {
//            if (crit.operator.equals(Operator.isNull) || crit.operator.equals(Operator.isNotNull)) {
//                namedParameterCount++;
//                continue;
//            }
//
//            if (crit.operator.equals(Operator.in) || crit.operator.equals(Operator.notIn)) {
//                String[] filters = crit.filter.split(",");
//                for (String filter : filters) {
//                    String paramName = "filter" + namedParameterCount;
//                    namedParameters.put(paramName, filter);
//                    namedParameterCount++;
//                }
//            } else {
//                String paramName = "filter" + namedParameterCount;
//                namedParameters.put(paramName, crit.filter);
//                namedParameterCount++;
//            }
//        }
//
//        return namedParameters;
//    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
//        StringBuilder sb = new StringBuilder("");
//
//        sb.append(String.format("Name:  %s | ", name));
//
//        sb.append(String.format("Database Type:  %s | ", databaseType));
//
//        columns.forEach(col -> sb.append(String.format("Column:  %s | ", col)));
//
//        sb.append(String.format("Table:  %s | ", table));
//
//        sb.append("Criteria:  ");
//        criteria.forEach(crit -> {
//            sb.append(String.format("Id:  %d ", crit.getId()));
//            sb.append(String.format("ParentId:  %d ", crit.getParentId()));
//            sb.append(String.format("Conjunction:  %s ", crit.getConjunction()));
//            sb.append(String.format("Front Parenthesis:  %s ", crit.getFrontParenthesis()));
//            sb.append(String.format("Column:  %s ", crit.getColumn()));
//            sb.append(String.format("Operator:  %s ", crit.getOperator()));
//            sb.append(String.format("Filter:  %s ", crit.getFilter()));
//            sb.append("End Parenthesis:  ");
//            crit.endParenthesis.forEach(paren -> sb.append(paren));
//            sb.append(" | ");
//        });
//
//        sb.append(String.format("Distinct:  %s | ", distinct));
//
//        sb.append(String.format("Group By:  %s | ", groupBy));
//
//        sb.append(String.format("Order By:  %s | ", orderBy));
//
//        sb.append(String.format("Limit:  %d | ", limit));
//
//        sb.append(String.format("Ascending:  %s | ", ascending));
//
//        sb.append(String.format("Offset:  %d | ", offset));
//
//        sb.append(String.format("Suppress Nulls:  %s | ", suppressNulls));
//
//        return sb.toString();
    }

    private void replaceParameters() throws NoMatchingParameterException {
        if (criteriaArguments.size() != 0) {
            for (Criteria criterion : criteria) {

                String filter = criterion.filter.toString();
                String[] splitFilters = filter.split(",");
                List<String> resultFilters = new ArrayList<>();

                for (String splitFilter : splitFilters) {
                    if (splitFilter.length() >= 7 && splitFilter.substring(0, 7).equals("$param:")) {
                        String paramName = splitFilter.substring(7);
                        String paramValue = criteriaArguments.get(paramName);
                        if (paramValue != null) {
                            resultFilters.add(paramValue);
                            //criterion.filter = paramValue;
                        } else {
                            String message = String.format("No criteria parameter was found with the name, %s", paramName);
                            throw new NoMatchingParameterException(message);
                        }
                    }
                }

                if (resultFilters.size() != 0) {
                    criterion.filter = String.join(",", resultFilters);
                }
            }
        }
    }

    /**
     * Adds isNull criterion to criteria if any of the statement's joins are an 'excluding' join, such as LEFT_JOIN_EXCLUDING,
     * RIGHT_JOIN_EXCLUDING, or FULL_OUTER_JOIN_EXCLUDING.
     */
    private void addExcludingJoinCriteria() {
        if (! this.joins.isEmpty()) {
            for (Join join : joins) {
                Join.JoinType joinType = join.getJoinType();
                if (joinType.equals(LEFT_EXCLUDING)) {
                    addCriteriaForExcludingJoin(join.getTargetTable(), join.getTargetJoinColumns());
                } else if (joinType.equals(RIGHT_EXCLUDING)) {
                    addCriteriaForExcludingJoin(join.getTargetTable(), join.getParentJoinColumns());
                } else if (joinType.equals(FULL_OUTER_EXCLUDING)) {
                    List<String> allJoinColumnns = new ArrayList<>();
                    allJoinColumnns.addAll(join.getParentJoinColumns());
                    allJoinColumnns.addAll(join.getTargetJoinColumns());
                    addCriteriaForExcludingJoin(join.getTargetTable(), allJoinColumnns);
                }
            }
        }
    }

    private void addCriteriaForExcludingJoin(String table, List<String> columns) {
        // Get max id
        int maxId = 0;
        for (Criteria criterion : criteria) {
            if (criterion.getId() > maxId) {
                maxId = criterion.getId();
            }
        }

        int nextId = maxId + 1;
        int parentId = 0;
        for (int i=0; i<columns.size(); i++) {
            Criteria criterion = new Criteria(nextId);

            // For the first iteration in this for loop, the parentId will be null.  On every iteration afterwards,
            // the parentId should be set to the nextId so that every criterion generated by this method is a child of
            // the first criterion that has a null parentId.
            if (i == 0) {
                parentId = nextId;
                criterion.setParentId(null);
                if (! criteria.isEmpty()) {
                    criterion.setConjunction(Conjunction.And);
                }
            } else {
                criterion.setParentId(parentId);
                criterion.setConjunction(Conjunction.Or);
            }

            criterion.setColumn(columns.get(i));
            criterion.setOperator(Operator.isNull);
            criteria.add(criterion);
        }
    }

}
