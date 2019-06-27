package com.querybuilder4j.sqlbuilders;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.querybuilder4j.QueryTemplateDaoImpl;
import com.querybuilder4j.TestUtils;
import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.statements.SelectStatement;
import com.querybuilder4j.utils.SelectStatementFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

import static com.querybuilder4j.config.Operator.in;
import static org.junit.Assert.*;

public class SqlBuilderTest {
    private static Map<DatabaseType, Properties> testProperties = new HashMap<>();
    private static Map<DatabaseType, List<SelectStatement>> selectStatementsByDatabase = new HashMap<>();
    private static final int NUMBER_OF_SELECT_STATEMENTS_TO_GENERATE = 100;
    private static final String STATIC_TEST_FILE_PATH = "./src/test/resources/static-select-statement-json/%s";

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
    public static void setUpOnceBeforeAnyTestsAreRun() throws Exception {
        testProperties = getTestProperties();

        // Create randomly generated/dynamic SelectStatements.
        for (Properties properties : testProperties.values()) {

            DynamicStatementGenerator dynamicStatementGenerator = new DynamicStatementGenerator(properties,
                    NUMBER_OF_SELECT_STATEMENTS_TO_GENERATE);
            List<SelectStatement> dynamicSelectStatements = dynamicStatementGenerator.createRandomSelectStatements();
            DatabaseType databaseType = TestUtils.getDatabaseType(properties);
            selectStatementsByDatabase.put(databaseType, dynamicSelectStatements);
        }

        // Get static SelectStatements JSON by database type.
        Gson gson = new Gson();
        Set<String> dirs = new HashSet<>();

        testProperties.keySet().forEach((dbType) -> dirs.add(dbType.toString().toLowerCase()));
        for (String dir : dirs) {
            File dbTypeTestDirectory = new File(String.format(STATIC_TEST_FILE_PATH, dir));
            File[] jsonFiles = dbTypeTestDirectory.listFiles();
            if (jsonFiles != null) {
                for (File file : jsonFiles) {
                    FileReader fileReader = new FileReader(file);
                    System.out.println("Loading file at this path:  " + file.toString());
                    JsonElement jsonElement = new JsonParser().parse(fileReader);
                    SelectStatement selectStatement = gson.fromJson(jsonElement, SelectStatement.class);
                    Properties databaseProperties = testProperties.get(DatabaseType.Sqlite);
                    selectStatement.setQueryTemplateDao(new QueryTemplateDaoImpl(databaseProperties));
                    selectStatement.setDatabaseMetaData(databaseProperties); // todo:  don't hard code this to Sqlite.

                    // Add selectStatement based on selectStatement's database type.
                    selectStatementsByDatabase.get(selectStatement.getDatabaseMetaData().getDatabaseType()).add(selectStatement);
                }
            }
        }

        // Load static SelectStatements JSON to be run by all database types.
        File staticTestDirectory = new File(String.format(STATIC_TEST_FILE_PATH, "all-db"));
        for (File file : staticTestDirectory.listFiles()) {
            FileReader fileReader = new FileReader(file);
            System.out.println("Loading file at this path:  " + file.toString());
            JsonElement jsonElement = new JsonParser().parse(fileReader);

            // Add the the SelectStatement for each database type in properties file, so that it is tested for each database type.
            for (DatabaseType dbType : testProperties.keySet()) {
                SelectStatement selectStatement = gson.fromJson(jsonElement, SelectStatement.class);
                selectStatement.setDatabaseMetaData(testProperties.get(dbType));
//                selectStatement.setDatabaseType(dbType);
                Properties databaseProperties = testProperties.get(dbType);
                selectStatement.setQueryTemplateDao(new QueryTemplateDaoImpl(databaseProperties));

                // Add selectStatement based on selectStatement's database type.
                DatabaseType databaseType = selectStatement.getDatabaseMetaData().getDatabaseType();
                selectStatementsByDatabase.get(databaseType).add(selectStatement);
            }
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
    public void runSubQuery_noArgs() throws Exception {
        SelectStatement stmt = new SelectStatementFactory()
                .select("county_spending_detail.amount")
                .from("county_spending_detail")
                .where("county_spending_detail.department", in, "$getDepartmentsIn2014()")
                .setQueryTemplateDao(new QueryTemplateDaoImpl(testProperties.get(DatabaseType.Sqlite)))
                .getSelectStatement(DatabaseType.Sqlite);

        // Get properties.
        Properties props = getTestProperties().get(DatabaseType.Sqlite);

        // Test that SQL runs successfully against database.
        buildAndRunQuery(stmt, props);

        // After the SelectStatement is run, pass the test.
        assertTrue(true);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void runSubQuery_oneRegularArg() throws Exception {
        SelectStatement stmt = new SelectStatementFactory()
                .select("county_spending_detail.department")
                .from("county_spending_detail")
                .where("county_spending_detail.department", in, "$getDepartmentsByYear(year=2014)")
                .setQueryTemplateDao(new QueryTemplateDaoImpl(testProperties.get(DatabaseType.Sqlite)))
                .getSelectStatement(DatabaseType.Sqlite);

        // Get properties.
        Properties props = getTestProperties().get(DatabaseType.Sqlite);

        // Test that SQL runs successfully against database.
        buildAndRunQuery(stmt, props);

        // After the SelectStatement is run, pass the test.
        assertTrue(true);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void runSubQuery_oneSubQueryArg() throws Exception {
        SelectStatement stmt = new SelectStatementFactory()
                .select("county_spending_detail.amount")
                .from("county_spending_detail")
                .where("county_spending_detail.department", in, "$getDepartmentsByYear(year=$get2014FiscalYear())")
                .setQueryTemplateDao(new QueryTemplateDaoImpl(testProperties.get(DatabaseType.Sqlite)))
                .getSelectStatement(DatabaseType.Sqlite);

        // Get properties.
        Properties props = getTestProperties().get(DatabaseType.Sqlite);

        // Test that SQL runs successfully against database.
        buildAndRunQuery(stmt, props);

        // After the SelectStatement is run, pass the test.
        assertTrue(true);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void runSubQuery_oneRegularArgOneSubQuery() throws Exception {
        SelectStatement stmt = new SelectStatementFactory()
                .select("county_spending_detail.amount")
                .from("county_spending_detail")
                .where("county_spending_detail.department", in, "$getDepartmentsByMultipleYears(year1=$get2014FiscalYear();year2=2017)")
                .and("county_spending_detail.department", in, "$get2014FiscalYear()", null)
                .setQueryTemplateDao(new QueryTemplateDaoImpl(testProperties.get(DatabaseType.Sqlite)))
                .getSelectStatement(DatabaseType.Sqlite);

        // Get properties.
        Properties props = getTestProperties().get(DatabaseType.Sqlite);

        // Test that SQL runs successfully against database.
        buildAndRunQuery(stmt, props);

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
        for (DatabaseType dbType : selectStatementsByDatabase.keySet()) {
            Properties props = testProperties.get(dbType);
            List<SelectStatement> selectStatements = selectStatementsByDatabase.get(dbType);
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
            System.out.println(String.format("Running the SQL generated from the SelectStatement, %s, against %s:  %s",
                    (selectStatement.getName().equals("")) ? "!No Name!" : selectStatement.getName(),
                    props.getProperty("databaseType"),
                    sql));
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