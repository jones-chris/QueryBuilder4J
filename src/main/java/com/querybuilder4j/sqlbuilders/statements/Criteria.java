package com.querybuilder4j.sqlbuilders.statements;


import com.querybuilder4j.config.Conjunction;
import com.querybuilder4j.config.Operator;
import com.querybuilder4j.config.Parenthesis;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlType(namespace = "Criteria")
@XmlRootElement(namespace = "Criteria")
public class Criteria implements Cloneable, Comparable {
    private Integer id;
    private Integer rank;
    public Integer parentCriteriaId;
    //private boolean orIsNull = false;
    public Conjunction conjunction;
    public Parenthesis frontParenthesis;
    public String column;
    public Operator operator;
    public String filter;
    public Parenthesis endParenthesis;


    public Criteria(Integer id) {
        this.id = id;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

//    public boolean isOrIsNull() {
//        return orIsNull;
//    }
//
//    public void setOrIsNull(boolean orIsNull) {
//        //frontParenthesis = Parenthesis.FrontParenthesis;
//        this.orIsNull = orIsNull;
//        //endParenthesis = Parenthesis.EndParenthesis;
//    }

//    public Conjunction getConjunction() {
//        return conjunction;
//    }
//
//    public void setConjunction(Conjunction conjunction) {
//        this.conjunction = conjunction;
//    }
//
//    public Parenthesis getFrontParenthesis() {
//        return frontParenthesis;
//    }
//
//    public void setFrontParenthesis(Parenthesis frontParenthesis) {
//        this.frontParenthesis = frontParenthesis;
//    }
//
//    public String getColumn() {
//        return column;
//    }
//
//    public void setColumn(String column) {
//        this.column = column;
//    }
//
//    public Operator getOperator() {
//        return operator;
//    }
//
//    public void setOperator(Operator operator) {
//        this.operator = operator;
//    }
//
//    public String getFilter() {
//        return filter;
//    }
//
//    public void setFilter(String filter) {
//        this.filter = filter;
//    }
//
//    public Parenthesis getEndParenthesis() {
//        return endParenthesis;
//    }
//
//    public void setEndParenthesis(Parenthesis endParenthesis) {
//        this.endParenthesis = endParenthesis;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Criteria that = (Criteria) o;
        return this.id.equals(that.id);
//        if (!column.equals(criteria.column)) return false;
//        if (operator != criteria.operator) return false;
//        return filter != null ? filter.equals(criteria.filter) : criteria.filter == null;
    }

//    @Override
//    public int hashCode() {
//        int result = column.hashCode();
//        result = 31 * result + operator.hashCode();
//        result = 31 * result + (filter != null ? filter.hashCode() : 0);
//        return result;
//    }

    @Override
    public String toString() throws IllegalArgumentException {
//        String modifiedFilter;
//        if (isFilterSubquery()) {
//            modifiedFilter = String.format("(%s)", filter);
//        } else if (operator.equals(Operator.between) || operator.equals(Operator.notBetween)) {
//            String[] filterArgs = filter.split(",");
//
//            if (filterArgs.length != 2) throw new IllegalArgumentException();
//
//            modifiedFilter = String.format("%s AND %s", filterArgs[0], filterArgs[1]);
//        } else {
//            modifiedFilter = filter;
//        }
//
//        if (orIsNull && filter != null) {
//            return String.format(" %s %s%s %s %s OR %s IS NULL %s ", conjunction, frontParenthesis, column,
//                    operator.toString(), modifiedFilter, column, endParenthesis);
//        } else if (orIsNull && filter == null) {
//            return String.format(" %s %s IS NULL ", conjunction, column);
//        } else {
//            return String.format(" %s %s%s %s %s%s ",
//                    conjunction, frontParenthesis, column, operator.toString(), modifiedFilter, endParenthesis);
//        }

        return String.format(" %s %s%s %s %s%s ", conjunction, frontParenthesis, column, operator.toString(), filter, endParenthesis);
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        Criteria newCriteria = (Criteria) super.clone();

        //newCriteria.orIsNull = orIsNull;
        newCriteria.id = id;
        newCriteria.rank = rank;
        newCriteria.parentCriteriaId = parentCriteriaId;
        newCriteria.conjunction = conjunction;
        newCriteria.frontParenthesis = frontParenthesis;
        newCriteria.column = column;
        newCriteria.operator = operator;
        newCriteria.filter = filter;
        newCriteria.endParenthesis = endParenthesis;
        //newCriteria.childCriteria = new ArrayList<>(childCriteria);

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
        if ((this.id - that.id) == 0) {
            return this.rank - that.rank;
        } else {
            return this.id - that.id;
        }
    }

//    public boolean isFilterSubquery() {
//        if (filter == null) return false;
//
//        if (filter.length() >= 6) {
//            return (filter.substring(0, 6).toLowerCase().equals("select"));
//        }
//
//        return false;
//    }
}
