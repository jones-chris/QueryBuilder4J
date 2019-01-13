package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.Constants;
import com.querybuilder4j.TestUtils;
import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
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
                    fail();
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
        Map<DatabaseType, Properties> testProperties = getTestProperties();
        for (DatabaseType dbType : testProperties.keySet()) {
            Properties props = testProperties.get(dbType);

            // Run SQL statement randomizer.
            DynamicStatementTests queryTests = new DynamicStatementTests(dbType, props);
            Map<SelectStatement, String> sqlMap = queryTests.buildSql_randomizer();

            for (SelectStatement selectStatement : sqlMap.keySet()) {
                try (Connection conn = TestUtils.getConnection(props);
                     Statement stmt = conn.createStatement()) {

                    String sql = selectStatement.toSql(props);
                    stmt.executeQuery(sql);
                    assertTrue(true);
                } catch (Exception ex) {
                    System.out.println("Select Statement Object:  ");
                    System.out.println(selectStatement.toString());
                    System.out.println("\n");
                    System.out.println("SQL String:  ");
                    System.out.println(sqlMap.get(selectStatement));
                    ex.printStackTrace();
                    fail();
                }
            }
        }
    }

    private Map<DatabaseType, Properties> getTestProperties() throws IOException {
        // If the 'testProperties' command line argument does not exist, then run tests using the default:  test-config.properties.
        String testPropertiesFilePath = System.getProperty("testProperties");
        if (testPropertiesFilePath == null) {
            testPropertiesFilePath = "./src/test/resources/test-config.properties";
        }

        // Get all database type properties into one Properties object.
        FileReader reader = new FileReader(testPropertiesFilePath);
        Properties allTestProperties = new Properties();
        allTestProperties.load(reader);

        // Check that all properties have a non-null value.
        allTestProperties.values().forEach((property) -> {
            if (property == null) {
                throw new NullPointerException(String.format("The property, %s, has a null value.  All properties must have a value " +
                        "in the test properties file.", property));
            }
        });

        // Get each database type's properties.
        Map<DatabaseType, Properties> props = new HashMap<>();
        Object url;
        Object driverClass;
        Object databaseType;
        Object username;
        Object password;

        // Get each DatabaseType's properties if they exist in test properties file.
        for (DatabaseType dbType : DatabaseType.values()) {
            String dbTypeString = dbType.toString().toLowerCase();
            Properties dbProps = new Properties();

            url = allTestProperties.get(dbTypeString + ".url");
            driverClass = allTestProperties.get(dbTypeString + ".driverClass");
            databaseType = allTestProperties.get(dbTypeString + ".databaseType");
            username = allTestProperties.get(dbTypeString + ".username");
            password = allTestProperties.get(dbTypeString + ".password");

            if (url != null && driverClass != null && databaseType != null) {
                dbProps.setProperty("url", url.toString());
                dbProps.setProperty("driverClass", driverClass.toString());
                dbProps.setProperty("databaseType", databaseType.toString());

                if (username != null && password != null) {
                    dbProps.setProperty("username", username.toString());
                    dbProps.setProperty("password", password.toString());
                }

                props.put(dbType, dbProps);
            }
        }

        return props;
    }

}