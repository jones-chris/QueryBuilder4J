package com.querybuilder4j.sqlbuilders;

import com.google.gson.Gson;
import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.config.Operator;
import com.querybuilder4j.sqlbuilders.dao.QueryTemplateDao;
import com.querybuilder4j.sqlbuilders.statements.Criteria;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QueryTemplateDaoImpl implements QueryTemplateDao {

//    private final String queryTemplate = "{\n" +
//            "  \"ascending\": false,\n" +
//            "  \"columns\": [\n" +
//            "    \"county_spending_detail.fiscal_year_period\",\n" +
//            "    \"county_spending_detail.fiscal_year\",\n" +
//            "    \"county_spending_detail.service\",\n" +
//            "    \"county_spending_detail.amount\"\n" +
//            "  ],\n" +
//            "  \"criteria\": [],\n" +
//            "  \"distinct\": true,\n" +
//            "  \"groupBy\": false,\n" +
//            "  \"joins\": [],\n" +
//            "  \"limit\": 5,\n" +
//            "  \"name\": \"sample_query\",\n" +
//            "  \"offset\": 5,\n" +
//            "  \"orderBy\": false,\n" +
//            "  \"suppressNulls\": false,\n" +
//            "  \"table\": \"county_spending_detail\"\n" +
//            "}";
    private Map<String, SelectStatement> queries = new HashMap<>();

    @Override
    public SelectStatement getQueryTemplateByName(String name) {
        //return new Gson().fromJson(queryTemplate, SelectStatement.class);
        if (queries.size() == 0) {
            addQueries();
        }

        return queries.get(name);
    }

    private void addQueries() {
        // No arg subquery
        Criteria criteria = new Criteria();
        criteria.setId(0);
        criteria.setColumn("county_spending_detail.fiscal_year");
        criteria.setOperator(Operator.equalTo);
        criteria.setFilter("2014");

        SelectStatement getDepartmentsIn2014 = new SelectStatement(DatabaseType.Sqlite);
        getDepartmentsIn2014.getColumns().add("county_spending_detail.department");
        getDepartmentsIn2014.setTable("county_spending_detail");
        getDepartmentsIn2014.getCriteria().add(criteria);
        queries.put("getDepartmentsIn2014", getDepartmentsIn2014);

        // One regular arg subquery
        Criteria criteria1 = new Criteria();
        criteria1.setId(0);
        criteria1.setColumn("county_spending_detail.fiscal_year");
        criteria1.setOperator(Operator.equalTo);
        criteria1.setFilter("$param:year");

        SelectStatement getDepartmentsByYear = new SelectStatement(DatabaseType.Sqlite);
        getDepartmentsByYear.getColumns().add("county_spending_detail.department");
        getDepartmentsByYear.setTable("county_spending_detail");
        getDepartmentsByYear.getCriteria().add(criteria1);
        queries.put("getDepartmentsByYear", getDepartmentsByYear);

        // No arg subquery
        //get2014FiscalYear
        Criteria criteria2 = new Criteria();
        criteria2.setId(0);
        criteria2.setColumn("county_spending_detail.fiscal_year");
        criteria2.setOperator(Operator.equalTo);
        criteria2.setFilter("2014");

        SelectStatement get2014FiscalYear = new SelectStatement(DatabaseType.Sqlite);
        getDepartmentsByYear.getColumns().add("county_spending_detail.fiscal_year");
        getDepartmentsByYear.setTable("county_spending_detail");
        getDepartmentsByYear.getCriteria().add(criteria2);
        queries.put("get2014FiscalYear", get2014FiscalYear);
    }

}
