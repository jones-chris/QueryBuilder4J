package com.querybuilder4j.sqlbuilders.statements;

import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.config.Operator;
import com.querybuilder4j.sqlbuilders.dao.QueryTemplateDao;
import com.querybuilder4j.utils.SelectStatementFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static com.querybuilder4j.config.Conjunction.And;
import static com.querybuilder4j.config.Operator.equalTo;
import static com.querybuilder4j.config.Operator.in;
import static org.junit.Assert.*;

public class StatementTest {
    private Criteria criteria1 = new Criteria(0);
    private Criteria criteria2 = new Criteria(1);
    private Criteria criteria3 = new Criteria(2);
    private Criteria criteria4 = new Criteria(3);
    private Criteria criteria5 = new Criteria(4);
    private SelectStatement statement = new SelectStatement();


    @Before
    public void setUp() throws Exception {
        criteria1.conjunction = And;
        criteria1.column = "column1";
        criteria1.operator = equalTo;
        criteria1.filter = "filter1";

        criteria2.parentId = 0;
        criteria2.conjunction = And;
        criteria2.column = "column2";
        criteria2.operator = equalTo;
        criteria2.filter = "filter2";

        criteria3.parentId = 1;
        criteria3.conjunction = And;
        criteria3.column = "column3";
        criteria3.operator = equalTo;
        criteria3.filter = "filter3";

        criteria4.parentId = 1;
        criteria4.conjunction = And;
        criteria4.column = "column4";
        criteria4.operator = equalTo;
        criteria4.filter = "filter4";

        criteria5.conjunction = And;
        criteria5.column = "column5";
        criteria5.operator = equalTo;
        criteria5.filter = "filter5";
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void addParenthesisToCriteria_EndsWithChildTwoEndingParenthesis() {
        // id = 0, parent id = null    (column1 = filter1
            // id = 1, parent id = 0       AND (column2 = filter2
                // id = 2, parent id = 1           AND column3 = filter3
                // id = 3, parent id = 1           AND column4 = filter4)
        //                                  )

        List<Criteria> criteria = new ArrayList<>();
        criteria.add(criteria1);
        criteria.add(criteria2);
        criteria.add(criteria3);
        criteria.add(criteria4);
        statement.getCriteria().addAll(criteria);

        System.out.println(criteria);
        assertTrue(true);
    }

    @Test
    public void addParenthesisToCriteriaTest_EndsWithOneEndingParenthesis() {
        // id = 0, parent id = null    (column1 = filter1
        // id = 1, parent id = 0       AND (column2 = filter2
        // id = 2, parent id = 1           AND column3 = filter3
        // id = 3, parent id = 1           AND column4 = filter4)
        // id = 4, parent id = 0       AND column5 = filter5)

        List<Criteria> criteria = new ArrayList<>();
        criteria.add(criteria1);
        criteria.add(criteria2);
        criteria.add(criteria3);
        criteria.add(criteria4);
        criteria.add(criteria5);
        statement.setCriteria(criteria);

        System.out.println(criteria);
        assertTrue(true);
    }

    @Test
    public void addParenthesisToCriteriaTest_EndsWithZeroEndingParenthesis() {
        // id = 0, parent id = null    column1 = filter1
        // id = 4, parent id = null    AND column5 = filter5

        List<Criteria> criteria = new ArrayList<>();
        criteria.add(criteria1);
        criteria.add(criteria5);

        statement.setCriteria(criteria);

        System.out.println(criteria);
        assertTrue(true);
    }

    @Test
    public void addParenthesisToCriteriaTest_EndsWithZeroEndingParenthesisButChildCriteria() {
        // id = 0, parent id = null    column1 = filter1
        // id = 1, parent id = null    AND (column 2 = filter2
        // id = 2, parent id = 1            AND column3 = filter3)
        // id = 3, parent id = null    AND column5 = filter5

        criteria2.parentId = null;
        criteria3.parentId = 1;
        criteria4.parentId = null;

        List<Criteria> criteria = new ArrayList<>();
        criteria.add(criteria1);
        criteria.add(criteria2);
        criteria.add(criteria3);
        criteria.add(criteria4);
        statement.setCriteria(criteria);

        System.out.println(criteria);
        assertTrue(true);
    }

    @Test
    public void setSubqueries_noArgSubQuery() throws Exception {
        QueryTemplateDao queryTemplateDao = Mockito.mock(QueryTemplateDao.class);

        SelectStatement stmt = new SelectStatementFactory()
                .select("county_spending_detail.service")
                .from("county_spending_detail")
                .where("county_spending_detail.service", in, "$get_services_in_2017()")
                .setQueryTemplateDao(queryTemplateDao)
                .getSelectStatement(DatabaseType.Sqlite);

        stmt.setSubqueries();

        assertEquals(stmt.getSubQueries().size(), 1);
        assertEquals(stmt.getSubQueries().get("$0"), "get_services_in_2017()");
    }

    @Test
    public void setSubqueries_oneArgSubQuery() throws Exception {
        QueryTemplateDao queryTemplateDao = Mockito.mock(QueryTemplateDao.class);

        SelectStatement stmt = new SelectStatementFactory()
                .select("county_spending_detail.service")
                .from("county_spending_detail")
                .where("county_spending_detail.service", in, "$get_services_in_year(year=2017)")
                .setQueryTemplateDao(queryTemplateDao)
                .getSelectStatement(DatabaseType.Sqlite);

        stmt.setSubqueries();

        assertEquals(stmt.getSubQueries().size(), 1);
        assertEquals(stmt.getSubQueries().get("$0"), "get_services_in_year(year=2017)");
    }

    @Test
    public void setSubqueries_twoArgSubQuery() throws Exception {
        QueryTemplateDao queryTemplateDao = Mockito.mock(QueryTemplateDao.class);

        SelectStatement stmt = new SelectStatementFactory()
                .select("county_spending_detail.service")
                .from("county_spending_detail")
                .where("county_spending_detail.service", in, "$get_services_in_years(year1=2017; year2=2018)")
                .setQueryTemplateDao(queryTemplateDao)
                .getSelectStatement(DatabaseType.Sqlite);

        stmt.setSubqueries();

        assertEquals(stmt.getSubQueries().size(), 1);
        assertEquals(stmt.getSubQueries().get("$0"), "get_services_in_years(year1=2017; year2=2018)");
    }

    @Test
    public void setSubqueries_subQueryWithOneNestedSubQuery() throws Exception {
        QueryTemplateDao queryTemplateDao = Mockito.mock(QueryTemplateDao.class);

        SelectStatement stmt = new SelectStatementFactory()
                .select("county_spending_detail.service")
                .from("county_spending_detail")
                .where("county_spending_detail.service", in, "$get_services_in_years(year1=$get_2017())")
                .setQueryTemplateDao(queryTemplateDao)
                .getSelectStatement(DatabaseType.Sqlite);

        stmt.setSubqueries();

        assertEquals(stmt.getSubQueries().size(), 2);
        assertEquals(stmt.getSubQueries().get("$0"), "get_2017()");
        assertEquals(stmt.getSubQueries().get("$1"), "get_services_in_years(year1=$0)");
    }

    @Test
    public void setSubqueries_subQueryWithTwoNestedSubQueriesOnSameLevel() throws Exception {
        QueryTemplateDao queryTemplateDao = Mockito.mock(QueryTemplateDao.class);

        SelectStatement stmt = new SelectStatementFactory()
                .select("county_spending_detail.service")
                .from("county_spending_detail")
                .where("county_spending_detail.service", in, "$get_services_in_years(year1=$get_2017(); year2=$get_2018())")
                .setQueryTemplateDao(queryTemplateDao)
                .getSelectStatement(DatabaseType.Sqlite);

        stmt.setSubqueries();

        assertEquals(stmt.getSubQueries().size(), 3);
        assertEquals(stmt.getSubQueries().get("$0"), "get_2018()");
        assertEquals(stmt.getSubQueries().get("$1"), "get_2017()");
        assertEquals(stmt.getSubQueries().get("$2"), "get_services_in_years(year1=$1; year2=$0)");
    }

    @Test
    public void setSubqueries_subQueryWithTwoNestedSubQueriesOnDifferentLevels() throws Exception {
        QueryTemplateDao queryTemplateDao = Mockito.mock(QueryTemplateDao.class);

        SelectStatement stmt = new SelectStatementFactory()
                .select("county_spending_detail.service")
                .from("county_spending_detail")
                .where("county_spending_detail.service", in, "$get_services_in_years(year1=$get_2017(year1=$get_2018()))")
                .setQueryTemplateDao(queryTemplateDao)
                .getSelectStatement(DatabaseType.Sqlite);

        stmt.setSubqueries();

        assertEquals(stmt.getSubQueries().size(), 3);
        assertEquals(stmt.getSubQueries().get("$0"), "get_2018()");
        assertEquals(stmt.getSubQueries().get("$1"), "get_2017(year1=$0)");
        assertEquals(stmt.getSubQueries().get("$2"), "get_services_in_years(year1=$1)");
    }

    @Test
    public void setSubqueries_subQueryWithTwoNestedSubQueriesOnDifferentLevelsWithOneArg() throws Exception {
        QueryTemplateDao queryTemplateDao = Mockito.mock(QueryTemplateDao.class);

        SelectStatement stmt = new SelectStatementFactory()
                .select("county_spending_detail.service")
                .from("county_spending_detail")
                .where("county_spending_detail.service", in, "$get_services_in_years(year1=$get_2017(year1=$get_2018()); 2018)")
                .setQueryTemplateDao(queryTemplateDao)
                .getSelectStatement(DatabaseType.Sqlite);

        stmt.setSubqueries();

        assertEquals(stmt.getSubQueries().size(), 3);
        assertEquals(stmt.getSubQueries().get("$0"), "get_2018()");
        assertEquals(stmt.getSubQueries().get("$1"), "get_2017(year1=$0)");
        assertEquals(stmt.getSubQueries().get("$2"), "get_services_in_years(year1=$1; 2018)");
    }

}