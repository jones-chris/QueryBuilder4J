package com.querybuilder4j.sqlbuilders;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SqlCleanserTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void escape() throws Exception {
        String s = "Tiffany's";
        String expectedString = "Tiffany''s";

        String newString = SqlCleanser.escape(s);

        assertTrue(newString.equals(expectedString));
    }

    /**
     * Tests an ANSI keyword being preceded by nothing.
     *
     * @throws Exception
     */
    @Test
    public void sqlIsClean_NothingPreceedingAnsiKeyword() throws Exception {
        String s = "GRanT * TO read_only_user";

        assertFalse(SqlCleanser.sqlIsClean(s));
    }

    /**
     * Tests an ANSI keyword being preceded by a semicolon.
     *
     * @throws Exception
     */
    @Test
    public void sqlIsClean_semiColonPreceedingAnsiKeyword() throws Exception {
        String s = ";GRanT * TO read_only_user";

        assertFalse(SqlCleanser.sqlIsClean(s));
    }

    /**
     * Tests an ANSI keyword being preceded by a single space.
     *
     * @throws Exception
     */
    @Test
    public void sqlIsClean_singleSpacePreceedingAnsiKeyword() throws Exception {
        String s = " GRanT ";

        assertFalse(SqlCleanser.sqlIsClean(s));
    }

    /**
     * Regression test.  The "IN" in "COUNTY_SPENDING_DETAIL" would cause SqlCleanser to fail before regexp was added to
     *   SqlCleanser.
     *
     * @throws Exception
     */
    @Test
    public void sqlIsClean_regressionTest_tableNameContainsAnsiKeyword() throws Exception {
        String s = "COUNTY_SPENDING_DETAIL";

        assertTrue(SqlCleanser.sqlIsClean(s));
    }

    /**
     * Tests if a sqlIsClean will return false if the input string includes a reserved operator.
     *
     * @throws Exception
     */
    @Test
    public void sqlIsClean_testsReservedOperators() throws Exception {
        String s = "*=";

        assertFalse(SqlCleanser.sqlIsClean(s));
    }

    @Test
    public void sqlIsClean2() throws Exception {

    }

}