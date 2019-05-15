package com.querybuilder4j;

import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.config.Operator;
import com.querybuilder4j.sqlbuilders.dao.QueryTemplateDao;
import com.querybuilder4j.sqlbuilders.statements.Criteria;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class QueryTemplateDaoImpl implements QueryTemplateDao {

    private Map<String, SelectStatement> queries = new HashMap<>();

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

        SelectStatement getDepartmentsIn2014 = new SelectStatement(DatabaseType.Sqlite);
        getDepartmentsIn2014.setName("getDepartmentsIn2014");
        getDepartmentsIn2014.getColumns().add("county_spending_detail.department");
        getDepartmentsIn2014.setTable("county_spending_detail");
        getDepartmentsIn2014.getCriteria().add(criteria);
        queries.put("getDepartmentsIn2014", getDepartmentsIn2014);

        // One regular arg subquery
        Criteria criteria1 = new Criteria();
        criteria1.setId(0);
        criteria1.setColumn("county_spending_detail.fiscal_year");
        criteria1.setOperator(Operator.equalTo);
        criteria1.setFilter("@year");

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
        get2014FiscalYear.getColumns().add("county_spending_detail.fiscal_year");
        get2014FiscalYear.setTable("county_spending_detail");
        get2014FiscalYear.getCriteria().add(criteria2);
        queries.put("get2014FiscalYear", get2014FiscalYear);

        //getDepartmentsByMultipleYears
        Criteria criteria3 = new Criteria();
        criteria3.setId(0);
        criteria3.setColumn("county_spending_detail.fiscal_year");
        criteria3.setOperator(Operator.in);
        criteria3.setFilter("@year1,@year2");

        SelectStatement getDepartmentsByMultipleYears = new SelectStatement(DatabaseType.Sqlite);
        getDepartmentsByMultipleYears.getColumns().add("county_spending_detail.department");
        getDepartmentsByMultipleYears.setTable("county_spending_detail");
        getDepartmentsByMultipleYears.getCriteria().add(criteria3);
        queries.put("getDepartmentsByMultipleYears", getDepartmentsByMultipleYears);
    }

}
