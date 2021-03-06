package com.querybuilder4j;

import com.querybuilder4j.statements.Column;
import com.querybuilder4j.statements.Operator;
import com.querybuilder4j.databasemetadata.QueryTemplateDao;
import com.querybuilder4j.statements.Criteria;
import com.querybuilder4j.statements.SelectStatement;

import java.util.*;

public class QueryTemplateDaoImpl implements QueryTemplateDao {

    private Map<String, SelectStatement> queries = new HashMap<>();

    private Properties properties;

    public QueryTemplateDaoImpl(Properties properties) {
        this.properties = properties;
    }

    @Override
    public SelectStatement getQueryTemplateByName(String name) {
        if (queries.size() == 0) {
            addQueries();
        }

        return queries.get(name);
    }

    @Override
    public boolean save(String primaryKey, String json) {
        return true;
    }

    @Override
    public List<String> getNames(Integer limit, Integer offset, boolean ascending) throws Exception {
        if (queries.size() == 0) {
            addQueries();
        }

        return new ArrayList<>(queries.keySet());
    }

    /**
     * This method only exists so that Gson will not throw a StackOverflow exception when serializing a SelectStatement
     * object and so that a QueryTemplateDaoImpl object just shows up as "" in the debugger when debugging (because
     * the debugger calls an object's toString() method.
     * @return
     */
    @Override
    public String toString() {
        return "";
    }

    private void addQueries() {
        // No arg subquery
        Criteria criteria = new Criteria();
        criteria.setId(0);
        criteria.setColumn("county_spending_detail.fiscal_year");
        criteria.setOperator(Operator.equalTo);
        criteria.setFilter("2014");

        SelectStatement getDepartmentsIn2014 = new SelectStatement();
        getDepartmentsIn2014.setName("getDepartmentsIn2014");
        getDepartmentsIn2014.getColumns().add(new Column("county_spending_detail.department"));
        getDepartmentsIn2014.setTable("county_spending_detail");
        getDepartmentsIn2014.getCriteria().add(criteria);
        getDepartmentsIn2014.setDatabaseMetaData(properties);
        queries.put("getDepartmentsIn2014", getDepartmentsIn2014);

        // One regular arg subquery
        Criteria criteria1 = new Criteria();
        criteria1.setId(0);
        criteria1.setColumn("county_spending_detail.fiscal_year");
        criteria1.setOperator(Operator.equalTo);
        criteria1.setFilter("@year");

        SelectStatement getDepartmentsByYear = new SelectStatement();
        getDepartmentsByYear.getColumns().add(new Column("county_spending_detail.department"));
        getDepartmentsByYear.setTable("county_spending_detail");
        getDepartmentsByYear.getCriteria().add(criteria1);
        getDepartmentsByYear.setDatabaseMetaData(properties);
        queries.put("getDepartmentsByYear", getDepartmentsByYear);

        // No arg subquery
        //get2014FiscalYear
        Criteria criteria2 = new Criteria();
        criteria2.setId(0);
        criteria2.setColumn("county_spending_detail.fiscal_year");
        criteria2.setOperator(Operator.equalTo);
        criteria2.setFilter("2014");

        SelectStatement get2014FiscalYear = new SelectStatement();
        get2014FiscalYear.getColumns().add(new Column("county_spending_detail.fiscal_year"));
        get2014FiscalYear.setTable("county_spending_detail");
        get2014FiscalYear.getCriteria().add(criteria2);
        get2014FiscalYear.setDatabaseMetaData(properties);
        queries.put("get2014FiscalYear", get2014FiscalYear);

        //getDepartmentsByMultipleYears
        Criteria criteria3 = new Criteria();
        criteria3.setId(0);
        criteria3.setColumn("county_spending_detail.fiscal_year");
        criteria3.setOperator(Operator.in);
        criteria3.setFilter("@year1,@year2");

        SelectStatement getDepartmentsByMultipleYears = new SelectStatement();
        getDepartmentsByMultipleYears.getColumns().add(new Column("county_spending_detail.department"));
        getDepartmentsByMultipleYears.setTable("county_spending_detail");
        getDepartmentsByMultipleYears.getCriteria().add(criteria3);
        getDepartmentsByMultipleYears.setDatabaseMetaData(properties);
        queries.put("getDepartmentsByMultipleYears", getDepartmentsByMultipleYears);
    }

}
