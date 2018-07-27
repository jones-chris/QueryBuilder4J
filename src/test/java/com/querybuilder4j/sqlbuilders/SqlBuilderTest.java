package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.Constants;
import com.querybuilder4j.QueryTests;
import com.querybuilder4j.TestUtils;
import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.sqlbuilders.statements.Criteria;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

import static org.junit.Assert.*;

public class SqlBuilderTest {
    private static SqlBuilder sqlBuilder;
    private static Connection conn;


    public SqlBuilderTest() { }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void runTests() throws Exception {
        Set<DatabaseType> keys = Constants.dbProperties.keySet();
        for (DatabaseType dbType : keys) {

            Properties props = Constants.dbProperties.get(dbType);
            conn = TestUtils.getConnection(props);

            // run each public method that returns a ResultSet and test results.
            Method[] methods = QueryTests.class.getMethods();
            for (Method method : methods) {
                if (method.getGenericReturnType().equals(String.class) &&
                        Modifier.isPublic(method.getModifiers()) &&
                        method.getDeclaringClass().equals(QueryTests.class)) {
                    try {
                        String sql = (String) method.invoke(new QueryTests(dbType, props), null);
                        ResultSet rs = conn.createStatement().executeQuery(sql);

                        //If this line is reached, then we know the SQL statement was accepted by the database, which is what
                        //we are testing.
                        assertTrue(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        assertTrue(false);
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
            conn = TestUtils.getConnection(props);

            // run SQL statement randomizer.
            QueryTests queryTests = new QueryTests(dbType, props);
            Map<SelectStatement, String> sqlMap = queryTests.buildSql_randomizer();

            for (SelectStatement selectStatement : sqlMap.keySet()) {
                try {
                    Statement stmt = conn.createStatement();
                    stmt.executeQuery(selectStatement.toSql());
                    stmt.close();
                    assertTrue(true);
                } catch (Exception ex) {
                    System.out.println("STATEMENT COLUMNS:  " + selectStatement.getColumns());
                    System.out.println();
                    System.out.println();

                    System.out.println("DATABASE TYPE:  " + dbType);

                    System.out.println("STATEMENT CRITERIA:  ");
                    System.out.println();
                    for (Criteria criteria : selectStatement.getCriteria()) {
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

                    System.out.println("SQL String:  " + sqlMap.get(selectStatement));
                    ex.printStackTrace();
                    assertTrue(false);
                }
            }
        }
    }

}