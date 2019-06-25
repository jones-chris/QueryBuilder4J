package com.querybuilder4j.statements;


import com.querybuilder4j.config.Conjunction;
import com.querybuilder4j.config.Operator;
import com.querybuilder4j.config.Parenthesis;

import java.util.ArrayList;
import java.util.List;

import static com.querybuilder4j.sqlbuilders.SqlCleanser.escape;
import static com.querybuilder4j.sqlbuilders.SqlCleanser.sqlIsClean;
import static java.util.Optional.ofNullable;


public class Criteria implements Cloneable, Comparable {
    private Integer id;
    public Integer parentId;
    public Conjunction conjunction;
    public Parenthesis frontParenthesis;
    public String column;
    public Operator operator;
    public String filter;
    public List<Parenthesis> endParenthesis = new ArrayList<>();


    public Criteria() {}

    public Criteria(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) { this.id = id; }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Conjunction getConjunction() {
        return conjunction;
    }

    public void setConjunction(Conjunction conjunction) {
        this.conjunction = conjunction;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Criteria that = (Criteria) o;
        return this.id.equals(that.id);
    }


    @Override
    public String toString() throws IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        sb.append("Conjunction:  ").append(ofNullable(conjunction).orElse(Conjunction.Empty));
        sb.append("Front Parenthesis:  ").append(ofNullable(frontParenthesis).orElse(Parenthesis.Empty));
        sb.append("Column:  ").append(column);
        sb.append("Operator:  ").append(operator);
        sb.append("Filer:  ").append(ofNullable(filter).orElse(""));

        String endParenthesisString = "";
        if (endParenthesis != null) {
            for (Parenthesis paren : endParenthesis) {
                endParenthesisString += paren;
            }
        }
        sb.append("End Parenthesis:  ").append(endParenthesisString);

        return sb.toString();
    }

    /**
     * Returns the SQL string representation of the criteria in this format:
     * [AND/OR] [FRONT PARENTHESIS] table_name.column_name [OPERATOR] filter [END PARENTHESIS]
     *
     * @param beginningDelimiter
     * @param endingDelimiter
     * @return String
     */
    public String toSql(char beginningDelimiter, char endingDelimiter) {
        String endParenthesisString = "";
        if (this.endParenthesis != null) {
            for (Parenthesis paren : this.endParenthesis) {
                endParenthesisString += paren;
            }
        }

        String[] tableAndColumn = this.column.split("\\.");
        return String.format(" %s %s%s%s%s.%s%s%s %s %s%s ",
                ofNullable(this.conjunction).orElse(Conjunction.Empty),
                ofNullable(this.frontParenthesis).orElse(Parenthesis.Empty),
                beginningDelimiter, escape(tableAndColumn[0]), endingDelimiter,
                beginningDelimiter, escape(tableAndColumn[1]), endingDelimiter,
                this.operator,
                ofNullable(this.filter).orElse(""),
                endParenthesisString);
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        Criteria newCriteria = (Criteria) super.clone();

        newCriteria.id = id;
        newCriteria.parentId = parentId;
        newCriteria.conjunction = conjunction;
        newCriteria.frontParenthesis = frontParenthesis;
        newCriteria.column = column;
        newCriteria.operator = operator;
        newCriteria.filter = filter;
        newCriteria.endParenthesis = endParenthesis;

        return newCriteria;
    }

    public boolean isValid() throws Exception {
        if (column == null) return false;
        if (operator == null) return false;
        return true;
    }

    @Override
    public int compareTo(Object o) {
        if (this.equals(o)) return 0;

        Criteria that = (Criteria) o;

        return this.getId().compareTo(that.getId());
    }

}
