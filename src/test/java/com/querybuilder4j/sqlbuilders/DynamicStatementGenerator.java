package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.TestUtils;
import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.sqlbuilders.statements.Criteria;
import com.querybuilder4j.sqlbuilders.statements.Join;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;
import org.apache.commons.lang.math.RandomUtils;

import java.util.*;

import static com.querybuilder4j.config.Conjunction.*;
import static com.querybuilder4j.config.Operator.*;

public class DynamicStatementGenerator {
    private DatabaseType databaseType;
    private List<String> randomColumns = new ArrayList<>();
    private List<String> randomTables = new ArrayList<>();
    private List<Criteria> randomCriteria = new ArrayList<>();
    private int numberOfSelectStatements;


    public DynamicStatementGenerator(DatabaseType databaseType, int numberOfSelectStatements) {
        this.databaseType = databaseType;
        this.numberOfSelectStatements = numberOfSelectStatements;

        randomColumns.add("county_spending_detail.department");
        randomColumns.add("county_spending_detail.service");
        randomColumns.add("county_spending_detail.fiscal_year_period");

        randomTables.add("county_spending_detail");
    }

    public List<SelectStatement> createRandomSelectStatements() throws Exception {
        List<SelectStatement> results = new ArrayList<>();
        randomCriteria = getCriteriaSet();

        //i is the number of sql statements to output.
        for (int i=0; i<numberOfSelectStatements; i++) {
            randomCriteria = getCriteriaSet();

            //get columns
            boolean getSingleColumn = RandomUtils.nextBoolean();
            List<String> columnsList = new ArrayList<>();
            if (getSingleColumn) {
                int randomIndex = RandomUtils.nextInt(randomColumns.size());
                columnsList.add(randomColumns.get(randomIndex));
            } else {
                int numOfColumns = org.apache.commons.lang3.RandomUtils.nextInt(1, randomColumns.size());
                for (int j=0; j<numOfColumns; j++) {
                    int randomIndex = RandomUtils.nextInt(randomColumns.size());
                    columnsList.add(randomColumns.get(randomIndex));
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
                    randomCriteria = getCriteriaSet();
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
            SelectStatement stmt = new SelectStatement(databaseType);
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

            results.add(stmt);
        }

        return results;
    }

    private List<Criteria> getCriteriaSet() {
        //wipe old items in randomCriteria
        randomCriteria = new ArrayList<>();

        //criteria
        //equal to
        Criteria criteriaEqualTo = new Criteria(0);
        criteriaEqualTo.parentId = null;
        criteriaEqualTo.conjunction = And;
        criteriaEqualTo.column = "county_spending_detail.service";
        criteriaEqualTo.operator = equalTo;
        criteriaEqualTo.filter = "General Government";

        //not equal to
        Criteria criteriaNotEqualTo = new Criteria(1);
        criteriaNotEqualTo.parentId = null;
        criteriaNotEqualTo.conjunction = And;
        criteriaNotEqualTo.column = "county_spending_detail.service";
        criteriaNotEqualTo.operator = notEqualTo;
        criteriaNotEqualTo.filter = "General Government";

        //greater than or equals
        Criteria criteriaGreaterThanOrEquals = new Criteria(2);
        criteriaGreaterThanOrEquals.parentId = null;
        criteriaGreaterThanOrEquals.conjunction = And;
        criteriaGreaterThanOrEquals.column = "county_spending_detail.service";
        criteriaGreaterThanOrEquals.operator = greaterThanOrEquals;
        criteriaGreaterThanOrEquals.filter = "General Government";

        //less than or equals
        Criteria criteriaLessThanOrEquals = new Criteria(3);
        criteriaLessThanOrEquals.parentId = null;
        criteriaLessThanOrEquals.conjunction = And;
        criteriaLessThanOrEquals.column = "county_spending_detail.service";
        criteriaLessThanOrEquals.operator = lessThanOrEquals;
        criteriaLessThanOrEquals.filter = "General Government";

        //greater than
        Criteria criteriaGreaterThan = new Criteria(4);
        criteriaGreaterThan.parentId = null;
        criteriaGreaterThan.conjunction = And;
        criteriaGreaterThan.column = "county_spending_detail.service";
        criteriaGreaterThan.operator = greaterThan;
        criteriaGreaterThan.filter = "General Government";

        //less than
        Criteria criteriaLessThan = new Criteria(5);
        criteriaLessThan.parentId = null;
        criteriaLessThan.conjunction = And;
        criteriaLessThan.column = "county_spending_detail.service";
        criteriaLessThan.operator = lessThan;
        criteriaLessThan.filter = "General Government";

        //like
        Criteria criteriaLike = new Criteria(6);
        criteriaLike.parentId = null;
        criteriaLike.conjunction = And;
        criteriaLike.column = "county_spending_detail.service";
        criteriaLike.operator = like;
        criteriaLike.filter = "General%";

        //not like
        Criteria criteriaNotLike = new Criteria(7);
        criteriaNotLike.parentId = null;
        criteriaNotLike.conjunction = And;
        criteriaNotLike.column = "county_spending_detail.service";
        criteriaNotLike.operator = notLike;
        criteriaNotLike.filter = "%Government";

        //in
        Criteria criteriaInFiscalYearPeriod = new Criteria(8);
        criteriaInFiscalYearPeriod.parentId = null;
        criteriaInFiscalYearPeriod.conjunction = And;
        criteriaInFiscalYearPeriod.column = "county_spending_detail.fiscal_year_period";
        criteriaInFiscalYearPeriod.operator = in;
        criteriaInFiscalYearPeriod.filter = "3,4";

        Criteria criteriaInService = new Criteria(9);
        criteriaInService.parentId = null;
        criteriaInService.conjunction = And;
        criteriaInService.column = "county_spending_detail.service";
        criteriaInService.operator = in;
        criteriaInService.filter = "General Government,Housing and Community Development";

        //not in
        Criteria criteriaNotIn = new Criteria(10);
        criteriaNotIn.parentId = null;
        criteriaNotIn.conjunction = And;
        criteriaNotIn.column = "county_spending_detail.service";
        criteriaNotIn.operator = notIn;
        criteriaNotIn.filter = "Housing and Community Development";

        //is null
        Criteria criteriaIsNull = new Criteria(11);
        criteriaIsNull.parentId = null;
        criteriaIsNull.conjunction = And;
        criteriaIsNull.column = "county_spending_detail.service";
        criteriaIsNull.operator = isNull;
        criteriaIsNull.filter = "Housing and Community Development";

        //is not null
        Criteria criteriaIsNotNull = new Criteria(12);
        criteriaIsNotNull.parentId = null;
        criteriaIsNotNull.conjunction = And;
        criteriaIsNotNull.column = "county_spending_detail.service";
        criteriaIsNotNull.operator = isNotNull;
        criteriaIsNotNull.filter = "Housing and Community Development";

        randomCriteria.add(criteriaEqualTo);
        randomCriteria.add(criteriaNotEqualTo);
        randomCriteria.add(criteriaGreaterThanOrEquals);
        randomCriteria.add(criteriaLessThanOrEquals);
        randomCriteria.add(criteriaGreaterThan);
        randomCriteria.add(criteriaLessThan);
        randomCriteria.add(criteriaLike);
        randomCriteria.add(criteriaNotLike);
        randomCriteria.add(criteriaInFiscalYearPeriod);
        randomCriteria.add(criteriaInService);
        randomCriteria.add(criteriaNotIn);
        randomCriteria.add(criteriaIsNull);
        randomCriteria.add(criteriaIsNotNull);

        return randomCriteria;
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
        if (databaseType.equals(DatabaseType.Sqlite)) {
            randomInt = TestUtils.getRandomInt(0, joinTypesForSqlite.length - 1);
        } else {
            randomInt = TestUtils.getRandomInt(0, joinTypes.length - 1);
        }

        Join.JoinType joinType;
        if (databaseType.equals(DatabaseType.Sqlite)) {
            joinType = joinTypesForSqlite[randomInt];
        } else {
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
