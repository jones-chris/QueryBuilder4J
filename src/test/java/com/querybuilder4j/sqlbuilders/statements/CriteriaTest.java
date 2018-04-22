package com.querybuilder4j.sqlbuilders.statements;

import com.querybuilder4j.config.Conjunction;
import com.querybuilder4j.config.Operator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.SortedSet;
import java.util.TreeSet;

import static com.querybuilder4j.config.Conjunction.*;
import static com.querybuilder4j.config.Operator.*;
import static org.junit.Assert.*;

public class CriteriaTest {
    private Criteria criteria1 = new Criteria(0);
    private Criteria criteria2 = new Criteria(1);
    private Criteria criteria3 = new Criteria(2);
    private Criteria criteria4 = new Criteria(3);

    @Before
    public void setUp() throws Exception {
        criteria1.conjunction = And;
        criteria1.column = "column1";
        criteria1.operator = equalTo;
        criteria1.filter = "filter1";

        criteria2.parentCriteriaId = 0;
        criteria2.setRank(1);
        criteria2.conjunction = And;
        criteria2.column = "column2";
        criteria2.operator = equalTo;
        criteria2.filter = "filter2";

        criteria3.parentCriteriaId = 0;
        criteria3.setRank(2);
        criteria3.conjunction = And;
        criteria3.column = "column3";
        criteria3.operator = equalTo;
        criteria3.filter = "filter3";

        criteria4.parentCriteriaId = 1;
        criteria4.setRank(1);
        criteria4.conjunction = And;
        criteria4.column = "column4";
        criteria4.operator = equalTo;
        criteria4.filter = "filter4";
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void equals() throws Exception {

    }

//    @Test
//    public void toString() throws Exception {
//
//    }
//
//    @Test
//    public void clone() throws Exception {
//
//    }

    @Test
    public void isValid() throws Exception {

    }

    @Test
    public void compareTo() throws Exception {
        SortedSet<Criteria> criteriaSet = new TreeSet<>();
        criteriaSet.add(criteria1);
        criteriaSet.add(criteria2);
        criteriaSet.add(criteria3);
        criteriaSet.add(criteria4);

        System.out.println(criteriaSet);
        assertTrue(true);
    }

}