package com.querybuilder4j;

import com.querybuilder4j.config.Conjunction;
import com.querybuilder4j.config.Operator;
import com.querybuilder4j.sqlbuilders.SqlBuilder;
import com.querybuilder4j.sqlbuilders.statements.Criteria;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;
import com.querybuilder4j.sqlbuilders.statements.Statement;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.querybuilder4j.config.Conjunction.*;
import static com.querybuilder4j.config.Operator.*;

public class QueryTests {
    private SqlBuilder sqlBuilder;
    private Properties properties;
    private String table = "county_spending_detail";
    private List<String> columns = new ArrayList<>();
    private Criteria criteria1;
    private Criteria criteria2;
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
        stmt.setTableSchema(TestUtils.multiColumnResultSetBuilder(properties).getMetaData());
        stmt.setDistinct(false);
        stmt.setColumns(columns);
        stmt.setTable(table);
        stmt.setLimit(this.limit);
        stmt.setOffset(this.offset);

        //set up criteria1
        criteria1 = new Criteria();
        criteria1.conjunction = And;
        criteria1.column = "fund";
        criteria1.operator = equalTo;
        criteria1.filter = "Permitting";

        // set up criteria2
        criteria2 = new Criteria();
        criteria2.conjunction = And;
        criteria2.column = "service";
        criteria2.operator = in;
        criteria2.filter = "Housing and Community Development";

        // set up criteriaWithIsNotNull
        criteriaWithIsNotNull = new Criteria();
        criteriaWithIsNotNull.conjunction = And;
        criteriaWithIsNotNull.column = "fund";
        criteriaWithIsNotNull.operator = isNotNull;

        //setup multiple criteria
        multipleCriteria.add(criteria1);
        multipleCriteria.add(criteria2);

        return stmt;
    }

    public String buildSql_LimitSuppressNulls() throws Exception {
        SelectStatement stmt = createNewMainSelectStmt();

        return sqlBuilder.buildSql(stmt);
    }

}
