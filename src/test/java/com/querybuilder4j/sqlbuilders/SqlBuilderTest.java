package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.QueryTests;
import com.querybuilder4j.TestUtils;
import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.config.SqlBuilderFactory;
import com.querybuilder4j.dbconnection.DbConnection;
import com.querybuilder4j.dbconnection.DbConnectionImpl;
import com.querybuilder4j.sqlbuilders.statements.Criteria;
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

        // Get properties files
//        try {
//            for (String fileName : propertiesFileNames) {
//                FileReader reader = new FileReader(fileName);
//                Properties props = new Properties();
//                props.load(reader);
//                properties.add(props);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void runTests() throws Exception {
        // Loop thru each properties files
        for (int i=0; i<properties.size(); i++) {

            Properties props = properties.get(i);
            conn = new DbConnectionImpl(props);

            DatabaseType dbType = DatabaseType.valueOf(props.getProperty("databaseType"));
            sqlBuilder = buildSqlBuilder(dbType);
            sqlBuilder.tableSchema = TestUtils.multiColumnResultSetBuilder(properties.get(i)).getMetaData();
//            sqlBuilder.typeMappings.put("text", true);
//            sqlBuilder.typeMappings.put("int2", false);

            // run each public method that returns a ResultSet and test results.
            Method[] methods = QueryTests.class.getMethods();
            for (Method method : methods) {
                if (method.getGenericReturnType().equals(String.class) &&
                        Modifier.isPublic(method.getModifiers()) &&
                        method.getDeclaringClass().equals(QueryTests.class)) {
                    String sql = (String) method.invoke(new QueryTests(sqlBuilder, props), null);
                    ResultSet rs = conn.execute(sql);

                    // move cursor to last record in ResultSet so that getRow() returns total number or records.
                    rs.last();

                    assertTrue(rs.getRow() > 1);
                }
            }
        }

    }

}