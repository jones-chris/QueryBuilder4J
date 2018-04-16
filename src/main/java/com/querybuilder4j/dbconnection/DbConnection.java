package com.querybuilder4j.dbconnection;


import java.sql.ResultSet;

public interface DbConnection {
    ResultSet execute(String sql) throws Exception;
    void userSignIn() throws Exception;
    ResultSet getDbSchemas();
    ResultSet getSchemaTables(String schemaName);
    ResultSet getSchemaViews(String schemaName);
    ResultSet getUserTables(String username);
    ResultSet getUserViews(String username);
    ResultSet getColumns(String tableName);
}
