package com.querybuilder4j;

import com.querybuilder4j.config.DatabaseType;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Constants {

    public static final Map<DatabaseType, Properties> dbProperties = new HashMap<DatabaseType, Properties>() {
        {
            Properties postgresqlProps = new Properties();
            postgresqlProps.setProperty("url", "jdbc:postgresql://localhost:5432/postgres");
            postgresqlProps.setProperty("username", "postgres");
            postgresqlProps.setProperty("password", "budgeto");
            postgresqlProps.setProperty("driverClass", "org.postgresql.Driver");
            postgresqlProps.setProperty("databaseType", "PostgreSQL");
            put(DatabaseType.PostgreSQL, postgresqlProps);

            Properties mysqlProps = new Properties();
            mysqlProps.setProperty("url", "jdbc:mysql://localhost:3306/sys");
            mysqlProps.setProperty("username", "root");
            mysqlProps.setProperty("password", "budgeto");
            mysqlProps.setProperty("driverClass", "com.mysql.cj.jdbc.Driver");
            mysqlProps.setProperty("databaseType", "MySql");
            put(DatabaseType.MySql, mysqlProps);

            Properties sqliteProps = new Properties();
            sqliteProps.setProperty("url", "jdbc:sqlite:C:/Users/Public/Repos/tests/QueryBuilder4JMVC/QueryBuilder4JMVC/data/querybuilder4j.db");
            sqliteProps.setProperty("driverClass", "org.sqlite.JDBC");
            sqliteProps.setProperty("databaseType", "Sqlite");
            put(DatabaseType.Sqlite, sqliteProps);

//            Properties sqlserverProps = new Properties();
//            sqlserverProps.setProperty("url", "jdbc:sqlserver://localhost;databaseName=master;integratedSecurity=true;");
//            //sqlserverProps.setProperty("username", "root"); //TODO:  may not need this.  check local db.
//            //sqlserverProps.setProperty("password", "budgeto");
//            sqlserverProps.setProperty("driverClass", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
//            sqlserverProps.setProperty("databaseType", "SqlServer");
//            put(DatabaseType.SqlServer, sqlserverProps);

            // add oracle properties.

            // add sqlite properties.
        }
    };

}
