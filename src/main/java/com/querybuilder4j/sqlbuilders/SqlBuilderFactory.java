package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.statements.DatabaseType;
import com.querybuilder4j.statements.SelectStatement;


public class SqlBuilderFactory {

    public static SqlBuilder buildSqlBuilder(SelectStatement stmt) throws Exception {
        SqlBuilder sqlBuilder;
        DatabaseType databaseType = stmt.getDatabaseMetaData().getDatabaseType();
        switch (databaseType) {
            case MySql:      sqlBuilder = new MySqlSqlBuilder(stmt);
                             break;
            case Oracle:     sqlBuilder = new OracleSqlBuilder(stmt);
                             break;
            case PostgreSQL: sqlBuilder = new PostgresSqlBuilder(stmt);
                             break;
            case Redshift:   sqlBuilder = new RedshiftSqlBuilder(stmt);
                             break;
            case SqlServer:  sqlBuilder = new SqlServerSqlBuilder(stmt);
                             break;
            case Sqlite:     sqlBuilder = new SqliteSqlBuilder(stmt);
                             break;
            default:         throw new RuntimeException(String.format("Database type, %s, not recognized", databaseType));
        }

        return sqlBuilder;
    }

}
