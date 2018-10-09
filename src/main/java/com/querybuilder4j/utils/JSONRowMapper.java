package com.querybuilder4j.utils;

import org.json.JSONObject;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JSONRowMapper implements RowMapper<JSONObject> {

    @Override
    public JSONObject mapRow(ResultSet rs, int rowNum) throws SQLException {
        JSONObject obj = new JSONObject();
        int total_columns = rs.getMetaData().getColumnCount();
        for (int i = 0; i < total_columns; i++) {
            obj.put(rs.getMetaData().getColumnLabel(i + 1)
                    .toLowerCase(), rs.getObject(i + 1));
        }
        return obj;
    }
}
