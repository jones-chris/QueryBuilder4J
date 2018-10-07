package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.Constants;
import com.querybuilder4j.TestUtils;
import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

import static org.junit.Assert.*;

public class SqlBuilderTest {
    private static SqlBuilder sqlBuilder;


    public SqlBuilderTest() { }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    @SuppressWarnings("unchecked")
    public void runStaticStatementTests() throws Exception {

        // run each public method that returns a Map and test that generated SQL String is run against the database without errors.
        Method[] methods = StaticStatementTests.class.getMethods();
        for (Method method : methods) {
            if (method.getGenericReturnType().getTypeName().equals("java.util.HashMap<java.lang.Object, java.lang.Object>") &&
                    Modifier.isPublic(method.getModifiers()) &&
                    method.getDeclaringClass().equals(StaticStatementTests.class)) {
                Connection conn = null;
                try {
                    Map<Object, Object> results = (Map<Object, Object>) method.invoke(new StaticStatementTests(), null);
                    DatabaseType dbType = ((SelectStatement) results.get("stmt")).getDatabaseType();
                    Properties props = Constants.dbProperties.get(dbType);
                    conn = TestUtils.getConnection(props);
                    String sql = (String) results.get("sql");
                    conn.createStatement().executeQuery(sql);

                    //If this line is reached, then we know the SQL statement was accepted by the database, which is what
                    //  we are testing.
                    assertTrue(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    assertTrue(false);
                } finally {
                    if (conn != null) {
                        conn.close();
                    }
                }
            }
        }

    }

    @Test
    public void runDynamicStatementTests() throws Exception {
        Set<DatabaseType> keys = Constants.dbProperties.keySet();
        for (DatabaseType dbType : keys) {
            Properties props = Constants.dbProperties.get(dbType);


            // run SQL statement randomizer.
            DynamicStatementTests queryTests = new DynamicStatementTests(dbType, props);
            Map<SelectStatement, String> sqlMap = queryTests.buildSql_randomizer();

            for (SelectStatement selectStatement : sqlMap.keySet()) {
                try (Connection conn = TestUtils.getConnection(props);
                     Statement stmt = conn.createStatement()) {

                    stmt.executeQuery(selectStatement.toSql(props));
                    assertTrue(true);
                } catch (Exception ex) {
                    System.out.println("Select Statement Object:  ");
                    System.out.println(selectStatement.toString());
                    System.out.println("\n");
                    System.out.println("SQL String:  ");
                    System.out.println(sqlMap.get(selectStatement));
                    ex.printStackTrace();
                    assertTrue(false);
                }
            }
        }
    }

}