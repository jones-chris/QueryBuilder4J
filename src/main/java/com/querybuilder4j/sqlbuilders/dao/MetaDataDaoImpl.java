package com.querybuilder4j.sqlbuilders.dao;

import com.querybuilder4j.utils.ResultSetToHashMapConverter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Properties;

public class MetaDataDaoImpl {
    private Properties properties;


    public MetaDataDaoImpl(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Map<String, Integer> getTableSchema(String table, String column) {
        try (Connection conn = getConnection(properties)) {
            ResultSet columnMetaData = conn.getMetaData().getColumns(null, null, table, "%");
            return ResultSetToHashMapConverter.toHashMap(columnMetaData);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Connection getConnection(Properties properties) throws Exception {
        String url = properties.getProperty("url");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");

        return DriverManager.getConnection(
                url,
                (username != null) ? username : null,
                (password != null) ? password : null
        );
    }

}
