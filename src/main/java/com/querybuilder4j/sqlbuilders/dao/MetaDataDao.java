package com.querybuilder4j.sqlbuilders.dao;

import java.sql.Connection;
import java.util.Map;
import java.util.Properties;

public interface MetaDataDao {

    Map<String, Integer> getTableSchema(String table, String column);
    Connection getConnection(Properties properties) throws Exception;

}
