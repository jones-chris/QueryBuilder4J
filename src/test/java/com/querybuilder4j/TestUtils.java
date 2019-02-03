package com.querybuilder4j;


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

    public static Connection getConnection(Properties properties) throws Exception {
        String url = properties.getProperty("url");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");

        return DriverManager.getConnection(
                url,
                (username != null) ? username : null,
                (password != null) ? password : null
        );
    }

    public static int getRandomInt(int minInclusive, int maxExclusive) {
        return org.apache.commons.lang3.RandomUtils.nextInt(minInclusive, maxExclusive);
    }

}
