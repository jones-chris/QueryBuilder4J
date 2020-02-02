package com.querybuilder4j.sqlfunctions;

import com.querybuilder4j.statements.DatabaseType;

public class SqlFunctions {

    /**
     * Takes a String and DatabaseType parameters and creates the SQL CAST function call string to convert the String
     * parameter into a SQL TEXT or VARCHAR type.
     *
     * @param s The function call as a String.
     * @param databaseType A DatabaseType to determine SQL syntax.
     * @return String
     */
    public static String castToString(String s, DatabaseType databaseType) {
        String functionString = "";
        switch (databaseType) {
            case MySql:
                functionString = String.format(" CAST('%s') AS CHAR(%d) ", s, s.length());
                break;
            case Oracle:
                functionString = String.format(" CAST('%s') AS VARCHAR2(%d) ", s, s.length());
                break;
            case PostgreSQL: case Redshift:
                functionString = String.format(" CAST('%s') AS VARCHAR(%d) ", s, s.length());
                break;
            case Sqlite:
                functionString = String.format(" CAST('%s') AS TEXT ", s);
                break;
            case SqlServer:
                functionString = String.format(" CAST('%s') AS VARCHAR ", s);
                break;
        }

        return functionString;
    }

}
