package com.querybuilder4j.statements;

import com.querybuilder4j.config.Constants;

import static com.querybuilder4j.sqlbuilders.SqlCleanser.escape;

/**
 * Data class for a database column.
 */
public class Column {

    private String fullyQualifiedName;
    private String alias = "";

    public Column() { }

    public Column(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    public Column(String fullyQualifiedName, String alias) {
        this.fullyQualifiedName = fullyQualifiedName;
        this.alias = alias;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean hasAlias() {
        return !alias.equals("");
    }

    public String getTable() throws Exception {
        String[] tableAndColumn = fullyQualifiedName.split("\\.");

        if (tableAndColumn.length != 2) {
            throw new Exception("Column must be in the format 'table.column'.  Here is what the column looks like:  "
                    + fullyQualifiedName);
        }

        return tableAndColumn[Constants.TABLE_INDEX];
    }

    public String getColumn() throws Exception {
        String[] tableAndColumn = fullyQualifiedName.split("\\.");

        if (tableAndColumn.length != 2) {
            throw new Exception("Column must be in the format 'table.column'.  Here is what the column looks like:  "
                    + fullyQualifiedName);
        }

        return tableAndColumn[Constants.COLUMN_INDEX];
    }

    public String toSql(char beginningDelimiter, char endingDelimiter) throws Exception {
        if (hasAlias()) {
            return String.format("%s%s%s.%s%s%s AS %s%s%s",
                    beginningDelimiter, escape(getTable()), endingDelimiter,
                    beginningDelimiter, escape(getColumn()), beginningDelimiter,
                    beginningDelimiter, escape(alias), endingDelimiter);
        } else {
            return String.format("%s%s%s.%s%s%s",
                    beginningDelimiter, escape(getTable()), endingDelimiter,
                    beginningDelimiter, escape(getColumn()), beginningDelimiter);
        }
    }
}
