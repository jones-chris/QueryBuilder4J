package com.querybuilder4j.utils;

import com.querybuilder4j.config.Conjunction;
import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.config.Operator;
import com.querybuilder4j.dao.QueryTemplateDao;
import com.querybuilder4j.statements.Criteria;
import com.querybuilder4j.statements.Join;
import com.querybuilder4j.statements.SelectStatement;

import java.util.Arrays;
import java.util.List;

public class SelectStatementFactory {

    private static SelectStatement stmt;

    /**
     * Serves as the getter for the stmt field.
     *
     * @param databaseType
     * @return
     */
    public SelectStatement getSelectStatement(DatabaseType databaseType) {
        stmt.setDatabaseType(databaseType);
        return stmt;
    }

    public SelectStatementFactory select(String... columns) {
        stmt = new SelectStatement();
        stmt.setColumns(Arrays.asList(columns));
        return this;
    }

    public SelectStatementFactory select(List<String> columns) {
        stmt = new SelectStatement();
        stmt.setColumns(columns);
        return this;
    }

    public SelectStatementFactory selectDistinct(String... columns) {
        stmt = new SelectStatement();
        stmt.setColumns(Arrays.asList(columns));
        return this;
    }

    public SelectStatementFactory distinct() {
        stmt.setDistinct(true);
        return this;
    }

    public SelectStatementFactory from(String table) {
        stmt.setTable(table);
        return this;
    }

    public SelectStatementFactory where(String tableAndColumn, Operator operator, String filter) {
        Criteria criteria = new Criteria();
        criteria.setColumn(tableAndColumn);
        criteria.setOperator(operator);
        criteria.setFilter(filter);
        criteria.setId(0);
        criteria.setParentId(null);
        stmt.getCriteria().add(criteria);
        return this;
    }

    public SelectStatementFactory and(String tableAndColumn, Operator operator, String filter, Integer parentId) {
        createCriteria(Conjunction.And, tableAndColumn, operator, filter, parentId);
        return this;
    }

    public SelectStatementFactory or(String tableAndColumn, Operator operator, String filter, Integer parentId) {
        createCriteria(Conjunction.Or, tableAndColumn, operator, filter, parentId);
        return this;
    }

    public SelectStatementFactory andSuppressNulls() {
        stmt.setSuppressNulls(true);
        return this;
    }

    public SelectStatementFactory innerJoin(String targetTable,
                                 List<String> parentTableAndColumns,
                                 List<String> targetTableAndColumns) {
        createJoin(Join.JoinType.INNER, stmt.getTable(), targetTable, parentTableAndColumns, targetTableAndColumns);
        return this;
    }

    public SelectStatementFactory outerJoin(String targetTable,
                                 List<String> parentTableAndColumns,
                                 List<String> targetTableAndColumns) {
        createJoin(Join.JoinType.FULL_OUTER, stmt.getTable(), targetTable, parentTableAndColumns, targetTableAndColumns);
        return this;
    }

    public SelectStatementFactory leftJoin(String targetTable,
                                List<String> parentTableAndColumns,
                                List<String> targetTableAndColumns) {
        createJoin(Join.JoinType.LEFT, stmt.getTable(), targetTable, parentTableAndColumns, targetTableAndColumns);
        return this;
    }

    public SelectStatementFactory rightJoin(String targetTable,
                                 List<String> parentTableAndColumns,
                                 List<String> targetTableAndColumns) {
        createJoin(Join.JoinType.RIGHT, stmt.getTable(), targetTable, parentTableAndColumns, targetTableAndColumns);
        return this;
    }

    public SelectStatementFactory fullJoin(String targetTable,
                                List<String> parentTableAndColumns,
                                List<String> targetTableAndColumns) {
        createJoin(Join.JoinType.FULL_OUTER, stmt.getTable(), targetTable, parentTableAndColumns, targetTableAndColumns);
        return this;
    }

    public SelectStatementFactory limit(Long limit) {
        stmt.setLimit(limit);
        return this;
    }

    public SelectStatementFactory offset(Long offset) {
        stmt.setOffset(offset);
        return this;
    }

    public SelectStatementFactory setQueryTemplateDao(QueryTemplateDao queryTemplateDao) {
        stmt.setQueryTemplateDao(queryTemplateDao);
        return this;
    }

    private void createJoin(Join.JoinType joinType,
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
        stmt.getJoins().add(join);
    }

    private void createCriteria(Conjunction conjunction, String tableAndColumn, Operator operator,
                                    String filter, Integer parentId) {
        Criteria criterion = new Criteria();
        criterion.setConjunction(conjunction);
        criterion.setColumn(tableAndColumn);
        criterion.setOperator(operator);
        criterion.setFilter(filter);
        criterion.setId(stmt.getCriteria().size());
        criterion.setParentId(parentId);
        stmt.getCriteria().add(criterion);
    }

}
