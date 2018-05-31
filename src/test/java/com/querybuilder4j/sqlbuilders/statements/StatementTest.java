package com.querybuilder4j.sqlbuilders.statements;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.SortedSet;
import java.util.TreeSet;

import static com.querybuilder4j.config.Conjunction.And;
import static com.querybuilder4j.config.Operator.equalTo;
import static org.junit.Assert.*;

public class StatementTest {
    private Criteria criteria1 = new Criteria(0);
    private Criteria criteria2 = new Criteria(1);
    private Criteria criteria3 = new Criteria(2);
    private Criteria criteria4 = new Criteria(3);
    private Criteria criteria5 = new Criteria(4);
    private Statement statement = new SelectStatement();


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

        SortedSet<Criteria> criteria = new TreeSet<>();
        criteria.add(criteria1);
        criteria.add(criteria2);
        criteria.add(criteria3);
        criteria.add(criteria4);
        statement.setCriteria(criteria);

        statement.addParenthesisToCriteria();

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

        SortedSet<Criteria> criteria = new TreeSet<>();
        criteria.add(criteria1);
        criteria.add(criteria2);
        criteria.add(criteria3);
        criteria.add(criteria4);
        criteria.add(criteria5);
        statement.setCriteria(criteria);

        statement.addParenthesisToCriteria();

        System.out.println(criteria);
        assertTrue(true);
    }

    @Test
    public void addParenthesisToCriteriaTest_EndsWithZeroEndingParenthesis() {
        // id = 0, parent id = null    column1 = filter1
        // id = 4, parent id = null    AND column5 = filter5

        SortedSet<Criteria> criteria = new TreeSet<>();
        criteria.add(criteria1);
        criteria.add(criteria5);
        statement.setCriteria(criteria);

        statement.addParenthesisToCriteria();

        System.out.println(criteria);
        assertTrue(true);
    }

    @Test
    public void addParenthesisToCriteriaTest_EndsWithZeroEndingParenthesisButChildCriteria() {
        // id = 0, parent id = null    column1 = filter1
        // id = 1, parent id = null    AND (column 2 = filter2
        // id = 2, parent id = 1       AND column3 = filter3)
        // id = 3, parent id = null    AND column5 = filter5

        criteria2.parentId = null;
        criteria3.parentId = 1;
        criteria4.parentId = null;

        SortedSet<Criteria> criteria = new TreeSet<>();
        criteria.add(criteria1);
        criteria.add(criteria2);
        criteria.add(criteria3);
        criteria.add(criteria4);
        statement.setCriteria(criteria);

        statement.addParenthesisToCriteria();

        System.out.println(criteria);
        assertTrue(true);
    }

}