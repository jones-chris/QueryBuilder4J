package com.querybuilder4j.sqlbuilders.statements;

import java.sql.ResultSetMetaData;
import java.util.*;

import static com.querybuilder4j.config.Parenthesis.*;

public abstract class Statement {
    protected String name;
    protected Properties properties;
    protected ResultSetMetaData tableSchema;
    protected List<String> columns = new ArrayList<>();
    protected String table;
    protected SortedSet<Criteria> criteria = new TreeSet<>();


    public Statement() {}

    public SortedSet<Criteria> getCriteria() {
        return criteria;
    }

    public Statement setCriteria(SortedSet<Criteria> criteria) {
        this.criteria = criteria;
        return this;
    }

    public String getName() {
        return name;
    }

    public Statement setName(String name) {
        this.name = name;
        return this;
    }

    public Properties getProperties() {
        return properties;
    }

    public Statement setProperties(Properties properties) {
        this.properties = properties;
        return this;
    }

    public ResultSetMetaData getTableSchema() {
        return tableSchema;
    }

    public Statement setTableSchema(ResultSetMetaData tableSchema) {
        this.tableSchema = tableSchema;
        return this;
    }

    public String getTable() {
        return table;
    }

    public Statement setTable(String table) {
        this.table = table;
        return this;
    }

    public List<String> getColumns() {
        return columns;
    }

    public Statement setColumns(List<String> columns) {
        this.columns = columns;
        return this;
    }

    public void addParenthesisToCriteria() {
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
                    criteria.last().endParenthesis.add(EndParenthesis);
                }
            }
        }

    }

    private int getLastIndexOfCriteriaAsParent(Integer parentId) {
        if (parentId == null) return -1;
        List<Criteria> criteriaList = new ArrayList<>(criteria);
        for (int i=criteriaList.size()-1; i>=0; i--) {
            if (criteriaList.get(i).parentId == null) continue;
            if (criteriaList.get(i).parentId == parentId) return i;
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

}
