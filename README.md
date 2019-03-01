# QueryBuilder4J
![Build Status](https://codebuild.us-east-1.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoiYWNYVXdQWWlCWmMvWmVOL2tnTXhTZ0dEd3RvQTl5QzBaalJRYXBVMnBQdm5YY0d5RmFzR3dUajBMRlNBVGtrMVVDeG1WVWVzeVZIYytVVjlnblhQblpNPSIsIml2UGFyYW1ldGVyU3BlYyI6IkdJcGdGNTVOVHB3U0l1K3giLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=master)

This repository is the server-side QueryBuilder4J Java library.  If you are looking for the JavaScript UI library for QueryBuilder4J, please go [here](https://github.com/jones-chris/QueryBuilder4J-UI).  

**What is QueryBuilder4J (qb4j)?**

Contrary to what the name of this library might imply, QueryBuilder4J (qb4j) is NOT an ORM (Object Relational Mapper).  ORMs are tools focused on alleviating a developer's work of writing hand-coded SELECT SQL strings.  Qb4j, on the other hand, is intended to alleviate a different audience's work - business users that lack knowledge to write SQL queries.  

Therefore, qb4j is focused on crafting SELECT SQL statements for *business user*, while ORMs are focused on crafting SQL statements for *developers*.

If you’d like to see qb4j in action – both the client side JavaScript library and this server side Java library – please go [here]( http://www.querybuilder4j.net/).  Otherwise, we’ll dive into the details.

Qb4j is designed to encapsulate the parts of a SELECT SQL statement into a single object.  This is convenient for a number of reasons:

1)	Spring framework’s object binding can be utilized to automatically instantiate an object using the request’s attributes.

2)	SELECT SQL statements can be serialized to JSON or XML and stored in a database so that users can retrieve them, run them, or use them as subqueries in their own queries.  If qb4j is integrated into multiple applications, then these queries can be shared across those applications.

3)	All parts of the SELECT SQL statement can be accessed and modified via a single object’s API.  This allows additional logic, such as business rules or security checks to be easily be applied before a statement is run against the database. 

4)	Qb4j is simple to download and use in your project.

5)	The following parts of a SELECT SQL statement are contained in the SelectStatement Java POJO:  Select, From, Where (including subqueries and a boolean property to suppress records returning all null values), Join (Inner, Outer, Left, Right, and Full Outer), Group By, Order By, Limit, Offset. 

**What SQL Dialects Does qb4j Support?**

Qb4j has been successfully tested on PostgreSQL, MySQL, and SQLite.  There are plans to test SQL Server, Oracle, Redshift, and H2 in the future.  

The testing process is rigorous.  A number of randomly generated SELECT SQL statements are built for each database and run against the database.  These randmly generated statements are intended to try many combinations of SELECT SQL statements (with/without joins, nested WHERE clauses, etc).  These tests are dynamic.  In addition, static tests are run.  These static tests are randomly generated statements that failed in the past.  Thus, the static tests serve as regression testing, while the randomly generated (dynamic) tests serve as proactive testing.  

An AWS CodeBuild has been created to automatically build and test qb4j.  

**Concerning SQL Injection**

SQL injenction is a valid concern whenever user input is used in SQL statements.   To help ease these concerns, the web app at  http://www.querybuilder4j.net/ was created as a sandbox for anyone to 1) see a working example of the QueryBuilder4J-UI JavaScript and QueryBuilder4J Java libraries in action and 2) attempt SQL injection.  Everything you see on the landing page is generated using the QueryBuilder4J-UI library.  All calls go to a server running qb4j.  Your SQL injection attempts, if they pass qb4j, will be run against a SQLite database.  You queries will be run with full admin privileges, which means you can run any CRUD (CREATE, READ, UPDATE, DELETE/DROP) statement you please except for DROP DATABASE because that command does not exist in the SQLite SQL dialect.  In the unlikely event that your SQL injection attempt is successful, your query will be logged, a qb4j developer will be notified, and the database will be healed and ready for your next SQL injection attempt.  The developer will review your query, make changes to qb4j, and create a static test to ensure future versions of qb4j prevent your SQL injection attack.  So, please put on your hacker hat and do your worst to get around qb4j's SQL injection defenses - it will only help qb4j get better and alleviate your SQL injection fears.  Happy injecting :)!  

*NOTE  qb4j will do it’s best to prevent SQL injection, but should not be your only defense.  Please read [further](https://www.owasp.org/index.php/SQL_Injection) about SQL injection and how to defend against it.*

**How Does Qb4j Attempt to Prevent SQL Injection?**

Qb4j attempts to prevent SQL injection with the following strategies:

1) All columns must be qualified with the table name in the format "table_name.column_name" and must exist in the target database.

2) All components of the WHERE clause (conjunction, parenthesis, operator), except for the column and filter are Java enum classes.

The first strategy ensures that the user's table and column input exists in the target database before querying on that target database.  This effectively prevents SQL injection attempts using table and column input. 

The second strategy ensures that a ```WHERE``` clause's conjunction, parenthesis, and operator match one of the expected Java enum class values.  If the user's input does not match, then an exception is thrown.

The only place that a SQL injection attempt is possible is a ```WHERE``` clause's filter.  The filter is a String that contains the user's criteria.  For example, if a ```WHERE``` clause was ```AND year = 2019```, then the filter is ```2019```.  Qb4j uses the following strategies to prevent SQL injection in the WHERE clause's filter:

1) Reserved ANSI SQL keywords are searched for and, if found, an exception is thrown.

2) Special characters are escaped, such as ```'```.

3) Textual data types, such as ```VARCHAR``` and ```TEXT```, are wrapped in single quotes.  This prevents SQL injection when a textual data column is chosen for a ```WHERE``` clause.

4) Numeric data types, such as ```INTEGER```, ```BOOLEAN```, and ```FLOAT```, are attempted to be parsed into their respective Java class.  If the parsing fails, then an exception is thrown.  This prevents users from using a numeric data column, which would not be wrapped in single quotes, as the target for a SQL injection attack.  See the ```canParseNonQuotedFilter``` method in the [SqlCleanser](https://github.com/jones-chris/QueryBuilder4J/blob/master/src/main/java/com/querybuilder4j/sqlbuilders/SqlCleanser.java) class for more details.

All of these strategies are implemented in the [SqlCleanser](https://github.com/jones-chris/QueryBuilder4J/blob/master/src/main/java/com/querybuilder4j/sqlbuilders/SqlCleanser.java) class.

**Show Me The Code!!!**

**Just Show Me a Working App...**

Before going any further, if you'd like to see the code for the web application running at http://www.querybuilder4j.net/, please see [this](https://github.com/jones-chris/QueryBuilder4JMVC).  That reference is an example of an application using the QueryBuilder4J-UI JavaScript and QueryBuilder4J Java libraries.  

**Just Start From Square One**

The JAR will be coming soon to Maven Central!

Let's assume for a moment that the JAR is available and you've downloaded it from Maven Central to use in your project.  I'll assume you're using the QueryBuilder4J-UI JavaScript library on the client side of your web app and Spring on the server side.  We'll assume you have a single endpoint that will handle the request that contains the data about the user's SELECT SQL statement they want to build.  Spring will map your endpoint to the controller method if you write the following code:

```Java
    @RequestMapping(value = "/query", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> getQueryResults(SelectStatement selectStatement) {
            try {
              // props is a Properties object containing the database url, username, etc that the generated SQL should be executed against.
              String sql = selectStatement.toSql(props); 
              
              // Run the sql String against your database and get back a JSON string containing the query results.
              String jsonResults = myService.executeSql(sql);

              return new ResponseEntity<>(jsonResults, HttpStatus.OK);
            } catch (Exception ex) {
              return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
    }
```

That's it!  Notice that Spring will take care of instantiating the SelectStatement object, which encapsulates all of the data the user submitted in the request.  All we have to do to get the SQL string from the SelectStatement is call ```toSql(props)``` where ```props``` is a Properties file containing database connection information, such as URL, username, password, database type (PostgreSQL, MySQL, etc), JDBC driver class name, or any other necessary information to connect to your database.

**Qb4j as a SQL Gateway**

Qb4j encapsulates a SELECT SQL statement's data in a single object, the [SelectStatement](https://github.com/jones-chris/QueryBuilder4J/blob/master/src/main/java/com/querybuilder4j/sqlbuilders/statements/SelectStatement.java).  Having all the data encapsulated in one object allows developers to write code that "fine tunes" a user's query before a SQL string is built.  For example, let's say that you want to apply a limit on the query results.  You could do the following:

```Java
@RequestMapping(value = "/query", method = RequestMethod.POST)
@ResponseBody
public ResponseEntity<String> getQueryResults(SelectStatement selectStatement) {
    if (selectStatement.getLimit() > 100000) {
        selectStatement.setLimit(100000);
    }
    
    String sql = selectStatement.toSql(props);
    
    ...Do some other stuff before sending the response...
}
```

In addition, this encapsulation allows developers to easily write their own SQL checks/cleansers for added security or business logic.  Below is an example of what that might look like:

```Java
@RequestMapping(value = "/query", method = RequestMethod.POST)
@ResponseBody
public ResponseEntity<String> getQueryResults(SelectStatement selectStatement) {
    selectStatement.getCriteria().forEach((criterion) -> {
        if (! myCustomSqlCleanser.isCleanSql(criterion.filter)) {
            throw new RuntimeException("Hey!  This is dirty SQL!!!  How dare you!!!");
        }
    });
    
    String sql = selectStatement.toSql(props);
    
    ...Do some other stuff before sending the response...
}
```

This effectively allows qb4j to act in a similar way to an API gateway would - requests are sent to an application using qb4j, qb4j binds the request's data to a ```SelectStatement``` object, and developers can write code to change or check the object before building a SQL string from it.
