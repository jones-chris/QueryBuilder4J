package com.querybuilder4j;

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

}
