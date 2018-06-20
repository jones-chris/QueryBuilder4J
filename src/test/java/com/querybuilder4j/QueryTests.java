package com.querybuilder4j;

import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.sqlbuilders.SqlBuilder;
import com.querybuilder4j.sqlbuilders.statements.Criteria;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;
import com.sun.org.apache.bcel.internal.generic.Select;
import org.apache.commons.lang.math.RandomUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;

import static com.querybuilder4j.config.Conjunction.*;
import static com.querybuilder4j.config.Operator.*;
import static com.querybuilder4j.config.SqlBuilderFactory.buildSqlBuilder;
import static org.junit.Assert.assertTrue;

public class QueryTests {
    private SqlBuilder sqlBuilder;
    private Connection connection;
    private Properties properties;
    private DatabaseType databaseType;
    private String table = "county_spending_detail";
    private List<String> columns = new ArrayList<>();
    private Criteria criteria0;
    private Criteria criteria1;
    private List<Criteria> multipleCriteria = new ArrayList<>();
    private Criteria criteriaWithIsNotNull;
    private long limit = 100;
    private long offset = 50;
    private List<String> randomColumns = new ArrayList<>();
    private List<String> randomTables = new ArrayList<>();
    private List<Criteria> randomCriteria = new ArrayList<>();
    private static final int NUMBER_OF_SQL_STATEMENTS = 50;


    public QueryTests(DatabaseType databaseType, Properties properties) {
        this.databaseType = databaseType;
        this.properties = properties;
        columns.add("fund");
        columns.add("service");

        //columns
        randomColumns.add("fund");
        randomColumns.add("service");
        randomColumns.add("fiscal_year_period");

        //tables
        randomTables.add("county_spending_detail");
    }

    public SelectStatement createNewMainSelectStmt() throws Exception {

        SelectStatement stmt = new SelectStatement();
        stmt.setDatabaseType(databaseType);
        stmt.setTableSchema(TestUtils.multiColumnResultSetBuilder(properties));
        stmt.setDistinct(false);
        stmt.setColumns(columns);
        stmt.setTable(table);
        stmt.setLimit(this.limit);
        stmt.setOffset(this.offset);

        //set up criteria0
        criteria0 = new Criteria(0);
        criteria0.parentId = null;
        criteria0.conjunction = And;
        criteria0.column = "fund";
        criteria0.operator = equalTo;
        criteria0.filter = "Permitting";

        // set up criteria1
        criteria1 = new Criteria(1);
        criteria1.parentId = null;
        criteria1.conjunction = And;
        criteria1.column = "service";
        criteria1.operator = in;
        criteria1.filter = "Housing and Community Development";

        // set up criteriaWithIsNotNull
        criteriaWithIsNotNull = new Criteria(2);
        criteriaWithIsNotNull.parentId = null;
        criteriaWithIsNotNull.conjunction = And;
        criteriaWithIsNotNull.column = "fund";
        criteriaWithIsNotNull.operator = isNotNull;

        //setup multiple criteria
        multipleCriteria.add(criteria0);
        multipleCriteria.add(criteria1);
        stmt.addCriteria(multipleCriteria);

        return stmt;
    }

    public String buildSql_LimitSuppressNulls() throws Exception {
        SelectStatement stmt = createNewMainSelectStmt();

        return stmt.toString();
    }

    public String buildSql_OneChild() throws Exception{
        Criteria childCriteria1 = new Criteria(2);
        childCriteria1.parentId = 1;
        childCriteria1.conjunction = Or;
        childCriteria1.column = "service";
        childCriteria1.operator = equalTo;
        childCriteria1.filter = "Mass Transit";
        SelectStatement stmt = createNewMainSelectStmt();
        stmt.addCriteria(childCriteria1);

        return stmt.toString();
    }

    public String buildSql_WithDistinct() throws Exception {
        SelectStatement stmt = createNewMainSelectStmt();
        stmt.setDistinct(true);

        return stmt.toString();
    }

    public String buildSql_GroupBy() throws Exception {
        SelectStatement stmt = createNewMainSelectStmt();
        stmt.setGroupBy(true);

        return stmt.toString();
    }

    public String buildSql_OrderBy() throws Exception {
        SelectStatement stmt = createNewMainSelectStmt();
        stmt.setOrderBy(true);

        return stmt.toString();
    }

    public String buildSql_GroupByOrderBy() throws Exception {
        SelectStatement stmt = createNewMainSelectStmt();
        stmt.setOffset(0);
        stmt.setGroupBy(true);
        stmt.setOrderBy(true);

        return stmt.toString();
    }

    public String buildSql_SuppressNulls() throws Exception {
        SelectStatement stmt = createNewMainSelectStmt();
        stmt.setSuppressNulls(true);

        return stmt.toString();
    }

    public String buildSql_FailedTest_PostgreSQL1() throws Exception {
        /*Id:0
        parentId:null
        frontParen:(
                conjunction:And
        column:fund
        operator:=
        filter:Permitting
                endParen[]
        Id:1
        parentId:0
        frontParen:(
                conjunction:And
        column:fund
        operator:>
        filter:Permitting
                endParen[]
        Id:2
        parentId:1
        frontParen:null
        conjunction:And
        column:fund
        operator:is not null
        filter:null
        endParen[]
        Id:3
        parentId:1
        frontParen:null
        conjunction:And
        column:fund
        operator:<
        filter:Permitting
        endParen[)]
        Id:4
        parentId:0
        frontParen:null
        conjunction:And
        column:fund
        operator:<=
        filter:Permitting
        endParen[), ), )]
        */

        SelectStatement stmt = new SelectStatement(DatabaseType.PostgreSQL);

        List<String> columns = new ArrayList<>();
        columns.add("fiscal_year_period");

        SortedSet<Criteria> criteria = new TreeSet<>();
        Criteria c0 = new Criteria(0);
        c0.parentId = null;
        c0.column = "fund";
        c0.operator = equalTo;
        c0.filter = "Permitting";
        criteria.add(c0);

        Criteria c1 = new Criteria(1);
        c1.parentId = 0;
        c1.conjunction = And;
        c1.column = "fund";
        c1.operator = greaterThan;
        c1.filter = "Permitting";
        criteria.add(c1);

        Criteria c2 = new Criteria(2);
        c2.parentId = 1;
        c2.conjunction = And;
        c2.column = "fund";
        c2.operator = isNotNull;
        c2.filter = null;
        criteria.add(c2);

        Criteria c3 = new Criteria(3);
        c3.parentId = 1;
        c3.conjunction = And;
        c3.column = "fund";
        c3.operator = lessThan;
        c3.filter = "Permitting";
        criteria.add(c3);

        Criteria c4 = new Criteria(4);
        c4.parentId = 0;
        c4.conjunction = And;
        c4.column = "fund";
        c4.operator = lessThanOrEquals;
        c4.filter = "Permitting";
        criteria.add(c4);

        stmt.setColumns(columns);
        stmt.setTable("county_spending_detail");
        stmt.setCriteria(criteria);
        stmt.setSuppressNulls(false);
        stmt.setGroupBy(false);
        stmt.setOrderBy(false);
        stmt.setAscending(false);
        stmt.setLimit(10);
        stmt.setOffset(1);
        stmt.setTableSchema(TestUtils.multiColumnResultSetBuilder(Constants.dbProperties.get(stmt.getDatabaseType())));

        return stmt.toString();
    }

    public Map<SelectStatement, String> buildSql_randomizer() throws Exception {
        Map<SelectStatement, String> results = new HashMap<>();
        randomCriteria = getCriteriaSet();

        //i is the number of sql statements to output.
        for (int i=0; i<NUMBER_OF_SQL_STATEMENTS; i++) {
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
            SortedSet<Criteria> criteriaSet = new TreeSet<>();
            if (getSingleCriteria) {
                int criteriaIndex = RandomUtils.nextInt(randomCriteria.size());
                Criteria criteriaClone = (Criteria) randomCriteria.get(criteriaIndex).clone();
                criteriaClone.setId(0);
                criteriaSet.add(criteriaClone);
            } else {
                criteriaSet.add(randomCriteria.get(0)); //TODO:  Remove this later.  This is just to clear the first criteria's conjunction.
                int numOfCriteria = org.apache.commons.lang3.RandomUtils.nextInt(1, randomCriteria.size());

                List<Integer> eligibleParentIds = new ArrayList<>();
                eligibleParentIds.add(0);

                for (int j=0; j<numOfCriteria; j++) {
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

            //get suppressNulls
            boolean isSuppressNulls = RandomUtils.nextBoolean();

            //get groupBy
            boolean isGroupBy = RandomUtils.nextBoolean();

            //get OrderBy
            boolean isOrderBy = RandomUtils.nextBoolean();
            boolean isAscending = RandomUtils.nextBoolean();

            //get limit
            long limit = (long) RandomUtils.nextInt(100);

            //get offset
            long offset = (long) RandomUtils.nextInt(100);

            //create select statement with randomized properties
            SelectStatement stmt = new SelectStatement(databaseType);
            stmt.setColumns(columnsList);
            stmt.setTable(table);
            stmt.setCriteria(criteriaSet);
            stmt.setSuppressNulls(isSuppressNulls);
            stmt.setGroupBy(isGroupBy);
            stmt.setOrderBy(isOrderBy);
            stmt.setAscending(isAscending);
            stmt.setLimit(limit);
            stmt.setOffset(offset);

            connection = TestUtils.getConnection(properties);
            ResultSet columnMetaData = connection.getMetaData().getColumns(null, null, "county_spending_detail", "%");

            //ResultSet columnMetaData = new DbConnectionImpl(properties).getConnection().getMetaData().getColumns(null, null, "county_spending_detail", "%");
            stmt.setTableSchema(columnMetaData);
            //stmt.setTableSchema(TestUtils.multiColumnResultSetBuilder(properties));

            //generate SQL statement and add to result list
            //results.put(stmt, sqlBuilder.buildSql(stmt));
            results.put(stmt, stmt.toString());
        }

        return results;
    }

    private List<Criteria> getCriteriaSet() {
        //criteria
        //equal to
        Criteria criteriaEqualTo = new Criteria(0);
        criteriaEqualTo.parentId = null;
        criteriaEqualTo.conjunction = And;
        criteriaEqualTo.column = "fund";
        criteriaEqualTo.operator = equalTo;
        criteriaEqualTo.filter = "Permitting";

        //not equal to
        Criteria criteriaNotEqualTo = new Criteria(1);
        criteriaNotEqualTo.parentId = null;
        criteriaNotEqualTo.conjunction = And;
        criteriaNotEqualTo.column = "fund";
        criteriaNotEqualTo.operator = notEqualTo;
        criteriaNotEqualTo.filter = "Permitting";

        //greater than or equals
        Criteria criteriaGreaterThanOrEquals = new Criteria(2);
        criteriaGreaterThanOrEquals.parentId = null;
        criteriaGreaterThanOrEquals.conjunction = And;
        criteriaGreaterThanOrEquals.column = "fund";
        criteriaGreaterThanOrEquals.operator = greaterThanOrEquals;
        criteriaGreaterThanOrEquals.filter = "Permitting";

        //less than or equals
        Criteria criteriaLessThanOrEquals = new Criteria(3);
        criteriaLessThanOrEquals.parentId = null;
        criteriaLessThanOrEquals.conjunction = And;
        criteriaLessThanOrEquals.column = "fund";
        criteriaLessThanOrEquals.operator = lessThanOrEquals;
        criteriaLessThanOrEquals.filter = "Permitting";

        //greater than
        Criteria criteriaGreaterThan = new Criteria(4);
        criteriaGreaterThan.parentId = null;
        criteriaGreaterThan.conjunction = And;
        criteriaGreaterThan.column = "fund";
        criteriaGreaterThan.operator = greaterThan;
        criteriaGreaterThan.filter = "Permitting";

        //less than
        Criteria criteriaLessThan = new Criteria(5);
        criteriaLessThan.parentId = null;
        criteriaLessThan.conjunction = And;
        criteriaLessThan.column = "fund";
        criteriaLessThan.operator = lessThan;
        criteriaLessThan.filter = "Permitting";

        //like
        Criteria criteriaLike = new Criteria(6);
        criteriaLike.parentId = null;
        criteriaLike.conjunction = And;
        criteriaLike.column = "fund";
        criteriaLike.operator = like;
        criteriaLike.filter = "Permitting";

        //not like
        Criteria criteriaNotLike = new Criteria(7);
        criteriaNotLike.parentId = null;
        criteriaNotLike.conjunction = And;
        criteriaNotLike.column = "fund";
        criteriaNotLike.operator = notLike;
        criteriaNotLike.filter = "Permitting";

        //in
        Criteria criteriaIn = new Criteria(8);
        criteriaIn.parentId = null;
        criteriaIn.conjunction = And;
        criteriaIn.column = "service";
        criteriaIn.operator = in;
        criteriaIn.filter = "Housing and Community Development";

        //not in
        Criteria criteriaNotIn = new Criteria(9);
        criteriaNotIn.parentId = null;
        criteriaNotIn.conjunction = And;
        criteriaNotIn.column = "service";
        criteriaNotIn.operator = notIn;
        criteriaNotIn.filter = "Housing and Community Development";

        //is null
        Criteria criteriaIsNull = new Criteria(10);
        criteriaIsNull.parentId = null;
        criteriaIsNull.conjunction = And;
        criteriaIsNull.column = "service";
        criteriaIsNull.operator = isNull;

        //is not null
        Criteria criteriaIsNotNull = new Criteria(11);
        criteriaIsNotNull.parentId = null;
        criteriaIsNotNull.conjunction = And;
        criteriaIsNotNull.column = "fund";
        criteriaIsNotNull.operator = isNotNull;

        randomCriteria = Arrays.asList(criteriaEqualTo, criteriaNotEqualTo, criteriaGreaterThanOrEquals, criteriaLessThanOrEquals,
                criteriaGreaterThan, criteriaLessThan, criteriaLike, criteriaNotLike, criteriaIn, criteriaNotIn, criteriaIsNull,
                criteriaIsNotNull);

        return randomCriteria;
    }
}
