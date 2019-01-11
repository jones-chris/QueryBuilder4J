package com.querybuilder4j.config;

import com.querybuilder4j.sqlbuilders.*;
import com.querybuilder4j.sqlbuilders.dao.QueryTemplateDao;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;

import java.util.Map;
import java.util.Properties;

public class SqlBuilderFactory {

    public static SqlBuilder buildSqlBuilder(DatabaseType databaseType, SelectStatement stmt,
                                             Map<String, String> subQueries, Properties properties,
                                             QueryTemplateDao queryTemplateDao) throws Exception {
        SqlBuilder sqlBuilder;
        switch (databaseType) {
            case MySql:      sqlBuilder = new MySqlSqlBuilder(stmt, subQueries, properties, queryTemplateDao);
                             break;
            case Oracle:     sqlBuilder = new OracleSqlBuilder(stmt, subQueries, properties, queryTemplateDao);
                             break;
            case PostgreSQL: sqlBuilder = new PostgresSqlBuilder(stmt, subQueries, properties, queryTemplateDao);
                             break;
            case Redshift:   sqlBuilder = new RedshiftSqlBuilder(stmt, subQueries, properties, queryTemplateDao);
                             break;
            case SqlServer:  sqlBuilder = new SqlServerSqlBuilder(stmt, subQueries, properties, queryTemplateDao);
                             break;
            case Sqlite:     sqlBuilder = new SqliteSqlBuilder(stmt, subQueries, properties, queryTemplateDao);
                             break;
            default:         throw new RuntimeException(String.format("Database type, %s, not recognized", databaseType));
        }

        return sqlBuilder;
    }

}
