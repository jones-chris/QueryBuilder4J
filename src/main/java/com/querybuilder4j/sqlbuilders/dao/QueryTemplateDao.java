package com.querybuilder4j.sqlbuilders.dao;

import com.querybuilder4j.sqlbuilders.statements.SelectStatement;

public interface QueryTemplateDao {

    SelectStatement getQueryTemplateByName(String name);

}
