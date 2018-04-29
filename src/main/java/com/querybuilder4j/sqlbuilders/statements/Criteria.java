package com.querybuilder4j.sqlbuilders.statements;


import com.querybuilder4j.config.Conjunction;
import com.querybuilder4j.config.Operator;
import com.querybuilder4j.config.Parenthesis;
import com.sun.istack.internal.NotNull;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

// ASSUMPTION #1:  criteria ids are ordered when coming from client
// ASSUMPTION #2:  a criteria's parent id refers to a criteria id prior to it.


// id = 0, parent id = null       column1 = filter1
// id = 1, parent id = null       AND (column2 = filter2
    // id = 2, parent id = 1        AND (column3 = filter3
        // id = 3, parent id = 2       OR column3 = filter3.1
        // id = 4, parent id = 2       OR column 3 = filter 3.2)
    // id = 5, parent id = 1        AND column4 = filter4)
// id = 6, parent id = null       AND column5 = filter5

// TRY THIS!!!!
// search if criteria id is a parentid, if true, add beginning parethesis.
// if this index in set == last indexOf criteria with same parentId, then set end parenthesis.



// rule:  if parent id is different from parent id BEFORE it, then set front parenthesis
// rule:  if parent id is different from parent id AFTER it, then set end parenthesis

// if this id == next parent id set beginning parenthesis.
// if this index in set == last indexOf criteria with same parentId, then set end parenthesis.

// find index of last criteria with parentid, if equal to this criteria index, then don't set end parenthesis
// loop through each criteria (using index)
        // find first index of parent

@XmlType(namespace = "Criteria")
@XmlRootElement(namespace = "Criteria")
public class Criteria implements Cloneable, Comparable {
    @NotNull
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Criteria that = (Criteria) o;
        return this.id.equals(that.id);
    }


    @Override
    public String toString() throws IllegalArgumentException {
        String endParenthesisString = "";
        if (endParenthesis != null) {
            for (Parenthesis paren : endParenthesis) {
                endParenthesisString += paren;
            }
        }

        return String.format(" %s %s%s %s %s%s ", ofNullable(conjunction).orElse(Conjunction.Empty),
                                                  ofNullable(frontParenthesis).orElse(Parenthesis.Empty),
                                                  column,
                                                  operator,
                                                  ofNullable(filter).orElse(""),
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

    public boolean isValid() {
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
