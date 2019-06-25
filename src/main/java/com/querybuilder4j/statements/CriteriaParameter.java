package com.querybuilder4j.statements;

public class CriteriaParameter {

    /**
     * The name of the criteria parameter.  For example, "@year" would be "year".
     */
    private String name;

    /**
     * The column of the criteria in table.column format.  For example, "employees.first_name".
     */
    private String column;

    /**
     * The description of the parameter.
     */
    private String description;

    public CriteriaParameter() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
