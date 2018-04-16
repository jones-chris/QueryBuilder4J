package com.querybuilder4j.dbconnection;

import org.junit.After;
import org.junit.Test;
import org.junit.BeforeClass;

import java.sql.ResultSet;
import java.util.Properties;

import static org.junit.Assert.*;


public class DbConnectionImplTest {
    public static Properties properties = new Properties();
    public static DbConnection dbConnection;

    @BeforeClass
    public static void setUp() throws Exception {
        properties.setProperty("url", "jdbc:postgresql://localhost:5432/postgres");
        properties.setProperty("username", "postgres");
        properties.setProperty("password", "budgeto");
        properties.setProperty("driverClass", "org.postgresql.Driver");
        dbConnection = new DbConnectionImpl(properties);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void readQuery() throws Exception {
        String sql = "select * from county_spending_detail limit 50;";

        ResultSet rs = dbConnection.execute(sql);

        rs.next(); //moves cursor to first row to prove that data was returned.
        assertTrue(rs.getRow() == 1);
    }

    @Test
    public void userSignIn() throws Exception {
        dbConnection.userSignIn();

        assertTrue(true);
    }

    @Test(expected = Exception.class)
    public void userSignInThrowsExceptionOnFail() throws Exception {
        Properties badProperties = new Properties();
        badProperties.setProperty("url", "jdbc:postgresql://localhost:5432/postgres");
        badProperties.setProperty("username", "postgres");
        badProperties.setProperty("password", "a_bad_password");
        badProperties.setProperty("driverClass", "org.postgresql.Driver");
        DbConnection badDbConnection = new DbConnectionImpl(badProperties);

        badDbConnection.userSignIn();
    }

    @Test
    public void getDbSchemas() throws Exception {

    }

    @Test
    public void getSchemaTables() throws Exception {

    }

    @Test
    public void getSchemaViews() throws Exception {

    }

    @Test
    public void getUserTables() throws Exception {

    }

    @Test
    public void getUserViews() throws Exception {

    }

    @Test
    public void getColumns() throws Exception {

    }

}