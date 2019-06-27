package com.querybuilder4j.validators;

import com.querybuilder4j.config.Constants;
import com.querybuilder4j.config.Operator;
import com.querybuilder4j.exceptions.ColumnNameNotFoundException;
import com.querybuilder4j.exceptions.DataTypeNotFoundException;
import com.querybuilder4j.sqlbuilders.SqlCleanser;
import com.querybuilder4j.statements.Criteria;
import com.querybuilder4j.statements.SelectStatement;

import java.util.*;

import static com.querybuilder4j.sqlbuilders.SqlCleanser.sqlIsClean;

public class SelectStatementValidatorImpl implements SelectStatementValidator {

    /**
     * The SelectStatement encapsulating the data to build a SQL string from.
     */
    private final SelectStatement stmt;

    public SelectStatementValidatorImpl(SelectStatement stmt) {
        this.stmt = stmt;
    }

    /**
     * Tests whether each column in the columns parameter has a table and column that can be found in the target database.
     * This method assumes that each column is in the format of [table.column].  After splitting the column on a period (.),
     * the method will throw an exception if the resulting array does not have exactly 2 elements (a table and a column).
     *
     * False is never actually returned - instead an exception will be thrown.  True will be returned if the criteria are valid.
     *
     * @return boolean
     */
    //todo:  add joins' tables and columns here.
    private boolean statementTablesAndColumnsExist() throws Exception {
        // Create list of statement's SELECT columns and WHERE columns.
        List<String> columns = new ArrayList<>(this.stmt.getColumns());
        this.stmt.getCriteria().forEach((criterion) -> columns.add(criterion.getColumn()));

        for (String column : columns) {
            String[] tableAndColumn = column.split("\\.");

            // Check that the tableAndColumn variable has two elements.  The column format should be [table.column].
            if (tableAndColumn.length != 2) { throw new Exception("One of the columns in either the SelectStatement's columns or criteria fields is " +
                    "either is not in the correct 'table.column' format or a column's table name or column name could not be " +
                    "found in the database."); }

            // Now that we know that the tableAndColumn variable has 2 elements, test if the table and column can be found
            // in the database.
            String table = tableAndColumn[Constants.TABLE_INDEX];
            boolean tableIsLegit = getTableColumnsTypes().containsKey(table);
            if (! tableIsLegit) { throw new Exception("This table could not be found in the database:  " + table); }

            String tableColumn = tableAndColumn[Constants.COLUMN_INDEX];
            boolean columnIsLegit = getTableColumnsTypes().get(table).containsKey(tableColumn);
            if (! columnIsLegit) { throw new Exception("This column could not be found in the database table:  " + tableColumn); }
        }

        // Check that statement's table is legit.
        boolean tablesAreValid = getTableColumnsTypes().containsKey(this.stmt.getTable());
        if (! tablesAreValid) { throw new Exception("This table could not be found in the database:  " + this.stmt.getTable()); }

        return true;
    }

    /**
     * Determines if the stmt's criteria are valid.  False is never actually returned - instead an exception will be thrown.
     * True will be returned if the criteria are valid.
     *
     * @return boolean
     * @throws Exception
     */
    private boolean criteriaAreValid() throws Exception {
        for (Criteria criterion : this.stmt.getCriteria()) {
            if (! criterion.isValid()) { throw new Exception("This criteria is not valid:  " + criterion); }

            if (! sqlIsClean(criterion)) { throw new Exception("This criterion failed to be clean SQL:  " + criterion); }

            // Now that we know that the criteria's operator is not 'isNull' or 'isNotNull', we can assume that the
            // criteria's filter is needed.  Therefore, we should check if the filter is null or an empty string.
            // If so, throw an exception.
            if (! criterion.operator.equals(Operator.isNull)) {
                if (! criterion.operator.equals(Operator.isNotNull)) {
                    if (criterion.filter != null) {
                        String[] tableAndColumn = criterion.column.split("\\.");
                        String table = tableAndColumn[Constants.TABLE_INDEX];
                        String column = tableAndColumn[Constants.COLUMN_INDEX];
                        boolean shouldHaveQuotes = isColumnQuoted(table, column, getTableColumnsTypes());

                        if (! shouldHaveQuotes && criterion.filter != null) {
                            String[] filters = criterion.filter.split(",");
                            for (String filter : filters) {
                                int columnDataType = getColumnDataType(tableAndColumn[Constants.TABLE_INDEX], tableAndColumn[Constants.COLUMN_INDEX]);
                                if (! SqlCleanser.canParseNonQuotedFilter(filter, columnDataType)) {
                                    throw new Exception("The criteria's filter is not an number type, " +
                                            "but the column is a number type:  " + criterion);
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     *
     * First, gets the SQL JDBC Type for the table and column parameters.  Then, gets a boolean from the typeMappings
     * class field associated with the SQL JDBC Types parameter, which is an int.  The typeMappings field will return
     * true if the SQL JDBC Types parameter should be quoted in a WHERE SQL clause and false if it should not be quoted.
     *
     * For example, the VARCHAR Type will return true, because it should be wrapped in single quotes in a WHERE SQL condition.
     * On the other hand, the INTEGER Type will return false, because it should NOT be wrapped in single quotes in a WHERE SQL condition.
     *
     * @param table
     * @param columnName
     * @return boolean
     * @throws DataTypeNotFoundException
     * @throws ColumnNameNotFoundException
     */
    public static boolean isColumnQuoted(String table, String columnName, Map<String, Map<String, Integer>> tableSchemas) throws DataTypeNotFoundException, ColumnNameNotFoundException {
        Integer dataType = getColumnDataType(table, columnName, tableSchemas);

        Boolean isQuoted = Constants.TYPE_MAPPINGS.get(dataType); //todo:  make typeMappings a public static field in SelectStatementValidator so that it can be called?  Maybe even put it in Constants class because it's called by SelectStatementValidator and SqlBuilder?

        if (isQuoted == null) { throw new DataTypeNotFoundException(String.format("Data type, %s, is not recognized", dataType)); }

        return isQuoted;
    }

    /**
     * Gets the SQL JDBC Type for the table and column parameters.
     *
     * @param table
     * @param columnName
     * @return int
     * @throws ColumnNameNotFoundException
     */
    // todo:  is this method needed anymore now that we have a static method by same name and isColumnQuoted is static also?
    public int getColumnDataType(String table, String columnName) throws ColumnNameNotFoundException {
        return getColumnDataType(table, columnName, getTableColumnsTypes());
    }

    /**
     * Gets the SQL JDBC Type for the table and column parameters.
     *
     * @param table
     * @param columnName
     * @return int
     * @throws ColumnNameNotFoundException
     */
    public static int getColumnDataType(String table, String columnName, Map<String, Map<String, Integer>> tableSchemas) throws ColumnNameNotFoundException {
        Integer dataType = tableSchemas.get(table).get(columnName); //todo:  pass tableSchemas as parameter into SqlBuilder from SelectStatementValidator?  Because SelectStatementValidator already got tableSchemas.

        if (dataType == null) {
            throw new ColumnNameNotFoundException("Could not find column:  " + columnName);
        } else {
            return dataType;
        }
    }

    private Map<String, Map<String, Integer>> getTableColumnsTypes() {
        return this.stmt.getDatabaseMetaData().getTablesMetaData().getTableColumnsTypes();
    }

    public boolean passesBasicValidation() throws Exception {
        // Test if stmt passes basic validation.
        if (this.stmt.getColumns() == null) { throw new Exception("Columns is null"); }

        if (this.stmt.getColumns().size() == 0) { throw new Exception("Columns has no elements"); }

        if (this.stmt.getTable() == null ||
            this.stmt.getTable().equals("")) { throw new Exception("The Table cannot be null or an empty string."); }

        if (this.stmt.getJoins() == null) { throw new Exception("Joins is null"); }

        if (this.stmt.getCriteria() == null) { throw new Exception("The Criteria is null"); }

        // Test if stmt criteria pass basic validation.
        for (Criteria criterion : this.stmt.getCriteria()) {
            criterion.isValid(); // Will throw exception instead of false.
        }

        // todo:  test if stmt joins pass basic validation.

        return true;
    }

    public boolean passesDatabaseValidation() throws Exception {
        statementTablesAndColumnsExist(); // Will throw exception instead of false.
        criteriaAreValid(); // Will throw exception instead of false.
        return true;
    }
}
