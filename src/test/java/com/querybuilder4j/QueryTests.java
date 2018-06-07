package com.querybuilder4j;

import com.querybuilder4j.sqlbuilders.SqlBuilder;
import com.querybuilder4j.sqlbuilders.statements.Criteria;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;

import java.util.*;

import static com.querybuilder4j.config.Conjunction.*;
import static com.querybuilder4j.config.Operator.*;

public class QueryTests {
    private SqlBuilder sqlBuilder;
    private Properties properties;
    private String table = "county_spending_detail";
    private List<String> columns = new ArrayList<>();
    private Criteria criteria0;
    private Criteria criteria1;
    private List<Criteria> multipleCriteria = new ArrayList<>();
    private Criteria criteriaWithIsNotNull;
    private long limit = 100;
    private long offset = 50;

    public QueryTests(SqlBuilder sqlBuilder, Properties properties) {
        this.sqlBuilder = sqlBuilder;
        this.properties = properties;
        columns.add("fund");
        columns.add("service");
    }

    public SelectStatement createNewMainSelectStmt() throws Exception {
//        FileReader reader = new FileReader("postgresql.properties");
//
//        Properties props = new Properties();
//        props.load(reader);

        SelectStatement stmt = new SelectStatement("main");
        stmt.setTableSchema(TestUtils.multiColumnResultSetBuilder(properties));
        stmt.setDistinct(false);
        stmt.setColumns(columns);
        stmt.setTable(table);
        stmt.setLimit(this.limit);
        stmt.setOffset(this.offset);

        //set up criteria0
        criteria0 = new Criteria(0);
        criteria0.parentId = null;
        criteria0.conjunction = And;
        criteria0.column = "fund";
        criteria0.operator = equalTo;
        criteria0.filter = "Permitting";

        // set up criteria1
        criteria1 = new Criteria(1);
        criteria1.parentId = null;
        criteria1.conjunction = And;
        criteria1.column = "service";
        criteria1.operator = in;
        criteria1.filter = "Housing and Community Development";

        // set up criteriaWithIsNotNull
        criteriaWithIsNotNull = new Criteria(2);
        criteriaWithIsNotNull.parentId = null;
        criteriaWithIsNotNull.conjunction = And;
        criteriaWithIsNotNull.column = "fund";
        criteriaWithIsNotNull.operator = isNotNull;

        //setup multiple criteria
        multipleCriteria.add(criteria0);
        multipleCriteria.add(criteria1);
        stmt.addCriteria(multipleCriteria);

        return stmt;
    }

    public String buildSql_LimitSuppressNulls() throws Exception {
        SelectStatement stmt = createNewMainSelectStmt();

        return sqlBuilder.buildSql(stmt);
    }

    public String buildSql_OneChild() throws Exception{
        Criteria childCriteria1 = new Criteria(2);
        childCriteria1.parentId = 1;
        childCriteria1.conjunction = Or;
        childCriteria1.column = "service";
        childCriteria1.operator = equalTo;
        childCriteria1.filter = "Mass Transit";
        SelectStatement stmt = createNewMainSelectStmt();
        stmt.addCriteria(childCriteria1);

        return sqlBuilder.buildSql(stmt);
    }

    public String buildSql_WithDistinct() throws Exception {
        SelectStatement stmt = createNewMainSelectStmt();
        stmt.setDistinct(true);

        return sqlBuilder.buildSql(stmt);
    }

    public String buildSql_GroupBy() throws Exception {
        SelectStatement stmt = createNewMainSelectStmt();
        stmt.setGroupBy(true);

        return sqlBuilder.buildSql(stmt);
    }

    public String buildSql_OrderBy() throws Exception {
        SelectStatement stmt = createNewMainSelectStmt();
        stmt.setOrderBy(true);

        return sqlBuilder.buildSql(stmt);
    }

    public String buildSql_GroupByOrderBy() throws Exception {
        SelectStatement stmt = createNewMainSelectStmt();
        stmt.setOffset(0);
        stmt.setGroupBy(true);
        stmt.setOrderBy(true);

        return sqlBuilder.buildSql(stmt);
    }

    public String buildSql_SuppressNulls() throws Exception {
        SelectStatement stmt = createNewMainSelectStmt();
        stmt.setSuppressNulls(true);

        return sqlBuilder.buildSql(stmt);
    }

//    public String buildSql_randomizer() throws Exception {
//
//    }
}
