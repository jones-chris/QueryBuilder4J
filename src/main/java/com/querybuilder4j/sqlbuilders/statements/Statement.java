package com.querybuilder4j.sqlbuilders.statements;

import com.querybuilder4j.config.Parenthesis;

import java.sql.ResultSetMetaData;
import java.util.*;

import static com.querybuilder4j.config.Parenthesis.*;

public abstract class Statement {
    protected String queryName;
    protected Properties properties;
    protected ResultSetMetaData tableSchema;
    private SortedSet<Criteria> criteria = new TreeSet<>();

    public Statement() {}

    public SortedSet<Criteria> getCriteria() {
        return criteria;
    }

    public Statement setCriteria(SortedSet<Criteria> criteria) {
        this.criteria = criteria;
        return this;
    }

    public abstract String buildSql();

    public void addParenthesisToCriteria() {
        // add logic to add/remove parenthesis to criteria

        List<Criteria> criteriaList = new ArrayList<>(criteria);
        if (criteria.size() > 0) {
            for (int i=0; i<criteria.size(); i++) {

                Criteria theCriteria = criteriaList.get(i);

                if (isCriteriaAParent(theCriteria.getId())) {
                    theCriteria.frontParenthesis = FrontParenthesis;
                }

                // if this is the last index of the criteria with this parent id, then add end parenthesis.
                if (getLastIndexOfCriteriaAsParent(theCriteria.parentId) == i) {
                    theCriteria.endParenthesis = EndParenthesis;
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
