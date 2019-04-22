package com.querybuilder4j.sqlbuilders.statements;


import com.google.gson.Gson;
import com.querybuilder4j.config.*;
import com.querybuilder4j.exceptions.NoMatchingParameterException;
import com.querybuilder4j.exceptions.WrongNumberArgsException;
import com.querybuilder4j.sqlbuilders.SqlBuilder;
import com.querybuilder4j.sqlbuilders.dao.QueryTemplateDao;
//import org.codehaus.jackson.map.ObjectMapper;

import java.util.*;

import static com.querybuilder4j.config.Parenthesis.EndParenthesis;
import static com.querybuilder4j.config.Parenthesis.FrontParenthesis;
import static com.querybuilder4j.sqlbuilders.statements.Join.JoinType.LEFT_EXCLUDING;
import static com.querybuilder4j.sqlbuilders.statements.Join.JoinType.RIGHT_EXCLUDING;
import static com.querybuilder4j.sqlbuilders.statements.Join.JoinType.FULL_OUTER_EXCLUDING;

public class SelectStatement {
//    private Integer id;
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

//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }

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

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isGroupBy() {
        return groupBy;
    }

    public void setGroupBy(boolean groupBy) {
        this.groupBy = groupBy;
    }

    public boolean isOrderBy() {
        return orderBy;
    }

    public void setOrderBy(boolean orderBy) {
        this.orderBy = orderBy;
    }

    public Long getLimit() {
        return limit;
    }

//    public SelectStatement setLimit(long limit) {
//        this.limit = limit;
//        return this;
//    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public Long getOffset() {
        return offset;
    }

//    public SelectStatement setOffset(long offset) {
//        this.offset = offset;
//        return this;
//    }

    public boolean isSuppressNulls() {
        return suppressNulls;
    }

    public void setSuppressNulls(boolean suppressNulls) {
        this.suppressNulls = suppressNulls;
    }

    public Map<String, String> getSubQueries() {
        return subQueries;
    }

    /**
     * One of two setters for subQueries field.  It's not recommended that developers use this because the parameter-less
     * setter will set subQueries automatically if subQuery calls are hand-written into the a criterion's filter.
     * @param subQueries
     */
    public void setSubQueries(Map<String, String> subQueries) {
        this.subQueries = subQueries;
    }

    /**
     * Automatically sets the subQueries field assuming that the subQuery calls are hand-written into a criterion's filter.
     * If you want to set the subQueries field manually, use the public setSubQueries method.
     */
    protected void setSubqueries() throws IllegalArgumentException {
        if (criteria.size() != 0 && queryTemplateDao != null) {
            criteria.forEach((criterion) -> {
                String[] filters = criterion.filter.split(",");
                for (String filter : filters) {
                    if (SqlBuilder.argIsSubQuery(filter)) {
                        LinkedList<Integer> begSubQueryIndeces = new LinkedList<>();
                        LinkedList<Integer> endSubQueryIndeces = new LinkedList<>();
                        char[] filterChars = filter.toCharArray();

                        for (int i=0; i<filterChars.length; i++) {
                            if (filterChars[i] == '$') {
                                begSubQueryIndeces.add(i);
                            } else if (filterChars[i] == ')') {
                                endSubQueryIndeces.add(i);
                            }
                        }

                        // Check that there are equal number of beginning and ending subQuery indeces - otherwise we have
                        // a malformed subQuery call.
                        if (begSubQueryIndeces.size() == endSubQueryIndeces.size()) {
                            // It's okay to make the while condition based on only one of the LinkedLists because we know at this
                            // point that both LinkedLists are equal sizes.
                            String newFilter = new String(filter);
                            while (begSubQueryIndeces.size() != 0) {
                                String subQueryId = "$" + subQueries.size();

                                // Match up beginning subQuery index with ending subQuery index.
//                                int consecutiveEndParenthesis = 0;
//                                for (int i=newFilter.length()-2; i>=0; i--) {
//                                    if (newFilter.charAt(i) == ')') {
//                                        consecutiveEndParenthesis ++;
//                                    } else {
//                                        break;
//                                    }
//                                }

                                // Always remove last beginning subQuery index.
                                // Only remove last ending subQuery index if it is the last item in the list - because it will
                                // always match up with the first beginning subQuery index.  Otherwise, calculate the ending
                                // subQuery index to remove.
//                                int begSubQueryIndex = begSubQueryIndeces.removeLast();
//                                int endSubQueryIndex;
//                                if (endSubQueryIndeces.size() > 1 && consecutiveEndParenthesis != 0) {
//                                    endSubQueryIndex = endSubQueryIndeces.remove(endSubQueryIndeces.size() - 1 - consecutiveEndParenthesis);
//                                } else if (endSubQueryIndeces.size() > 1 && consecutiveEndParenthesis == 0) {
//                                    endSubQueryIndex = endSubQueryIndeces.remove(endSubQueryIndeces.size() - 2);
//                                } else {
//                                    endSubQueryIndex = endSubQueryIndeces.removeLast();
//                                }
                                int begSubQueryIndex = begSubQueryIndeces.removeLast();
                                int endSubQueryIndex = 1000;
                                // Find ending index that is greater than beginning index, but closest to ending index.
                                for (Integer endIndex : endSubQueryIndeces) {
                                    if (endIndex > begSubQueryIndex) {
                                        if ((endIndex - begSubQueryIndex) < (endSubQueryIndex - begSubQueryIndex)) {
                                            endSubQueryIndex = endIndex;
                                        }
                                    }
                                }
                                endSubQueryIndeces.remove(new Integer(endSubQueryIndex));

                                // Now, get the subQueryCall from filter (which does not change)
                                String subQueryCall = newFilter.substring(begSubQueryIndex + 1, endSubQueryIndex + 1); // remove +1 from begSubQueryIndex?

                                // Now, look in newFilter (which changes) and replace that subQueryCall with the subQueryId
                                newFilter = newFilter.replace("$" + subQueryCall, subQueryId);

                                // Now, add the subQueryId and subQueryCall to subQueries.
                                subQueries.put(subQueryId, subQueryCall);

                                for (int i=0; i<begSubQueryIndeces.size(); i++) {
                                    int begElement = begSubQueryIndeces.get(i);
                                    if (begElement > begSubQueryIndex) {
                                        int newElement = begElement - (subQueryCall.length());
                                        begSubQueryIndeces.set(i, newElement);
                                    }

                                    int endElement = endSubQueryIndeces.get(i);
                                    if (endElement > endSubQueryIndex) {
                                        int newElement = endElement - (subQueryCall.length()-1);
                                        endSubQueryIndeces.set(i, newElement);
                                    }
                                }
                            }
                            criterion.filter = newFilter;
                        } else {
                            throw new IllegalArgumentException("SubQuery is malformed");
                        }
                    }
                }
            });
        }
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

    @SuppressWarnings("unchecked")
    public String toSql(Properties properties) {
        try {
            databaseType = Enum.valueOf(DatabaseType.class, properties.getProperty("databaseType"));
            addExcludingJoinCriteria();
            Collections.sort(this.criteria);
            clearParenthesisFromCriteria();
            addParenthesisToCriteria();
            // If subQueries has not been set (it will have a 0 size, if that's the case), then set subQueries.
//            if (subQueries.size() == 0) { setSubqueries(); }
            setSubqueries();
            replaceParameters();
            SqlBuilder sqlBuilder = SqlBuilderFactory.buildSqlBuilder(databaseType, this, properties); // subQueries get built here.
            return sqlBuilder.buildSql(); // root query gets built here.
        } catch (Exception e) {
            throw new RuntimeException(e);  // Todo:  pass exception into RunTimeException constructor instead of just the message for better error descriptions.
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
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            return mapper.writeValueAsString(this);
//        } catch (IOException ex) {
//            return "";
//        }

        //return "hello";
    }

    /**
     * Checks that there is an equal number of parameters in the criteria (not the criteriaParameters field) and
     * criteriaArguments.  After doing so, it attempts to replace the parameters in the criteria (again, not the
     * criteriaParameters field) with the relevant value from criteriaArguments.
     *
     * @throws NoMatchingParameterException if the parameter cannot be found as a key in criteriaArguments.
     * @throws WrongNumberArgsException if the number of criteria parameters (not the criteriaParameters field) is not the
     * same as the number of criteriaArguments.
     */
    private void replaceParameters() throws NoMatchingParameterException, WrongNumberArgsException {
        // Throw exception if there are parameters in the criteria, but not the same number of criteria arguments.
//        int numOfParams = 0;
//        for (Criteria criterion : criteria) {
//            if (criterion.filter != null) {
//                String[] splitFilters = criterion.filter.split(",");
//                for (String splitFilter : splitFilters) {
//                    if (splitFilter.length() >= 7 && (splitFilter.substring(0, 7).equals("$param:") || splitFilter.substring(0, 8).equals("subquery"))) {
//                        numOfParams++;
//                    }
//                }
//            }
//
//        }
//        if (numOfParams != criteriaArguments.size()) {
//            String message = String.format("There are %d parameters in the statement's criteria, but %d criteria arguments.  " +
//                    "There should be the same number of parameters and arguments.  Object:  %S", numOfParams, criteriaArguments.size(), this.toString());
//            throw new WrongNumberArgsException(message);
//        }

        // Now that we know there are equal number of parameters and arguments, try replacing the parameters with arguments.
        if (criteriaArguments.size() != 0) {
            for (Criteria criterion : criteria) {

                String filter = criterion.filter;
                String[] splitFilters = filter.split(",");
                List<String> resultFilters = new ArrayList<>();

                for (String splitFilter : splitFilters) {
                    if (splitFilter.length() >= 1 && splitFilter.substring(0, 1).equals("@")) {
                        String paramName = splitFilter.substring(1);
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
