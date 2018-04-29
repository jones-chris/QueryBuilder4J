package com.querybuilder4j.dbconnection;


import java.sql.ResultSet;
import java.sql.SQLException;

public interface DbConnection {
    ResultSet execute(String sql) throws Exception;
    ResultSet getDbSchemas() throws SQLException;
    ResultSet getSchemaTables(String schemaName);
    ResultSet getSchemaViews(String schemaName);
    ResultSet getUserTables(String username);
    ResultSet getUserViews(String username);
    ResultSet getColumns(String tableName);
}
