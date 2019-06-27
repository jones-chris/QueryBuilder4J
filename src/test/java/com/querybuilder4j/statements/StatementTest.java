package com.querybuilder4j.statements;

import com.querybuilder4j.databasemetadata.QueryTemplateDao;
import com.querybuilder4j.utils.SelectStatementFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static com.querybuilder4j.statements.Operator.in;
import static org.junit.Assert.*;

public class StatementTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
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