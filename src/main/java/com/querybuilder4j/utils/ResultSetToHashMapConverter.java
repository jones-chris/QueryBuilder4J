package com.querybuilder4j.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ResultSetToHashMapConverter {

    /**
     * A convience method for converting a ResultSet to a Map of String and Integers.  The only two ResultSet columns
     * relevant to querybuilder4j are COLUMN_NAME and DATA_TYPE.
     *
     * @param rs a ResultSet
     * @return Map
     * @throws SQLException if rs cannot be walked or the COLUMN_NAME or DATA_TYPE columns do not exist in rs.
     */
    public static Map<String, Integer> toHashMap(ResultSet rs) throws SQLException {
        Map<String, Integer> map = new HashMap<>();

        while (rs.next()) {
            map.put(rs.getString("COLUMN_NAME"), rs.getInt("DATA_TYPE"));
        }

        return map;
    }

}
