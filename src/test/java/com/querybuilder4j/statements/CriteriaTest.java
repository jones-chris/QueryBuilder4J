package com.querybuilder4j.statements;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.SortedSet;
import java.util.TreeSet;

import static com.querybuilder4j.statements.Conjunction.*;
import static com.querybuilder4j.statements.Operator.*;
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

        criteria2.parentId = 0;
        criteria2.conjunction = And;
        criteria2.column = "column2";
        criteria2.operator = equalTo;
        criteria2.filter = "filter2";

        criteria3.parentId = 0;
        criteria3.conjunction = And;
        criteria3.column = "column3";
        criteria3.operator = equalTo;
        criteria3.filter = "filter3";

        criteria4.parentId = 1;
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

    @Test
    public void isValid_ColumnIsNullReturnsFalse() throws Exception {
        Criteria criteria = new Criteria();
        criteria.setId(1);
        criteria.column = null;
        criteria.operator = Operator.equalTo;

        assertFalse(criteria.isValid());
    }

    @Test
    public void isValid_OperatorIsNullReturnsFalse() throws Exception {
        Criteria criteria = new Criteria();
        criteria.setId(1);
        criteria.column = "column1";
        criteria.operator = null;

        assertFalse(criteria.isValid());
    }

    @Test
    public void isValid_ColumnAndOperatorAreNullReturnsFalse() throws Exception {
        Criteria criteria = new Criteria();
        criteria.setId(1);
        criteria.column = null;
        criteria.operator = null;

        assertFalse(criteria.isValid());
    }

    @Test
    public void isValid_ColumnAndOperatorAreNotNullReturnsTrue() throws Exception {
        Criteria criteria = new Criteria();
        criteria.setId(1);
        criteria.column = "column1";
        criteria.operator = Operator.equalTo;

        assertTrue(criteria.isValid());
    }

//    @Test
//    public void toString_AllDataPresent() throws Exception {
//        criteria1.frontParenthesis = Parenthesis.FrontParenthesis;
//        criteria1.endParenthesis.add(Parenthesis.EndParenthesis);
//        criteria1.endParenthesis.add(Parenthesis.EndParenthesis);
//        String expected = " And (column1 = filter1)) ";
//
//        String actual = criteria1.toString();
//
//        System.out.println(expected);
//        System.out.println(actual);
//        assertTrue(charsMatch(expected, actual));
//    }

    @Test
    public void compareTo() throws Exception {
        SortedSet<Criteria> criteriaSet = new TreeSet<>();
        criteriaSet.add(criteria4);
        criteriaSet.add(criteria3);
        criteriaSet.add(criteria2);
        criteriaSet.add(criteria1);
        Criteria[] criteriaArray = new Criteria[criteriaSet.size()];
        criteriaSet.toArray(criteriaArray);

        assertTrue(criteriaSet.size() == 4);
        assertTrue(criteriaArray[0].equals(criteria1));
        assertTrue(criteriaArray[1].equals(criteria2));
        assertTrue(criteriaArray[2].equals(criteria3));
        assertTrue(criteriaArray[3].equals(criteria4));
    }

}