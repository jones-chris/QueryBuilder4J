package com.querybuilder4j.databasemetadata;

import com.querybuilder4j.statements.SelectStatement;

import java.util.List;

public interface QueryTemplateDao {

    SelectStatement getQueryTemplateByName(String name);
    boolean save(String primaryKey, String json);
    List<String> getNames(Integer limit, Integer offset, boolean ascending) throws Exception;

}
