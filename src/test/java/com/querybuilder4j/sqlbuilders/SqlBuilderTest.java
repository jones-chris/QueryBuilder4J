package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.Constants;
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
        Set<DatabaseType> keys = Constants.dbProperties.keySet();
        for (DatabaseType dbType : keys) {
            Properties props = Constants.dbProperties.get(dbType);
            conn = new DbConnectionImpl(props);

            DatabaseType dbTypeProp = DatabaseType.valueOf(props.getProperty("databaseType")); //Although redundant, the database type will no necessarily be known at runtime, so it's good to assume it's not given.
            sqlBuilder = buildSqlBuilder(dbType);
            sqlBuilder.tableSchema = TestUtils.multiColumnResultSetBuilder(props);

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

                    System.out.println("DATABASE TYPE:  " + dbType);

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