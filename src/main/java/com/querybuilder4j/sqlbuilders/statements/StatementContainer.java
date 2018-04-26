package com.querybuilder4j.sqlbuilders.statements;

import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.config.Parenthesis;
import com.querybuilder4j.config.SqlBuilderFactory;
import com.querybuilder4j.dbconnection.DbConnection;
import com.querybuilder4j.sqlbuilders.AbstractSqlBuilder;
import sun.invoke.empty.Empty;

import java.sql.ResultSet;
import java.util.*;

import static com.querybuilder4j.config.SqlBuilderFactory.buildSqlBuilder;

public class StatementContainer {
    private Properties properties;
    private SortedSet<Statement> statements = new TreeSet<>();
    private DbConnection dbConnection;

    public StatementContainer(Properties properties, DbConnection dbConnection) {
        this.properties = properties;
        this.dbConnection = dbConnection;
    }

    public StatementContainer(Properties properties, DbConnection dbConnection, TreeSet<Statement> statements) {
        this.properties = properties;
        this.dbConnection = dbConnection;
        this.statements = statements;
    }

    public Set<Statement> getStatements() {
        return statements;
    }

    public void setStatements(TreeSet<Statement> statements) {
        this.statements = statements;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Map<Statement, ResultSet> runStatements() {
        String databaseTypeProp = properties.getProperty("databaseType");
        DatabaseType databaseType = Enum.valueOf(DatabaseType.class, databaseTypeProp) ;
        AbstractSqlBuilder sqlBuilder = buildSqlBuilder(databaseType);

        Map<Statement, ResultSet> resultMap = new HashMap<>();

        for (Statement stmt : statements) {
            try {
                String sql = "";

                if (stmt instanceof SelectStatement) {
                    sql = sqlBuilder.buildSql((SelectStatement) stmt);
                } else if (stmt instanceof InsertStatement) {
                    sql = sqlBuilder.buildSql((InsertStatement) stmt);
                } else if (stmt instanceof UpdateStatement) {
                    sql = sqlBuilder.buildSql((UpdateStatement) stmt);
                } else if (stmt instanceof DeleteStatement) {
                    sql = sqlBuilder.buildSql((DeleteStatement) stmt);
                }

                ResultSet resultSet = dbConnection.execute(sql);
                resultMap.put(stmt, resultSet);

                // Reset each criteria's front and end parenthesis before passing it back to calling function in resultMap.
                stmt.getCriteria().forEach(criteria -> {
                    criteria.frontParenthesis = Parenthesis.Empty;
                    criteria.endParenthesis = new ArrayList<>();
                });
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        return resultMap;
    }
}
