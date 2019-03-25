package com.querybuilder4j.sqlbuilders;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.querybuilder4j.QueryTemplateDaoImpl;
import com.querybuilder4j.TestUtils;
import com.querybuilder4j.config.Conjunction;
import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.config.Operator;
import com.querybuilder4j.sqlbuilders.dao.QueryTemplateDao;
import com.querybuilder4j.sqlbuilders.statements.Criteria;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

import static org.junit.Assert.*;

public class SqlBuilderTest {
    private static QueryTemplateDao queryTemplateDao = new QueryTemplateDaoImpl();
    private static Map<DatabaseType, Properties> testProperties = new HashMap<>();
    private static Map<DatabaseType, List<SelectStatement>> dynamicSelectStatements = new HashMap<>();
    private static List<String> staticSelectStatementsJSON = new ArrayList<>();
    private static final int NUMBER_OF_SELECT_STATEMENTS_TO_GENERATE = 1000;
    private static final String STATIC_TEST_FILE_PATH = "./src/test/resources/static-select-statement-json";

    public SqlBuilderTest() { }

    /**
     * This method runs just once before any tests are run.  This method gets the properties for each database type so
     * that tests can be run against each database detailed in the properties.
     *
     * It also randomly generates SelectStatements for each database type.
     *
     * @throws Exception
     */
    @BeforeClass
    public static void SetUpOnceBeforeAnyTestsAreRun() throws Exception {
        testProperties = getTestProperties();

        // Create randomly generated/dynamic SelectStatements.
        for (DatabaseType dbType : testProperties.keySet()) {
            DynamicStatementGenerator dynamicStatementGenerator = new DynamicStatementGenerator(dbType,
                    NUMBER_OF_SELECT_STATEMENTS_TO_GENERATE);
            List<SelectStatement> selectStatements = dynamicStatementGenerator.createRandomSelectStatements();
            dynamicSelectStatements.put(dbType, selectStatements);
        }

        // Load static SelectStatements JSON for regression testing.
        File staticTestDirectory = new File(STATIC_TEST_FILE_PATH);
        for (File file : staticTestDirectory.listFiles()) {
            FileReader fileReader = new FileReader(file);
            JsonElement jsonElement = new JsonParser().parse(fileReader);
            staticSelectStatementsJSON.add(jsonElement.toString());
        }
    }

    /**
     * This method runs before each test method is run.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {

    }

    /**
     * This method runs after each test method is run.
     *
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {

    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void runStaticStatementTests() throws Exception {
        for (String selectStatementJSON : staticSelectStatementsJSON) {
//            Gson gson = new Gson();
//            SelectStatement selectStatement = gson.fromJson(selectStatementJSON, SelectStatement.class);
            ObjectMapper mapper = new ObjectMapper();
            SelectStatement selectStatement = mapper.readValue(selectStatementJSON, SelectStatement.class);

            // Run the SelectStatement against each database in testProperties.
            for (DatabaseType dbType : testProperties.keySet()) {
                selectStatement.setDatabaseType(dbType);
                Properties props = testProperties.get(dbType);
                String sql = "If you see this then the SelectStatement has not been built into a SQL string yet";
                try {
                    buildAndRunQuery(selectStatement, props);
                } catch (Exception ex) {
                    throw createDetailedQb4jException(selectStatement, sql, ex);
                }
            }

        }

        // After all SelectStatements are run, pass the test.
        assertTrue(true);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void runSubQuery_noArgs() throws Exception {
        // create root statement
        Criteria criteria = new Criteria();
        criteria.setId(0);
        criteria.setConjunction(Conjunction.And);
        criteria.setColumn("county_spending_detail.department");
        criteria.setOperator(Operator.in);
        criteria.setFilter("$0");
//        criteria.setFilter("$getDepartmentsIn2014()");
        SelectStatement rootStmt = new SelectStatement(DatabaseType.Sqlite);
        rootStmt.getColumns().add("county_spending_detail.amount");
        rootStmt.setTable("county_spending_detail");
        rootStmt.getCriteria().add(criteria);
        rootStmt.getSubQueries().put("$0", "getDepartmentsIn2014()");
        rootStmt.setQueryTemplateDao(queryTemplateDao);

        // Get properties.
        Properties props = getTestProperties().get(DatabaseType.Sqlite);

        // Test that SQL runs successfully against database.
        buildAndRunQuery(rootStmt, props);

        // After the SelectStatement is run, pass the test.
        assertTrue(true);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void runSubQuery_oneRegularArg() throws Exception {
        // create root statement
        Criteria criteria = new Criteria();
        criteria.setId(0);
        criteria.setConjunction(Conjunction.And);
        criteria.setColumn("county_spending_detail.department");
        criteria.setOperator(Operator.in);
//        criteria.setFilter("$getDepartmentsByYear(year=2014)");
        criteria.setFilter("$0");
        SelectStatement rootStmt = new SelectStatement(DatabaseType.Sqlite);
        rootStmt.getColumns().add("county_spending_detail.amount");
        rootStmt.setTable("county_spending_detail");
        rootStmt.getCriteria().add(criteria);
        rootStmt.getSubQueries().put("$0", "getDepartmentsByYear(year=2014)");
        rootStmt.setQueryTemplateDao(queryTemplateDao);

        // Get properties.
        Properties props = getTestProperties().get(DatabaseType.Sqlite);

        // Test that SQL runs successfully against database.
        buildAndRunQuery(rootStmt, props);

        // After the SelectStatement is run, pass the test.
        assertTrue(true);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void runSubQuery_oneSubQueryArg() throws Exception {
        // Create root statement
        Criteria criteria = new Criteria();
        criteria.setId(0);
        criteria.setConjunction(Conjunction.And);
        criteria.setColumn("county_spending_detail.department");
        criteria.setOperator(Operator.in);
        criteria.setFilter("$0");
//        criteria.setFilter("$getDepartmentsByYear(year=$get2014FiscalYear())");
        SelectStatement rootStmt = new SelectStatement(DatabaseType.Sqlite);
        rootStmt.getColumns().add("county_spending_detail.amount");
        rootStmt.setTable("county_spending_detail");
        rootStmt.getCriteria().add(criteria);
        rootStmt.getSubQueries().put("$0", "getDepartmentsByYear(year=$1)");
        rootStmt.getSubQueries().put("$1", "get2014FiscalYear()");
        rootStmt.setQueryTemplateDao(queryTemplateDao);

        // Get properties.
        Properties props = getTestProperties().get(DatabaseType.Sqlite);

        // Test that SQL runs successfully against database.
        buildAndRunQuery(rootStmt, props);

        // After the SelectStatement is run, pass the test.
        assertTrue(true);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void runSubQuery_oneRegularArgOneSubQuery() throws Exception {
        // Create root statement
        Criteria criteria = new Criteria();
        criteria.setId(0);
        criteria.setConjunction(Conjunction.And);
        criteria.setColumn("county_spending_detail.department");
        criteria.setOperator(Operator.in);
        criteria.setFilter("$0");
//        criteria.setFilter("$getDepartmentsByMultipleYears(year1=$get2014FiscalYear();year2=2017)");

        Criteria criteria1 = new Criteria();
        criteria1.setId(1);
        criteria1.setConjunction(Conjunction.And);
        criteria1.setColumn("county_spending_detail.department");
        criteria1.setOperator(Operator.in);
        criteria1.setFilter("$1");
//        criteria.setFilter("$get2014FiscalYear()");

        SelectStatement rootStmt = new SelectStatement(DatabaseType.Sqlite);
        rootStmt.getColumns().add("county_spending_detail.amount");
        rootStmt.setTable("county_spending_detail");
        rootStmt.getCriteria().add(criteria);
        rootStmt.getCriteria().add(criteria1);
        rootStmt.getSubQueries().put("$0", "getDepartmentsByMultipleYears(year1=$1,year2=2017)");
        rootStmt.getSubQueries().put("$1", "get2014FiscalYear()");
        rootStmt.setQueryTemplateDao(queryTemplateDao);

        // Get properties.
        Properties props = getTestProperties().get(DatabaseType.Sqlite);

        // Test that SQL runs successfully against database.
        buildAndRunQuery(rootStmt, props);

        // After the SelectStatement is run, pass the test.
        assertTrue(true);
    }

    /**
     * Builds the dynamic/randomly-generated SelectStatements into SQL strings and then runs them each against the
     * database using the database information in the Properties object.
     *
     * @throws Exception
     */
    @Test
    public void runDynamicStatementTests() throws Exception {
        for (DatabaseType dbType : testProperties.keySet()) {
            Properties props = testProperties.get(dbType);
            List<SelectStatement> selectStatements = dynamicSelectStatements.get(dbType);
            for (SelectStatement selectStatement : selectStatements) {
                buildAndRunQuery(selectStatement, props);
            }
        }

        // After all SelectStatements are run, pass the test.
        assertTrue(true);
    }

    /**
     * Creates an Exception with a detailed message while also preserving the Throwable ex parameter that is passed to
     * the function.
     *
     * The sql parameter will be null if the SelectStatement could not be built into a SQL string.  The sql parameter will
     * NOT be null if the SelectStatement could be built into a SQL string, but it failed to run successfully against the
     * database.
     *
     * @param selectStatement
     * @param sql
     * @param ex
     * @return
     */
    private Exception createDetailedQb4jException(SelectStatement selectStatement, String sql, Throwable ex) {
        String exceptionMessage = "Select Statement Object:  " + selectStatement.toString() + "\n" +
                                  "Generated SQL:  " + sql + "\n";
        return new Exception(exceptionMessage, ex);
    }

    /**
     * Builds and runs the SelectStatement against the database in the props parameter.
     *
     * @param selectStatement
     * @param props
     * @throws Exception
     */
    private void buildAndRunQuery(SelectStatement selectStatement, Properties props) throws Exception {
        String sql = "If you see this then the SelectStatement has not been built into a SQL string yet";
        try (Connection conn = TestUtils.getConnection(props);
             Statement stmt = conn.createStatement()) {

            sql = selectStatement.toSql(props);
            System.out.println(sql); //todo:  remove?
            stmt.executeQuery(sql);
        } catch (Exception ex) {
            throw createDetailedQb4jException(selectStatement, sql, ex);
        }
    }

    /**
     * Gets the Properties file containing the database connection information that will be used to execute SQL strings.
     *
     * @return
     * @throws IOException
     */
    private static Map<DatabaseType, Properties> getTestProperties() throws IOException {
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