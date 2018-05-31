package com.querybuilder4j.dbconnection;


import com.querybuilder4j.config.DatabaseType;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface DbConnection {
    ResultSet execute(String sql) throws Exception;
    String[] getDbSchemas() throws SQLException;
    ResultSet getSchemaTables(String schemaName) throws SQLException;
//    ResultSet getSchemaViews(String schemaName);
    ResultSet getUserReadTables(String username, DatabaseType databaseType) throws Exception;
//    ResultSet getUserReadViews(String username, DatabaseType databaseType);
    ResultSet getUserWriteTables(String username, DatabaseType databaseType) throws Exception;
//    ResultSet getUserWriteViews(String username, DatabaseType databaseType);
    ResultSet getColumns(String schemaName, String tableName) throws SQLException;
}
