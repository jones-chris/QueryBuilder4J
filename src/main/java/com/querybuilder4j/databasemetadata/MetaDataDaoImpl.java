package com.querybuilder4j.databasemetadata;

import com.querybuilder4j.config.Constants;
import com.querybuilder4j.utils.ResultSetToHashMapConverter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public class MetaDataDaoImpl {
    private final String url;
    private final String username;
    private final String password;


    public MetaDataDaoImpl(Properties properties) {
        this.url = properties.getProperty(Constants.DATABASE_URL);
        this.username = properties.getProperty(Constants.DATABASE_USERNAME);
        this.password = properties.getProperty(Constants.DATABASE_PASSWORD);
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Map<String, Integer> getTableSchema(String table) {
        try (Connection conn = getConnection()) {
            ResultSet columnMetaData = conn.getMetaData().getColumns(null, null, table, "%");
            return ResultSetToHashMapConverter.toHashMap(columnMetaData);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.url, this.username, this.password);
    }
    
}
