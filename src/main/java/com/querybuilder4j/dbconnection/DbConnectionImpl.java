package com.querybuilder4j.dbconnection;


import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.sqlbuilders.SqlBuilder;

import java.sql.*;
import java.util.Properties;


public class DbConnectionImpl implements DbConnection {
    private Properties properties;
    private final Connection connection;
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

    public Connection getConnection() {
        return connection;
    }

    @Override
    public ResultSet execute(String sql) throws Exception {
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            return statement.executeQuery(sql);
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

    @Override
    public ResultSet getUserReadTables(String username, DatabaseType databaseType) throws Exception {
        String sql = SqlBuilder.readTablesSql.get(databaseType);
        return execute(sql);
    }

    @Override
    public ResultSet getUserWriteTables(String username, DatabaseType databaseType) throws Exception {
        String sql = SqlBuilder.writeTablesSql.get(databaseType);
        return execute(sql);
    }

    @Override
    public ResultSet getColumns(String schemaName, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        return metaData.getColumns(null, schemaName, tableName, null); //TODO:  change columnPattern param to "%" otherwise MySQL will throw exception with null argument.
    }
}
