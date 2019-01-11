package com.querybuilder4j.sqlbuilders.statements;


import com.querybuilder4j.config.*;
import com.querybuilder4j.exceptions.NoMatchingParameterException;
import com.querybuilder4j.sqlbuilders.SqlBuilder;
import com.querybuilder4j.sqlbuilders.dao.QueryTemplateDao;
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
//        replaceParameters(); //Todo: is this needed?
//    }

    public String toSql(Properties properties) {
        try {
            databaseType = Enum.valueOf(DatabaseType.class, properties.getProperty("databaseType"));
            Collections.sort(this.criteria);
            clearParenthesisFromCriteria();
            addParenthesisToCriteria();
            replaceParameters(); //Todo: is this needed?
            SqlBuilder sqlBuilder = SqlBuilderFactory.buildSqlBuilder(databaseType, this, subQueries, properties, queryTemplateDao);
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

    private void replaceParameters() throws NoMatchingParameterException {
        if (criteriaArguments.size() != 0) {
            for (Criteria criterion : criteria) {
                String filter = criterion.filter.toString();
                if (filter.substring(0, 7).equals("$param ")) {
                    String paramName = filter.substring(7);
                    String paramValue = criteriaArguments.get(paramName);
                    if (paramValue != null) {
                        criterion.filter = paramValue;
                    } else {
                        String message = String.format("No criteria parameter was found with the name, %s", paramName);
                        throw new NoMatchingParameterException(message);
                    }
                }
            }
        }
    }

    /************************
     * Start of builder API
     ************************/

    public static SelectStatement select(DatabaseType databaseType, List<String> columns) {
        SelectStatement statement = new SelectStatement();
        statement.setColumns(columns);
        return statement;
    }

    public SelectStatement distinct() {
        this.distinct = true;
        return this;
    }

    public SelectStatement from(String table) {
        this.table = table;
        return this;
    }

    public SelectStatement where(String tableAndColumn, Operator operator, String filter) {
        Criteria criteria = new Criteria();
        criteria.setColumn(tableAndColumn);
        criteria.setOperator(operator);
        criteria.setFilter(filter);
        criteria.setId(0);
        criteria.setParentId(null);
        this.criteria.add(criteria);
        return this;
    }

    public SelectStatement and(String tableAndColumn, Operator operator, String filter, int parentId) {
        Criteria criteria = createCriteria(Conjunction.And, tableAndColumn, operator, filter, parentId);
        this.criteria.add(criteria);
        return this;
    }

    public SelectStatement or(String tableAndColumn, Operator operator, String filter, int parentId) {
        Criteria criteria = createCriteria(Conjunction.Or, tableAndColumn, operator, filter, parentId);
        this.criteria.add(criteria);
        return this;
    }

    public SelectStatement andSuppressNulls() {
        this.suppressNulls = true;
        return this;
    }

    public SelectStatement innerJoin(String targetTable,
                                     List<String> parentTableAndColumns,
                                     List<String> targetTableAndColumns) {
        Join join = createJoin(Join.JoinType.INNER, this.table, targetTable, parentTableAndColumns, targetTableAndColumns);
        this.joins.add(join);
        return this;
    }

    public SelectStatement outerJoin(String targetTable,
                                     List<String> parentTableAndColumns,
                                     List<String> targetTableAndColumns) {
        Join join = createJoin(Join.JoinType.OUTER, this.table, targetTable, parentTableAndColumns, targetTableAndColumns);
        this.joins.add(join);
        return this;
    }

    public SelectStatement leftJoin(String targetTable,
                                     List<String> parentTableAndColumns,
                                     List<String> targetTableAndColumns) {
        Join join = createJoin(Join.JoinType.LEFT, this.table, targetTable, parentTableAndColumns, targetTableAndColumns);
        this.joins.add(join);
        return this;
    }

    public SelectStatement rightJoin(String targetTable,
                                     List<String> parentTableAndColumns,
                                     List<String> targetTableAndColumns) {
        Join join = createJoin(Join.JoinType.RIGHT, this.table, targetTable, parentTableAndColumns, targetTableAndColumns);
        this.joins.add(join);
        return this;
    }

    public SelectStatement fullJoin(String targetTable,
                                    List<String> parentTableAndColumns,
                                    List<String> targetTableAndColumns) {
        Join join = createJoin(Join.JoinType.FULL, this.table, targetTable, parentTableAndColumns, targetTableAndColumns);
        this.joins.add(join);
        return this;
    }

    public SelectStatement limit(Long limit) {
        this.limit = limit;
        return this;
    }

    public SelectStatement offset(Long offset) {
        this.offset = offset;
        return this;
    }

    private Join createJoin(Join.JoinType joinType,
                            String parentTable,
                            String targetTable,
                            List<String> parentTableAndColumns,
                            List<String> targetTableAndColumns) {
        Join join = new Join();
        join.setJoinType(joinType);
        join.setParentTable(parentTable);
        join.setTargetTable(targetTable);
        join.setParentJoinColumns(parentTableAndColumns);
        join.setTargetJoinColumns(targetTableAndColumns);
        return join;
    }

    private Criteria createCriteria(Conjunction conjunction, String tableAndColumn, Operator operator,
                                    String filter, int parentId) {
        Criteria criteria = new Criteria();
        criteria.setConjunction(conjunction);
        criteria.setColumn(tableAndColumn);
        criteria.setOperator(operator);
        criteria.setFilter(filter);
        criteria.setId(this.criteria.size());
        criteria.setParentId(parentId);
        return criteria;
    }

}
