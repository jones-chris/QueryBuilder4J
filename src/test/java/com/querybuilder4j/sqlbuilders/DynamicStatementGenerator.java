package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.QueryTemplateDaoImpl;
import com.querybuilder4j.TestUtils;
import com.querybuilder4j.statements.*;
import com.querybuilder4j.databasemetadata.QueryTemplateDao;
import org.apache.commons.lang.math.RandomUtils;

import java.util.*;

import static com.querybuilder4j.statements.Conjunction.*;
import static com.querybuilder4j.statements.Operator.*;

public class DynamicStatementGenerator {
    private Properties databaseProperties;
    private List<String> randomColumns = new ArrayList<>();
    private List<String> randomTables = new ArrayList<>();
    private List<Criteria> randomCriteria = new ArrayList<>();
    private int numberOfSelectStatements;
    private QueryTemplateDao queryTemplateDao;


    public DynamicStatementGenerator(Properties databaseProperties, int numberOfSelectStatements) {
        this.databaseProperties = databaseProperties;
        this.numberOfSelectStatements = numberOfSelectStatements;
        this.queryTemplateDao = new QueryTemplateDaoImpl(databaseProperties);

        randomColumns.add("county_spending_detail.department");
        randomColumns.add("county_spending_detail.service");
        randomColumns.add("county_spending_detail.fiscal_year_period");

        randomTables.add("county_spending_detail");
    }

    public List<SelectStatement> createRandomSelectStatements() throws Exception {
        List<SelectStatement> results = new ArrayList<>();
        getCriteriaSet();

        //i is the number of sql statements to output.
        for (int i=0; i<numberOfSelectStatements; i++) {
            getCriteriaSet();

            //get columns
            boolean getSingleColumn = RandomUtils.nextBoolean();
            List<Column> columnsList = new ArrayList<>();
            if (getSingleColumn) {
                int randomIndex = RandomUtils.nextInt(randomColumns.size());
                columnsList.add(new Column(randomColumns.get(randomIndex)));
            } else {
                int numOfColumns = org.apache.commons.lang3.RandomUtils.nextInt(1, randomColumns.size());
                for (int j=0; j<numOfColumns; j++) {
                    int randomIndex = RandomUtils.nextInt(randomColumns.size());
                    columnsList.add(new Column(randomColumns.get(randomIndex)));
                }
            }

            //get table
            int tablesIndex = RandomUtils.nextInt(randomTables.size());
            String table = randomTables.get(tablesIndex);

            //get criteria
            boolean getSingleCriteria = RandomUtils.nextBoolean();
            List<Criteria> criteriaSet = new ArrayList<>();
            if (getSingleCriteria) {
                int criteriaIndex = RandomUtils.nextInt(randomCriteria.size());
                Criteria criteriaClone = (Criteria) randomCriteria.get(criteriaIndex).clone();
                criteriaClone.setId(0);
                criteriaSet.add(criteriaClone);
            } else {
                criteriaSet.add(randomCriteria.get(0));
                int numOfCriteria = TestUtils.getRandomInt(1, randomCriteria.size());

                List<Integer> eligibleParentIds = new ArrayList<>();
                eligibleParentIds.add(0);

                for (int j=1; j<numOfCriteria; j++) {
                    getCriteriaSet();
                    int randomIndex = RandomUtils.nextInt(randomCriteria.size());
                    Criteria criteriaClone = (Criteria) randomCriteria.get(randomIndex).clone();
                    criteriaClone.setId(j); // set criteria's id sequentially so that sqlBuilder logic works.  The object is a clone, so it will not overwrite the object it's cloned from.

                    if (j > 0) {
                        //keep track of last assigned parentId
                        //if randomParentIndex is greater than last assigned parentId, generate randomParentIndex again.

                        //TODO:  add support for parentId of null.

                        int randomParentIndex = TestUtils.getRandomInt(0, j); //will be 0 for criteria #1.

                        //remove all elements that are less than randomParentIndex
                        while (! eligibleParentIds.contains(randomParentIndex)) {
                            randomParentIndex = TestUtils.getRandomInt(0, j);
                        }

                        List<Integer> newEligibleParentIds = new ArrayList<>();
                        for (int parentId : eligibleParentIds) {
                            if (parentId <= randomParentIndex)
                                newEligibleParentIds.add(parentId);
                        }
                        eligibleParentIds = newEligibleParentIds;
                        eligibleParentIds.add(j);

                        criteriaClone.parentId = randomParentIndex;
                    }

                    criteriaSet.add(criteriaClone);
                }
            }

            boolean needJoin = RandomUtils.nextBoolean();
            List<Join> joins = new ArrayList<>();
            if (needJoin) {
                boolean needMultipleJoinColumns = RandomUtils.nextBoolean();
                Join join = createJoin(needMultipleJoinColumns);
                joins.add(join);
            }

            boolean isSuppressNulls = RandomUtils.nextBoolean();
            boolean isGroupBy = RandomUtils.nextBoolean();
            boolean isOrderBy = RandomUtils.nextBoolean();
            boolean isAscending = RandomUtils.nextBoolean();
            long limit = (long) RandomUtils.nextInt(100);
            long offset = (long) RandomUtils.nextInt(100);

            // Create select statement with randomized properties
            SelectStatement stmt = new SelectStatement();
            stmt.setColumns(columnsList);
            stmt.setTable(table);
            stmt.setJoins(joins);
            stmt.setCriteria(criteriaSet);
            stmt.setSuppressNulls(isSuppressNulls);
            stmt.setGroupBy(isGroupBy);
            stmt.setOrderBy(isOrderBy);
            stmt.setAscending(isAscending);
            stmt.setLimit(limit);
            stmt.setOffset(offset);
            stmt.setQueryTemplateDao(queryTemplateDao);
            stmt.setDatabaseMetaData(databaseProperties);

            results.add(stmt);
        }

        return results;
    }

    private void getCriteriaSet() {
        //wipe old items in randomCriteria
        randomCriteria = new ArrayList<>();

        //quoted column - equal to
        Criteria quotedColumn_criteriaEqualTo = new Criteria(0);
        quotedColumn_criteriaEqualTo.parentId = null;
        quotedColumn_criteriaEqualTo.conjunction = And;
        quotedColumn_criteriaEqualTo.column = "county_spending_detail.service";
        quotedColumn_criteriaEqualTo.operator = equalTo;
        quotedColumn_criteriaEqualTo.filter = "General Government";
        randomCriteria.add(quotedColumn_criteriaEqualTo);

        //quoted column - not equal to
        Criteria quotedColumn_criteriaNotEqualTo = new Criteria(1);
        quotedColumn_criteriaNotEqualTo.parentId = null;
        quotedColumn_criteriaNotEqualTo.conjunction = And;
        quotedColumn_criteriaNotEqualTo.column = "county_spending_detail.service";
        quotedColumn_criteriaNotEqualTo.operator = notEqualTo;
        quotedColumn_criteriaNotEqualTo.filter = "General Government";
        randomCriteria.add(quotedColumn_criteriaNotEqualTo);

        //quoted column - greater than or equals
        Criteria quotedColumn_criteriaGreaterThanOrEquals = new Criteria(2);
        quotedColumn_criteriaGreaterThanOrEquals.parentId = null;
        quotedColumn_criteriaGreaterThanOrEquals.conjunction = And;
        quotedColumn_criteriaGreaterThanOrEquals.column = "county_spending_detail.service";
        quotedColumn_criteriaGreaterThanOrEquals.operator = greaterThanOrEquals;
        quotedColumn_criteriaGreaterThanOrEquals.filter = "General Government";
        randomCriteria.add(quotedColumn_criteriaGreaterThanOrEquals);

        //quoted column - less than or equals
        Criteria quotedColumn_criteriaLessThanOrEquals = new Criteria(3);
        quotedColumn_criteriaLessThanOrEquals.parentId = null;
        quotedColumn_criteriaLessThanOrEquals.conjunction = And;
        quotedColumn_criteriaLessThanOrEquals.column = "county_spending_detail.service";
        quotedColumn_criteriaLessThanOrEquals.operator = lessThanOrEquals;
        quotedColumn_criteriaLessThanOrEquals.filter = "General Government";
        randomCriteria.add(quotedColumn_criteriaLessThanOrEquals);

        //quoted column - greater than
        Criteria quotedColumn_criteriaGreaterThan = new Criteria(4);
        quotedColumn_criteriaGreaterThan.parentId = null;
        quotedColumn_criteriaGreaterThan.conjunction = And;
        quotedColumn_criteriaGreaterThan.column = "county_spending_detail.service";
        quotedColumn_criteriaGreaterThan.operator = greaterThan;
        quotedColumn_criteriaGreaterThan.filter = "General Government";
        randomCriteria.add(quotedColumn_criteriaGreaterThan);

        //quoted column - less than
        Criteria quotedColumn_criteriaLessThan = new Criteria(5);
        quotedColumn_criteriaLessThan.parentId = null;
        quotedColumn_criteriaLessThan.conjunction = And;
        quotedColumn_criteriaLessThan.column = "county_spending_detail.service";
        quotedColumn_criteriaLessThan.operator = lessThan;
        quotedColumn_criteriaLessThan.filter = "General Government";
        randomCriteria.add(quotedColumn_criteriaLessThan);

        //quoted column - like
        Criteria quotedColumn_criteriaLike = new Criteria(6);
        quotedColumn_criteriaLike.parentId = null;
        quotedColumn_criteriaLike.conjunction = And;
        quotedColumn_criteriaLike.column = "county_spending_detail.service";
        quotedColumn_criteriaLike.operator = like;
        quotedColumn_criteriaLike.filter = "General%";
        randomCriteria.add(quotedColumn_criteriaLike);

        //quoted column - not like
        Criteria quotedColumn_criteriaNotLike = new Criteria(7);
        quotedColumn_criteriaNotLike.parentId = null;
        quotedColumn_criteriaNotLike.conjunction = And;
        quotedColumn_criteriaNotLike.column = "county_spending_detail.service";
        quotedColumn_criteriaNotLike.operator = notLike;
        quotedColumn_criteriaNotLike.filter = "%Government";
        randomCriteria.add(quotedColumn_criteriaNotLike);

        //quoted column - in
        Criteria quotedColumn_criteriaIn = new Criteria(8);
        quotedColumn_criteriaIn.parentId = null;
        quotedColumn_criteriaIn.conjunction = And;
        quotedColumn_criteriaIn.column = "county_spending_detail.service";
        quotedColumn_criteriaIn.operator = in;
        quotedColumn_criteriaIn.filter = "General Government,Housing and Community Development";
        randomCriteria.add(quotedColumn_criteriaIn);

        //quoted column - not in
        Criteria quotedColumn_criteriaNotIn = new Criteria(9);
        quotedColumn_criteriaNotIn.parentId = null;
        quotedColumn_criteriaNotIn.conjunction = And;
        quotedColumn_criteriaNotIn.column = "county_spending_detail.service";
        quotedColumn_criteriaNotIn.operator = notIn;
        quotedColumn_criteriaNotIn.filter = "Housing and Community Development";
        randomCriteria.add(quotedColumn_criteriaNotIn);

        //quoted column - is null without null filter
        Criteria quotedColumn_criteriaIsNullWithoutNullFilter = new Criteria(10);
        quotedColumn_criteriaIsNullWithoutNullFilter.parentId = null;
        quotedColumn_criteriaIsNullWithoutNullFilter.conjunction = And;
        quotedColumn_criteriaIsNullWithoutNullFilter.column = "county_spending_detail.service";
        quotedColumn_criteriaIsNullWithoutNullFilter.operator = isNull;
        quotedColumn_criteriaIsNullWithoutNullFilter.filter = "Housing and Community Development";
        randomCriteria.add(quotedColumn_criteriaIsNullWithoutNullFilter);

        //quoted column - is null with null filter
        Criteria quotedColumn_criteriaIsNullWithNullFilter = new Criteria(11);
        quotedColumn_criteriaIsNullWithNullFilter.parentId = null;
        quotedColumn_criteriaIsNullWithNullFilter.conjunction = And;
        quotedColumn_criteriaIsNullWithNullFilter.column = "county_spending_detail.service";
        quotedColumn_criteriaIsNullWithNullFilter.operator = isNull;
        quotedColumn_criteriaIsNullWithNullFilter.filter = null;
        randomCriteria.add(quotedColumn_criteriaIsNullWithNullFilter);

        //quoted column - is null with empty string filter
        Criteria quotedColumn_criteriaIsNullWithEmptyStringFilter = new Criteria(12);
        quotedColumn_criteriaIsNullWithEmptyStringFilter.parentId = null;
        quotedColumn_criteriaIsNullWithEmptyStringFilter.conjunction = And;
        quotedColumn_criteriaIsNullWithEmptyStringFilter.column = "county_spending_detail.service";
        quotedColumn_criteriaIsNullWithEmptyStringFilter.operator = isNull;
        quotedColumn_criteriaIsNullWithEmptyStringFilter.filter = "";
        randomCriteria.add(quotedColumn_criteriaIsNullWithEmptyStringFilter);

        //quoted column - is not null without null filter
        Criteria quotedColumn_criteriaIsNotNullWithoutNullFilter = new Criteria(13);
        quotedColumn_criteriaIsNotNullWithoutNullFilter.parentId = null;
        quotedColumn_criteriaIsNotNullWithoutNullFilter.conjunction = And;
        quotedColumn_criteriaIsNotNullWithoutNullFilter.column = "county_spending_detail.service";
        quotedColumn_criteriaIsNotNullWithoutNullFilter.operator = isNotNull;
        quotedColumn_criteriaIsNotNullWithoutNullFilter.filter = "Housing and Community Development";
        randomCriteria.add(quotedColumn_criteriaIsNotNullWithoutNullFilter);

        //quoted column - is not null with null filter
        Criteria quotedColumn_criteriaIsNotNullWithNullFilter = new Criteria(14);
        quotedColumn_criteriaIsNotNullWithNullFilter.parentId = null;
        quotedColumn_criteriaIsNotNullWithNullFilter.conjunction = And;
        quotedColumn_criteriaIsNotNullWithNullFilter.column = "county_spending_detail.service";
        quotedColumn_criteriaIsNotNullWithNullFilter.operator = isNotNull;
        quotedColumn_criteriaIsNotNullWithNullFilter.filter = null;
        randomCriteria.add(quotedColumn_criteriaIsNotNullWithNullFilter);

        //quoted column - is not null with empty string filter
        Criteria quotedColumn_criteriaIsNotNullWithEmptyStringFilter = new Criteria(15);
        quotedColumn_criteriaIsNotNullWithEmptyStringFilter.parentId = null;
        quotedColumn_criteriaIsNotNullWithEmptyStringFilter.conjunction = And;
        quotedColumn_criteriaIsNotNullWithEmptyStringFilter.column = "county_spending_detail.service";
        quotedColumn_criteriaIsNotNullWithEmptyStringFilter.operator = isNotNull;
        quotedColumn_criteriaIsNotNullWithEmptyStringFilter.filter = "";
        randomCriteria.add(quotedColumn_criteriaIsNotNullWithEmptyStringFilter);

        //nonquoted column - equal to
        Criteria nonQuotedColumn_criteriaEqualTo = new Criteria(16);
        nonQuotedColumn_criteriaEqualTo.parentId = null;
        nonQuotedColumn_criteriaEqualTo.conjunction = And;
        nonQuotedColumn_criteriaEqualTo.column = "county_spending_detail.service";
        nonQuotedColumn_criteriaEqualTo.operator = equalTo;
        nonQuotedColumn_criteriaEqualTo.filter = "General Government";
        randomCriteria.add(nonQuotedColumn_criteriaEqualTo);

        //nonquoted column - not equal to
        Criteria nonQuotedColumn_criteriaNotEqualTo = new Criteria(17);
        nonQuotedColumn_criteriaNotEqualTo.parentId = null;
        nonQuotedColumn_criteriaNotEqualTo.conjunction = And;
        nonQuotedColumn_criteriaNotEqualTo.column = "county_spending_detail.fiscal_year_period";
        nonQuotedColumn_criteriaNotEqualTo.operator = notEqualTo;
        nonQuotedColumn_criteriaNotEqualTo.filter = "1";
        randomCriteria.add(nonQuotedColumn_criteriaNotEqualTo);

        //nonquoted column - greater than or equals
        Criteria nonQuotedColumn_criteriaGreaterThanOrEquals = new Criteria(18);
        nonQuotedColumn_criteriaGreaterThanOrEquals.parentId = null;
        nonQuotedColumn_criteriaGreaterThanOrEquals.conjunction = And;
        nonQuotedColumn_criteriaGreaterThanOrEquals.column = "county_spending_detail.fiscal_year_period";
        nonQuotedColumn_criteriaGreaterThanOrEquals.operator = greaterThanOrEquals;
        nonQuotedColumn_criteriaGreaterThanOrEquals.filter = "1";
        randomCriteria.add(nonQuotedColumn_criteriaGreaterThanOrEquals);

        //nonquoted column - less than or equals
        Criteria nonQuotedColumn_criteriaLessThanOrEquals = new Criteria(19);
        nonQuotedColumn_criteriaLessThanOrEquals.parentId = null;
        nonQuotedColumn_criteriaLessThanOrEquals.conjunction = And;
        nonQuotedColumn_criteriaLessThanOrEquals.column = "county_spending_detail.fiscal_year_period";
        nonQuotedColumn_criteriaLessThanOrEquals.operator = lessThanOrEquals;
        nonQuotedColumn_criteriaLessThanOrEquals.filter = "1";
        randomCriteria.add(nonQuotedColumn_criteriaLessThanOrEquals);

        //nonquoted column - greater than
        Criteria nonQuotedColumn_criteriaGreaterThan = new Criteria(10);
        nonQuotedColumn_criteriaGreaterThan.parentId = null;
        nonQuotedColumn_criteriaGreaterThan.conjunction = And;
        nonQuotedColumn_criteriaGreaterThan.column = "county_spending_detail.fiscal_year_period";
        nonQuotedColumn_criteriaGreaterThan.operator = greaterThan;
        nonQuotedColumn_criteriaGreaterThan.filter = "1";
        randomCriteria.add(nonQuotedColumn_criteriaGreaterThan);

        //nonquoted column - less than
        Criteria nonQuotedColumn_criteriaLessThan = new Criteria(21);
        nonQuotedColumn_criteriaLessThan.parentId = null;
        nonQuotedColumn_criteriaLessThan.conjunction = And;
        nonQuotedColumn_criteriaLessThan.column = "county_spending_detail.fiscal_year_period";
        nonQuotedColumn_criteriaLessThan.operator = lessThan;
        nonQuotedColumn_criteriaLessThan.filter = "1";
        randomCriteria.add(nonQuotedColumn_criteriaLessThan);

        //nonquoted column - like
        // todo:  add support for like/not like for nonquoted columns.
//        Criteria nonQuotedColumn_criteriaLike = new Criteria(6);
//        nonQuotedColumn_criteriaLike.parentId = null;
//        nonQuotedColumn_criteriaLike.conjunction = And;
//        nonQuotedColumn_criteriaLike.column = "county_spending_detail.fiscal_year_period";
//        nonQuotedColumn_criteriaLike.operator = like;
//        nonQuotedColumn_criteriaLike.filter = "1%";
//        randomCriteria.add(nonQuotedColumn_criteriaLike);

        //nonquoted column - not like
//        Criteria nonQuotedColumn_criteriaNotLike = new Criteria(7);
//        nonQuotedColumn_criteriaNotLike.parentId = null;
//        nonQuotedColumn_criteriaNotLike.conjunction = And;
//        nonQuotedColumn_criteriaNotLike.column = "county_spending_detail.fiscal_year_period";
//        nonQuotedColumn_criteriaNotLike.operator = notLike;
//        nonQuotedColumn_criteriaNotLike.filter = "%1";
//        randomCriteria.add(nonQuotedColumn_criteriaNotLike);

        //nonquoted column - in
        Criteria nonQuotedColumn_criteriaIn = new Criteria(22);
        nonQuotedColumn_criteriaIn.parentId = null;
        nonQuotedColumn_criteriaIn.conjunction = And;
        nonQuotedColumn_criteriaIn.column = "county_spending_detail.fiscal_year_period";
        nonQuotedColumn_criteriaIn.operator = in;
        nonQuotedColumn_criteriaIn.filter = "1,2";
        randomCriteria.add(nonQuotedColumn_criteriaIn);

        //nonquoted column - not in
        Criteria nonQuotedColumn_criteriaNotIn = new Criteria(23);
        nonQuotedColumn_criteriaNotIn.parentId = null;
        nonQuotedColumn_criteriaNotIn.conjunction = And;
        nonQuotedColumn_criteriaNotIn.column = "county_spending_detail.fiscal_year_period";
        nonQuotedColumn_criteriaNotIn.operator = notIn;
        nonQuotedColumn_criteriaNotIn.filter = "1,2";
        randomCriteria.add(nonQuotedColumn_criteriaNotIn);

        //nonquoted column - is null
        Criteria nonQuotedColumn_criteriaIsNull = new Criteria(24);
        nonQuotedColumn_criteriaIsNull.parentId = null;
        nonQuotedColumn_criteriaIsNull.conjunction = And;
        nonQuotedColumn_criteriaIsNull.column = "county_spending_detail.fiscal_year_period";
        nonQuotedColumn_criteriaIsNull.operator = isNull;
        nonQuotedColumn_criteriaIsNull.filter = "1";
        randomCriteria.add(nonQuotedColumn_criteriaIsNull);

        //nonquoted column - is null with null filter
        Criteria nonQuotedColumn_criteriaIsNullWithNullFilter = new Criteria(25);
        nonQuotedColumn_criteriaIsNullWithNullFilter.parentId = null;
        nonQuotedColumn_criteriaIsNullWithNullFilter.conjunction = And;
        nonQuotedColumn_criteriaIsNullWithNullFilter.column = "county_spending_detail.fiscal_year_period";
        nonQuotedColumn_criteriaIsNullWithNullFilter.operator = isNull;
        nonQuotedColumn_criteriaIsNullWithNullFilter.filter = null;
        randomCriteria.add(nonQuotedColumn_criteriaIsNullWithNullFilter);

        //nonquoted column - is null with empty string filter
        Criteria nonQuotedColumn_criteriaIsNullWithEmptyStringFilter = new Criteria(26);
        nonQuotedColumn_criteriaIsNullWithEmptyStringFilter.parentId = null;
        nonQuotedColumn_criteriaIsNullWithEmptyStringFilter.conjunction = And;
        nonQuotedColumn_criteriaIsNullWithEmptyStringFilter.column = "county_spending_detail.fiscal_year_period";
        nonQuotedColumn_criteriaIsNullWithEmptyStringFilter.operator = isNull;
        nonQuotedColumn_criteriaIsNullWithEmptyStringFilter.filter = "";
        randomCriteria.add(nonQuotedColumn_criteriaIsNullWithEmptyStringFilter);

        //quoted column - is not null
        Criteria nonQuotedColumn_criteriaIsNotNull = new Criteria(27);
        nonQuotedColumn_criteriaIsNotNull.parentId = null;
        nonQuotedColumn_criteriaIsNotNull.conjunction = And;
        nonQuotedColumn_criteriaIsNotNull.column = "county_spending_detail.fiscal_year_period";
        nonQuotedColumn_criteriaIsNotNull.operator = isNotNull;
        nonQuotedColumn_criteriaIsNotNull.filter = "1";
        randomCriteria.add(nonQuotedColumn_criteriaIsNotNull);

        //quoted column - is not null with null filter
        Criteria nonQuotedColumn_criteriaIsNotNullWithNullFilter = new Criteria(28);
        nonQuotedColumn_criteriaIsNotNullWithNullFilter.parentId = null;
        nonQuotedColumn_criteriaIsNotNullWithNullFilter.conjunction = And;
        nonQuotedColumn_criteriaIsNotNullWithNullFilter.column = "county_spending_detail.fiscal_year_period";
        nonQuotedColumn_criteriaIsNotNullWithNullFilter.operator = isNotNull;
        nonQuotedColumn_criteriaIsNotNullWithNullFilter.filter = null;
        randomCriteria.add(nonQuotedColumn_criteriaIsNotNullWithNullFilter);

        //quoted column - is not null with empty string filter
        Criteria nonQuotedColumn_criteriaIsNotNullWithEmptyStringFilter = new Criteria(29);
        nonQuotedColumn_criteriaIsNotNullWithEmptyStringFilter.parentId = null;
        nonQuotedColumn_criteriaIsNotNullWithEmptyStringFilter.conjunction = And;
        nonQuotedColumn_criteriaIsNotNullWithEmptyStringFilter.column = "county_spending_detail.fiscal_year_period";
        nonQuotedColumn_criteriaIsNotNullWithEmptyStringFilter.operator = isNotNull;
        nonQuotedColumn_criteriaIsNotNullWithEmptyStringFilter.filter = "";
        randomCriteria.add(nonQuotedColumn_criteriaIsNotNullWithEmptyStringFilter);
    }

    private Join createJoin(boolean shouldHaveMultipleJoinColumns) {
        final String parentTable = "county_spending_detail";
        final String targetTablePeriods = "periods";
        final String targetTableServiceHierarchy = "service_hierarchy";
        final Join.JoinType[] joinTypesForSqlite = {
            Join.JoinType.LEFT_EXCLUDING,
            Join.JoinType.LEFT,
            Join.JoinType.INNER
        };
        final Join.JoinType[] joinTypesForMySql = {
            Join.JoinType.INNER,
            Join.JoinType.LEFT,
            Join.JoinType.RIGHT
        };
        final Join.JoinType[] joinTypes = {
            Join.JoinType.LEFT_EXCLUDING,
            Join.JoinType.FULL_OUTER_EXCLUDING,
            Join.JoinType.FULL_OUTER,
            Join.JoinType.RIGHT_EXCLUDING,
            Join.JoinType.RIGHT,
            Join.JoinType.LEFT,
            Join.JoinType.INNER
        };

        Join join = new Join();
        int randomInt;
        Join.JoinType joinType;
        if (TestUtils.getDatabaseType(databaseProperties).equals(DatabaseType.Sqlite)) {
            randomInt = TestUtils.getRandomInt(0, joinTypesForSqlite.length - 1);
            joinType = joinTypesForSqlite[randomInt];
        } else if (TestUtils.getDatabaseType(databaseProperties).equals(DatabaseType.MySql)) {
            randomInt = TestUtils.getRandomInt(0, joinTypesForMySql.length - 1);
            joinType = joinTypesForMySql[randomInt];
        } else {
            randomInt = TestUtils.getRandomInt(0, joinTypes.length - 1);
            joinType = joinTypes[randomInt];
        }
        join.setJoinType(joinType);
        join.setParentTable(parentTable);

        List<String> parentJoinColumns = new ArrayList<>();
        List<String> targetJoinColumns = new ArrayList<>();
        if (shouldHaveMultipleJoinColumns) {
            join.setTargetTable(targetTableServiceHierarchy);

            parentJoinColumns.add("county_spending_detail.fiscal_year");
            targetJoinColumns.add("service_hierarchy.fiscal_year");

            parentJoinColumns.add("county_spending_detail.service");
            targetJoinColumns.add("service_hierarchy.service");

            join.setParentJoinColumns(parentJoinColumns);
            join.setTargetJoinColumns(targetJoinColumns);
        } else {
            join.setTargetTable(targetTablePeriods);

            parentJoinColumns.add("county_spending_detail.fiscal_year_period");
            targetJoinColumns.add("periods.period");

            join.setParentJoinColumns(parentJoinColumns);
            join.setTargetJoinColumns(targetJoinColumns);
        }

        return join;
    }

}
