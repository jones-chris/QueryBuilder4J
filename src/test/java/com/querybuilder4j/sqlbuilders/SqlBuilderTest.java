package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.TestUtils;
import com.querybuilder4j.config.Conjunction;
import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.config.Operator;
import com.querybuilder4j.sqlbuilders.dao.QueryTemplateDao;
import com.querybuilder4j.sqlbuilders.statements.Criteria;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

import static org.junit.Assert.*;

public class SqlBuilderTest {
    private static SqlBuilder sqlBuilder;
    private static QueryTemplateDao queryTemplateDao = new QueryTemplateDaoImpl();


    public SqlBuilderTest() { }

    @Before
    public void setUp() throws Exception {
        
    }

    @After
    public void tearDown() throws Exception {

    }

//    @Test
//    @SuppressWarnings("unchecked")
//    public void runStaticStatementTests() throws Exception {
//
//        // run each public method that returns a Map and test that generated SQL String is run against the database without errors.
//        Method[] methods = StaticStatementTests.class.getMethods();
//        for (Method method : methods) {
//            if (method.getGenericReturnType().getTypeName().equals("java.util.HashMap<java.lang.Object, java.lang.Object>") &&
//                    Modifier.isPublic(method.getModifiers()) &&
//                    method.getDeclaringClass().equals(StaticStatementTests.class)) {
//                Connection conn = null;
//                try {
//                    Map<Object, Object> results = (Map<Object, Object>) method.invoke(new StaticStatementTests(), null);
//                    DatabaseType dbType = ((SelectStatement) results.get("stmt")).getDatabaseType();
//                    Properties props = Constants.dbProperties.get(dbType);
//                    conn = TestUtils.getConnection(props);
//                    String sql = (String) results.get("sql");
//                    conn.createStatement().executeQuery(sql);
//
//                    //If this line is reached, then we know the SQL statement was accepted by the database, which is what
//                    //  we are testing.
//                    assertTrue(true);
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                    fail();
//                } finally {
//                    if (conn != null) {
//                        conn.close();
//                    }
//                }
//            }
//        }
//
//    }

    @Test
    public void runSubQuery_noArgs() throws Exception {
        // create root statement
        Criteria criteria = new Criteria();
        criteria.setId(0);
        criteria.setConjunction(Conjunction.And);
        criteria.setColumn("county_spending_detail.department");
        criteria.setOperator(Operator.in);
        criteria.setFilter("subquery0");
        SelectStatement rootStmt = new SelectStatement(DatabaseType.Sqlite);
        rootStmt.getColumns().add("county_spending_detail.amount");
        rootStmt.setTable("county_spending_detail");
        rootStmt.getCriteria().add(criteria);
        rootStmt.getSubQueries().put("subquery0", "getDepartmentsIn2014()");
        rootStmt.setQueryTemplateDao(queryTemplateDao);

        // Call toSql()
        Properties props = getTestProperties().get(DatabaseType.Sqlite);
        String sql = rootStmt.toSql(props);

        // Test that SQL runs successfully against database.
        testIfSqlExecutesSuccessfully(sql, rootStmt, props);
    }

    @Test
    public void runSubQuery_oneRegularArg() throws Exception {
        // create root statement
        Criteria criteria = new Criteria();
        criteria.setId(0);
        criteria.setConjunction(Conjunction.And);
        criteria.setColumn("county_spending_detail.department");
        criteria.setOperator(Operator.in);
        criteria.setFilter("subquery0");
        SelectStatement rootStmt = new SelectStatement(DatabaseType.Sqlite);
        rootStmt.getColumns().add("county_spending_detail.amount");
        rootStmt.setTable("county_spending_detail");
        rootStmt.getCriteria().add(criteria);
        rootStmt.getSubQueries().put("subquery0", "getDepartmentsByYear(year=2014)");
        rootStmt.setQueryTemplateDao(queryTemplateDao);

        // Call toSql()
        Properties props = getTestProperties().get(DatabaseType.Sqlite);
        String sql = rootStmt.toSql(props);

        // Test that SQL runs successfully against database.
        testIfSqlExecutesSuccessfully(sql, rootStmt, props);
    }

    @Test
    public void runSubQuery_oneSubQueryArg() throws Exception {
        // Create root statement
        Criteria criteria = new Criteria();
        criteria.setId(0);
        criteria.setConjunction(Conjunction.And);
        criteria.setColumn("county_spending_detail.department");
        criteria.setOperator(Operator.in);
        criteria.setFilter("subquery0");
        SelectStatement rootStmt = new SelectStatement(DatabaseType.Sqlite);
        rootStmt.getColumns().add("county_spending_detail.amount");
        rootStmt.setTable("county_spending_detail");
        rootStmt.getCriteria().add(criteria);
        rootStmt.getSubQueries().put("subquery0", "getDepartmentsByYear(year=subquery1)");
        rootStmt.getSubQueries().put("subquery1", "get2014FiscalYear()");
        rootStmt.setQueryTemplateDao(queryTemplateDao);

        // Call toSql()
        Properties props = getTestProperties().get(DatabaseType.Sqlite);
        String sql = rootStmt.toSql(props);

        // Test that SQL runs successfully against database.
        testIfSqlExecutesSuccessfully(sql, rootStmt, props);
    }

    @Test
    public void runSubQuery_oneRegularArgOneSubQuery() throws Exception {
        // Create root statement
        Criteria criteria = new Criteria();
        criteria.setId(0);
        criteria.setConjunction(Conjunction.And);
        criteria.setColumn("county_spending_detail.department");
        criteria.setOperator(Operator.in);
        criteria.setFilter("subquery0");

        Criteria criteria1 = new Criteria();
        criteria1.setId(1);
        criteria1.setConjunction(Conjunction.And);
        criteria1.setColumn("county_spending_detail.department");
        criteria1.setOperator(Operator.in);
        criteria1.setFilter("subquery1");

        SelectStatement rootStmt = new SelectStatement(DatabaseType.Sqlite);
        rootStmt.getColumns().add("county_spending_detail.amount");
        rootStmt.setTable("county_spending_detail");
        rootStmt.getCriteria().add(criteria);
        rootStmt.getCriteria().add(criteria1);
        rootStmt.getSubQueries().put("subquery0", "getDepartmentsByMultipleYears(year1=subquery1,year2=2017)");
        rootStmt.getSubQueries().put("subquery1", "get2014FiscalYear()");
        rootStmt.setQueryTemplateDao(queryTemplateDao);

        // Call toSql()
        Properties props = getTestProperties().get(DatabaseType.Sqlite);
        String sql = rootStmt.toSql(props);

        // Test that SQL runs successfully against database.
        testIfSqlExecutesSuccessfully(sql, rootStmt, props);
    }


    @Test
    public void runDynamicStatementTests() throws Exception {
        Map<DatabaseType, Properties> testProperties = getTestProperties();
        for (DatabaseType dbType : testProperties.keySet()) {
            Properties props = testProperties.get(dbType);

            // Run SQL statement randomizer.
            DynamicStatementTests queryTests = new DynamicStatementTests(dbType, props);
            Map<SelectStatement, String> sqlMap = new HashMap<>();
            try {
                sqlMap = queryTests.buildSql_randomizer();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                ex.printStackTrace();
                fail();
            }

            for (SelectStatement selectStatement : sqlMap.keySet()) {
                String sql = selectStatement.toSql(props);
                testIfSqlExecutesSuccessfully(sql, selectStatement, props);
            }
        }
    }

    private void testIfSqlExecutesSuccessfully(String sql, SelectStatement selectStatement, Properties props) {
        try (Connection conn = TestUtils.getConnection(props);
             Statement stmt = conn.createStatement()) {

            stmt.executeQuery(sql);
            assertTrue(true);
        } catch (Exception ex) {
            System.out.println("Select Statement Object:  " + selectStatement.toString() + "\n");
            System.out.println("Generated SQL:  " + sql + "\n");
            fail();
        }
    }

    private Map<DatabaseType, Properties> getTestProperties() throws IOException {
        // If the 'testProperties' command line argument does not exist, then run tests using the default:  test-config.properties.
        String testPropertiesFilePath = System.getProperty("testProperties");
        if (testPropertiesFilePath == null || testPropertiesFilePath.equals("")) {
            testPropertiesFilePath = "./src/test/resources/test-config.properties";
        }

        // Get all database type properties into one Properties object.
        FileReader reader = new FileReader(testPropertiesFilePath);
        Properties allTestProperties = new Properties();
        allTestProperties.load(reader);

        // Check that all properties have a non-null value.
        allTestProperties.values().forEach((property) -> {
            if (property == null) {
                throw new NullPointerException("The property has a null value.  All properties must have a value " +
                        "in the test properties file.  Please ensure that there are no comments in the properties file, also.");
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

                if (username != null) {
                    dbProps.setProperty("username", username.toString());
                }

                if (password != null) {
                    dbProps.setProperty("password", password.toString());
                }

                props.put(dbType, dbProps);
            }
        }

        return props;
    }

}