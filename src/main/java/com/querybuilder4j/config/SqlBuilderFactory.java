package com.querybuilder4j.config;

import com.querybuilder4j.sqlbuilders.*;

public class SqlBuilderFactory {

    public static AbstractSqlBuilder buildSqlBuilder(DatabaseType databaseType) {
        AbstractSqlBuilder sqlBuilder;
        switch (databaseType) {
            case MySql:      sqlBuilder = new MySqlSqlBuilder();
                             break;
            case Oracle:     sqlBuilder = new OracleSqlBuilder();
                             break;
            case PostgreSQL: sqlBuilder = new PostgresSqlBuilder();
                             break;
            case Redshift:   sqlBuilder = new RedshiftSqlBuilder();
                             break;
            case SqlServer:  sqlBuilder = new SqlServerSqlBuilder();
                             break;
            case Sqlite:     sqlBuilder = new SqliteSqlBuilder();
                             break;
            default:         throw new RuntimeException(String.format("Database type, %s, not recognized", databaseType));
        }

        return sqlBuilder;
    }

}
