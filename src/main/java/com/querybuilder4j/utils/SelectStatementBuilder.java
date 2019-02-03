package com.querybuilder4j.utils;

import com.querybuilder4j.config.Conjunction;
import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.config.Operator;
import com.querybuilder4j.sqlbuilders.statements.Criteria;
import com.querybuilder4j.sqlbuilders.statements.Join;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;

import java.util.Arrays;
import java.util.List;

public class SelectStatementBuilder {

    public static SelectStatement stmt;

    public static SelectStatement select(String[] columns) {
        stmt = new SelectStatement();
        stmt.setColumns(Arrays.asList(columns));
        return stmt;
    }

    public static SelectStatement select(DatabaseType databaseType, List<String> columns) {
        stmt = new SelectStatement();
        stmt.setColumns(columns);
        return stmt;
    }

    public static SelectStatement selectDistinct(List<String> columns) {
        stmt = new SelectStatement();
        stmt.setColumns(columns);
        return stmt;
    }

    public SelectStatement distinct() {
        stmt.setDistinct(true);
        return stmt;
    }

    public SelectStatement from(String table) {
        stmt.setTable(table);
        return stmt;
    }

    public SelectStatement where(String tableAndColumn, Operator operator, String filter) {
        Criteria criteria = new Criteria();
        criteria.setColumn(tableAndColumn);
        criteria.setOperator(operator);
        criteria.setFilter(filter);
        criteria.setId(0);
        criteria.setParentId(null);
        stmt.getCriteria().add(criteria);
        return stmt;
    }

    public SelectStatement and(String tableAndColumn, Operator operator, String filter, int parentId) {
        Criteria criteria = createCriteria(Conjunction.And, tableAndColumn, operator, filter, parentId);
        stmt.getCriteria().add(criteria);
        return stmt;
    }

    public SelectStatement or(String tableAndColumn, Operator operator, String filter, int parentId) {
        Criteria criteria = createCriteria(Conjunction.Or, tableAndColumn, operator, filter, parentId);
        stmt.getCriteria().add(criteria);
        return stmt;
    }

    public SelectStatement andSuppressNulls() {
        stmt.setSuppressNulls(true);
        return stmt;
    }

    public SelectStatement innerJoin(String targetTable, List<String> parentTableAndColumns, List<String> targetTableAndColumns) {
        Join join = createJoin(Join.JoinType.INNER, stmt.getTable(), targetTable, parentTableAndColumns, targetTableAndColumns);
        stmt.getJoins().add(join);
        return stmt;
    }

    public SelectStatement outerJoin(String targetTable,
                                     List<String> parentTableAndColumns,
                                     List<String> targetTableAndColumns) {
        Join join = createJoin(Join.JoinType.OUTER, stmt.getTable(), targetTable, parentTableAndColumns, targetTableAndColumns);
        stmt.getJoins().add(join);
        return stmt;
    }

    public SelectStatement leftJoin(String targetTable,
                                    List<String> parentTableAndColumns,
                                    List<String> targetTableAndColumns) {
        Join join = createJoin(Join.JoinType.LEFT, stmt.getTable(), targetTable, parentTableAndColumns, targetTableAndColumns);
        stmt.getJoins().add(join);
        return stmt;
    }

    public SelectStatement rightJoin(String targetTable,
                                     List<String> parentTableAndColumns,
                                     List<String> targetTableAndColumns) {
        Join join = createJoin(Join.JoinType.RIGHT, stmt.getTable(), targetTable, parentTableAndColumns, targetTableAndColumns);
        stmt.getJoins().add(join);
        return stmt;
    }

    public SelectStatement fullJoin(String targetTable,
                                    List<String> parentTableAndColumns,
                                    List<String> targetTableAndColumns) {
        Join join = createJoin(Join.JoinType.FULL, stmt.getTable(), targetTable, parentTableAndColumns, targetTableAndColumns);
        stmt.getJoins().add(join);
        return stmt;
    }

    public SelectStatement limit(Long limit) {
        stmt.setLimit(limit);
        return stmt;
    }

    public SelectStatement offset(Long offset) {
        stmt.setOffset(offset);
        return stmt;
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
        criteria.setId(stmt.getCriteria().size());
        criteria.setParentId(parentId);
        return criteria;
    }

}
