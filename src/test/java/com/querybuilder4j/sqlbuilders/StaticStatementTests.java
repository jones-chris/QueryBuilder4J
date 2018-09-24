package com.querybuilder4j.sqlbuilders;

import com.querybuilder4j.Constants;
import com.querybuilder4j.TestUtils;
import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.sqlbuilders.statements.Criteria;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;
import com.querybuilder4j.utils.ResultSetToHashMapConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.querybuilder4j.config.Conjunction.And;
import static com.querybuilder4j.config.Operator.*;
import static com.querybuilder4j.config.Operator.lessThanOrEquals;

public class StaticStatementTests {

    public StaticStatementTests() {}

    public HashMap<Object, Object> buildSql_FailedTest_PostgreSQL1() throws Exception {
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

        List<Criteria> criteria = new ArrayList<>();
        Criteria c0 = new Criteria(0);
        c0.parentId = null;
        c0.conjunction = And;
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
//        stmt.setTableSchema(ResultSetToHashMapConverter.toHashMap(TestUtils.multiColumnResultSetBuilder(Constants.dbProperties.get(stmt.getDatabaseType())))); //TODO:  FIX THIS.

        HashMap<Object, Object> results = new HashMap<>();
        results.put("stmt", stmt);
        results.put("sql", stmt.toSql());
        return results;
    }

}
