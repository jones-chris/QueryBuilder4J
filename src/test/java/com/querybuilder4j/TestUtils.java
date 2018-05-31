package com.querybuilder4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class TestUtils {

    public static boolean charsMatch(String s1, String s2) {
        if (s1.length() != s2.length()) return false;

        char[] s1Array = new char[s1.length()];
        char[] s2Array = new char[s2.length()];

        for (int i=0; i<s1Array.length; i++) {
            if (s1Array[i] != s2Array[i]) return false;
        }

        return true;
    }

    public static ResultSet multiColumnResultSetBuilder(Properties properties) throws Exception {
        Connection connection = DriverManager.getConnection(
                    properties.getProperty("url"),
                    properties.getProperty("username"),
                    properties.getProperty("password"));

            DatabaseMetaData metaData = connection.getMetaData();
            return metaData.getColumns(null, "public", "county_spending_detail", null);
    }

}
