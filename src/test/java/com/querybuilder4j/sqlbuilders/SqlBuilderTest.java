package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.QueryTests;
import com.querybuilder4j.TestUtils;
import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.config.SqlBuilderFactory;
import com.querybuilder4j.dbconnection.DbConnection;
import com.querybuilder4j.dbconnection.DbConnectionImpl;
import com.querybuilder4j.sqlbuilders.statements.Criteria;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import sun.reflect.generics.tree.ReturnType;

import java.io.FileReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.util.*;

import static com.querybuilder4j.config.Conjunction.And;
import static com.querybuilder4j.config.Operator.*;
import static com.querybuilder4j.config.Operator.lessThanOrEquals;
import static com.querybuilder4j.config.SqlBuilderFactory.buildSqlBuilder;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class SqlBuilderTest {
    public static List<String> columns = new ArrayList<>();
    public static String table = "county_spending_detail";
    public static Criteria criteria1 = new Criteria();
    public static Criteria criteria2 = new Criteria();
    public static List<Criteria> multipleCriteria = new ArrayList<>();
    public static SqlBuilder sqlBuilder;
    public static DbConnection conn;
    public List<String> propertiesFileNames = new ArrayList<>();
    public List<Properties> properties = new ArrayList<>();

    static {
        columns.add("fund");
        columns.add("service");
    }

    public SqlBuilderTest() {
        Properties propsPg = new Properties();
        propsPg.setProperty("url", "jdbc:postgresql://localhost:5432/postgres");
        propsPg.setProperty("username", "postgres");
        propsPg.setProperty("password", "budgeto");
        propsPg.setProperty("driverClass", "org.postgresql.Driver");
        propsPg.setProperty("databaseType", "PostgreSQL");
        properties.add(propsPg);

//        Properties propsMySql = new Properties();
//        propsMySql.setProperty("url", "jdbc:mysql://localhost:3306/sys");
//        propsMySql.setProperty("username", "root");
//        propsMySql.setProperty("password", "budgeto");
//        propsMySql.setProperty("driverClass", "com.mysql.cj.jdbc.Driver");
//        propsMySql.setProperty("databaseType", "MySql");
//        properties.add(propsMySql);
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void buildSql_FailedTest1() throws Exception {
        /*Id:0
        parentId:null
        frontParen:(
                conjunction:And
        column:fund
        operator:=
        filter:Permitting
                endParen[]
        Id:1
        parentId:0
        frontParen:(
                conjunction:And
        column:fund
        operator:>
        filter:Permitting
                endParen[]
        Id:2
        parentId:1
        frontParen:null
        conjunction:And
        column:fund
        operator:is not null
        filter:null
        endParen[]
        Id:3
        parentId:1
        frontParen:null
        conjunction:And
        column:fund
        operator:<
        filter:Permitting
        endParen[)]
        Id:4
        parentId:0
        frontParen:null
        conjunction:And
        column:fund
        operator:<=
        filter:Permitting
        endParen[), ), )]
        */

        SelectStatement stmt = new SelectStatement();

        List<String> columns = new ArrayList<>();
        columns.add("fiscal_year_period");

        SortedSet<Criteria> criteria = new TreeSet<>();
        Criteria c0 = new Criteria(0);
        c0.parentId = null;
        c0.column = "fund";
        c0.operator = equalTo;
        c0.filter = "Permitting";
        criteria.add(c0);

        Criteria c1 = new Criteria(1);
        c1.parentId = 0;
        c1.conjunction = And;
        c1.column = "fund";
        c1.operator = greaterThan;
        c1.filter = "Permitting";
        criteria.add(c1);

        Criteria c2 = new Criteria(2);
        c2.parentId = 1;
        c2.conjunction = And;
        c2.column = "fund";
        c2.operator = isNotNull;
        c2.filter = null;
        criteria.add(c2);

        Criteria c3 = new Criteria(3);
        c3.parentId = 1;
        c3.conjunction = And;
        c3.column = "fund";
        c3.operator = lessThan;
        c3.filter = "Permitting";
        criteria.add(c3);

        Criteria c4 = new Criteria(4);
        c4.parentId = 0;
        c4.conjunction = And;
        c4.column = "fund";
        c4.operator = lessThanOrEquals;
        c4.filter = "Permitting";
        criteria.add(c4);

        stmt.setColumns(columns);
        stmt.setTable("county_spending_detail");
        stmt.setCriteria(criteria);
        stmt.setSuppressNulls(false);
        stmt.setGroupBy(false);
        stmt.setOrderBy(false);
        stmt.setAscending(false);
        stmt.setLimit(10);
        stmt.setOffset(1);
        stmt.setTableSchema(TestUtils.multiColumnResultSetBuilder(properties.get(0)));

        sqlBuilder = buildSqlBuilder(DatabaseType.PostgreSQL);
        String sql = sqlBuilder.buildSql(stmt);

        try {
            Properties props = properties.get(0);
            conn = new DbConnectionImpl(props);
            ResultSet rs = conn.execute(sql);
            assertTrue(true);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void runTests() throws Exception {
        for (int i=0; i<properties.size(); i++) {

            Properties props = properties.get(i);
            conn = new DbConnectionImpl(props);

            DatabaseType dbType = DatabaseType.valueOf(props.getProperty("databaseType"));
            sqlBuilder = buildSqlBuilder(dbType);
            sqlBuilder.tableSchema = TestUtils.multiColumnResultSetBuilder(properties.get(i));

            // run each public method that returns a ResultSet and test results.
            Method[] methods = QueryTests.class.getMethods();
            for (Method method : methods) {
                if (method.getGenericReturnType().equals(String.class) &&
                        Modifier.isPublic(method.getModifiers()) &&
                        method.getDeclaringClass().equals(QueryTests.class)) {
                    try {
                        String sql = (String) method.invoke(new QueryTests(sqlBuilder, props), null);
                        ResultSet rs = conn.execute(sql);

                        //If this line is reached, then we know the SQL statement was accepted by the database, which is what
                        //we are testing.
                        assertTrue(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

    }

    @Test
    public void runRandomizedTests() throws Exception {
        for (int i=0; i<properties.size(); i++) {
            Properties props = properties.get(i);
            conn = new DbConnectionImpl(props);

            DatabaseType dbType = DatabaseType.valueOf(props.getProperty("databaseType"));
            sqlBuilder = buildSqlBuilder(dbType);
            sqlBuilder.tableSchema = TestUtils.multiColumnResultSetBuilder(properties.get(i));

            // run SQL statement randomizer.
            QueryTests queryTests = new QueryTests(sqlBuilder, props);
            Map<SelectStatement, String> sqlMap = queryTests.buildSql_randomizer();

            for (SelectStatement stmt : sqlMap.keySet()) {
                try {
                    conn.execute(sqlMap.get(stmt));
                    assertTrue(true);
                } catch (Exception ex) {
                    System.out.println("STATEMENT COLUMNS:  " + stmt.getColumns());
                    System.out.println();
                    System.out.println();

                    System.out.println("STATEMENT CRITERIA:  ");
                    System.out.println();
                    for (Criteria criteria : stmt.getCriteria()) {
                        System.out.println("Id:" + criteria.getId());
                        System.out.println("parentId:" + criteria.parentId);
                        System.out.println("frontParen:" + criteria.frontParenthesis);
                        System.out.println("conjunction:" + criteria.conjunction);
                        System.out.println("column:" + criteria.column);
                        System.out.println("operator:" + criteria.operator);
                        System.out.println("filter:" + criteria.filter);
                        System.out.println("endParen" + criteria.endParenthesis);
                    }
                    System.out.println();
                    System.out.println();

                    System.out.println("SQL String:  " + sqlMap.get(stmt));
                    ex.printStackTrace();
                    assertTrue(false);
                }
            }
        }
    }

}