package com.querybuilder4j;

import com.querybuilder4j.sqlbuilders.SqlBuilder;
import com.querybuilder4j.sqlbuilders.statements.Criteria;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;
import org.apache.commons.lang.math.RandomUtils;

import java.util.*;

import static com.querybuilder4j.config.Conjunction.*;
import static com.querybuilder4j.config.Operator.*;

public class QueryTests {
    private SqlBuilder sqlBuilder;
    private Properties properties;
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
    //private Map<String, Map<Integer, List<Object>>> randomizerChoices = new HashMap<>();

    public QueryTests(SqlBuilder sqlBuilder, Properties properties) {
        this.sqlBuilder = sqlBuilder;
        this.properties = properties;
        columns.add("fund");
        columns.add("service");

        //columns
        randomColumns.add("fund");
        randomColumns.add("service");
        randomColumns.add("fiscal_year_period");

        //tables
        randomTables.add("county_spending_detail");

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
    }

    public SelectStatement createNewMainSelectStmt() throws Exception {
//        FileReader reader = new FileReader("postgresql.properties");
//
//        Properties props = new Properties();
//        props.load(reader);

        SelectStatement stmt = new SelectStatement("main");
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

        return sqlBuilder.buildSql(stmt);
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

        return sqlBuilder.buildSql(stmt);
    }

    public String buildSql_WithDistinct() throws Exception {
        SelectStatement stmt = createNewMainSelectStmt();
        stmt.setDistinct(true);

        return sqlBuilder.buildSql(stmt);
    }

    public String buildSql_GroupBy() throws Exception {
        SelectStatement stmt = createNewMainSelectStmt();
        stmt.setGroupBy(true);

        return sqlBuilder.buildSql(stmt);
    }

    public String buildSql_OrderBy() throws Exception {
        SelectStatement stmt = createNewMainSelectStmt();
        stmt.setOrderBy(true);

        return sqlBuilder.buildSql(stmt);
    }

    public String buildSql_GroupByOrderBy() throws Exception {
        SelectStatement stmt = createNewMainSelectStmt();
        stmt.setOffset(0);
        stmt.setGroupBy(true);
        stmt.setOrderBy(true);

        return sqlBuilder.buildSql(stmt);
    }

    public String buildSql_SuppressNulls() throws Exception {
        SelectStatement stmt = createNewMainSelectStmt();
        stmt.setSuppressNulls(true);

        return sqlBuilder.buildSql(stmt);
    }

    public List<String> buildSql_randomizer() throws Exception {
        List<String> results = new ArrayList<>();

        //i is the number of sql statements to output.
        for (int i=0; i<10; i++) {

            //get columns
            boolean getSingleColumn = RandomUtils.nextBoolean();
            List<String> columnsList = new ArrayList<>();
            if (getSingleColumn) {
                columnsList.add(randomColumns.get(0));
            } else {
                columnsList.add(randomColumns.get(0));
                columnsList.add(randomColumns.get(2));
            }

            //get table
            int tablesIndex = RandomUtils.nextInt(randomTables.size());
            String table = randomTables.get(tablesIndex);

            //get criteria
            boolean getSingleCriteria = RandomUtils.nextBoolean();
            SortedSet<Criteria> criteriaSet = new TreeSet<>();
            if (getSingleCriteria) {
                int criteriaIndex = RandomUtils.nextInt(randomCriteria.size());
                criteriaSet.add(randomCriteria.get(0));
            } else {
                criteriaSet.add(randomCriteria.get(0));
                criteriaSet.add(randomCriteria.get(3));
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
            SelectStatement stmt = new SelectStatement();
            stmt.setColumns(columnsList);
            stmt.setTable(table);
            stmt.setCriteria(criteriaSet);
            stmt.setSuppressNulls(isSuppressNulls);
            stmt.setGroupBy(isGroupBy);
            stmt.setOrderBy(isOrderBy);
            stmt.setAscending(isAscending);
            stmt.setLimit(limit);
            stmt.setOffset(offset);
            stmt.setTableSchema(TestUtils.multiColumnResultSetBuilder(properties));

            //generate SQL statement and add to result list
            results.add(sqlBuilder.buildSql(stmt));
        }

        return results;
    }
}
