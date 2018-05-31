package com.querybuilder4j.dbconnection;


import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.sqlbuilders.SqlBuilder;

import java.sql.*;
import java.util.Properties;


public class DbConnectionImpl implements DbConnection {
    private Properties properties;
    private Connection connection;
    private String driverClassName;


    public DbConnectionImpl(Properties properties) throws SQLException {
        this.properties = properties;

        String username = properties.getProperty("username");
        String password = properties.getProperty("password");

        this.driverClassName = properties.getProperty("driverClass");
        this.connection = DriverManager.getConnection(properties.getProperty("url"),
                (username != null) ? username : null,
                (password != null) ? password : null);
    }

    @Override
    public ResultSet execute(String sql) throws Exception {
        try {
//            Class.forName(driverClassName);
//            connection = DriverManager.getConnection(properties.getProperty("url"),
//                    properties.getProperty("username"),
//                    properties.getProperty("password"));
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            return statement.executeQuery(sql);
        } finally {
            connection.close();
        }
    }

    @Override
    public String[] getDbSchemas() throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();

        ResultSet rs = metaData.getSchemas();

        rs.last();
        int numOfRows = rs.getRow();
        String[] data = new String[numOfRows];

        rs.beforeFirst();
        int i = 0;
        while (rs.next()) {
            data[i] = rs.getString("TABLE_SCHEM");
            i++;
        }

        return data;
    }

    @Override
    public ResultSet getSchemaTables(String schemaName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        return metaData.getTables(null, schemaName, null, null);
    }

//    @Override
//    public ResultSet getSchemaViews(String schemaName) {
//        return null;
//    }

    @Override
    public ResultSet getUserReadTables(String username, DatabaseType databaseType) throws Exception {
        String sql = SqlBuilder.readTablesSql.get(databaseType);
        return execute(sql);
    }

//    @Override
//    public ResultSet getUserReadViews(String username, DatabaseType databaseType) {
//        return null;
//    }

    @Override
    public ResultSet getUserWriteTables(String username, DatabaseType databaseType) throws Exception {
        String sql = SqlBuilder.writeTablesSql.get(databaseType);
        return execute(sql);
    }

//    @Override
//    public ResultSet getUserWriteViews(String username, DatabaseType databaseType) {
//        return null;
//    }

    @Override
    public ResultSet getColumns(String schemaName, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        return metaData.getColumns(null, schemaName, tableName, null);
    }
}
