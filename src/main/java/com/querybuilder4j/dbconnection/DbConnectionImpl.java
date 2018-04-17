package com.querybuilder4j.dbconnection;


import java.sql.*;
import java.util.Properties;

import static java.sql.DriverManager.getConnection;

public class DbConnectionImpl implements DbConnection {
    private Properties properties;
    private Connection connection;
    private String driverClassName;


    public DbConnectionImpl(Properties properties) throws SQLException {
        this.properties = properties;
        this.driverClassName = properties.getProperty("driverClass");
        this.connection = getConnection(properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password"));
    }

    @Override
    public ResultSet execute(String sql) throws Exception {
        try {
            Class.forName(driverClassName);
            connection = getConnection(properties.getProperty("url"),
                    properties.getProperty("username"),
                    properties.getProperty("password"));
            Statement statement = connection.createStatement();
            return statement.executeQuery(sql);
        } finally {
            connection.close();
        }
    }

    @Override
    public void userSignIn() throws Exception {
        try {
            Class.forName(driverClassName);
            connection = getConnection(properties.getProperty("url"),
                    properties.getProperty("username"),
                    properties.getProperty("password"));
        } finally {
            connection.close();
        }
    }

    @Override
    public ResultSet getDbSchemas() throws SQLException {
//        String driverName = properties.getProperty("driverClass");
//        if (driverName.toLowerCase().contains("mysql")) {
            DatabaseMetaData metaData = connection.getMetaData();
            return metaData.getSchemas();
//        } else if (driverName.toLowerCase().contains("oracle")) {
//
//        } else if (driverName.toLowerCase().contains("postgresql")) {
//
//        } else if (driverName.toLowerCase().contains("redshift")) {
//
//        } else if (driverName.toLowerCase().contains("sqlserver")) {
//
//        } else if (driverName.toLowerCase().contains("sqlite")) {
//
//        }
    }

    @Override
    public ResultSet getSchemaTables(String schemaName) {
        return null;
    }

    @Override
    public ResultSet getSchemaViews(String schemaName) {
        return null;
    }

    @Override
    public ResultSet getUserTables(String username) {
        return null;
    }

    @Override
    public ResultSet getUserViews(String username) {
        return null;
    }

    @Override
    public ResultSet getColumns(String tableName) {
        return null;
    }
}
