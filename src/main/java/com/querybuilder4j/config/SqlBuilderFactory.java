package com.querybuilder4j.config;

import com.querybuilder4j.sqlbuilders.*;
import com.querybuilder4j.statements.SelectStatement;

import java.util.Properties;

public class SqlBuilderFactory {

    public static SqlBuilder buildSqlBuilder(DatabaseType databaseType, SelectStatement stmt, Properties properties) throws Exception {
        SqlBuilder sqlBuilder;
        switch (databaseType) {
            case MySql:      sqlBuilder = new MySqlSqlBuilder(stmt, properties);
                             break;
            case Oracle:     sqlBuilder = new OracleSqlBuilder(stmt, properties);
                             break;
            case PostgreSQL: sqlBuilder = new PostgresSqlBuilder(stmt, properties);
                             break;
            case Redshift:   sqlBuilder = new RedshiftSqlBuilder(stmt, properties);
                             break;
            case SqlServer:  sqlBuilder = new SqlServerSqlBuilder(stmt, properties);
                             break;
            case Sqlite:     sqlBuilder = new SqliteSqlBuilder(stmt, properties);
                             break;
            default:         throw new RuntimeException(String.format("Database type, %s, not recognized", databaseType));
        }

        return sqlBuilder;
    }

}
