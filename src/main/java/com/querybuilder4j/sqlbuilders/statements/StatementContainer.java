package com.querybuilder4j.sqlbuilders.statements;

import java.util.*;

public class StatementContainer {
    private SortedSet<Statement> statements = new TreeSet<>();

    public StatementContainer() {}

    public StatementContainer(TreeSet<Statement> statements) {
        this.statements = statements;

        //call function to add parenthesis
    }

    public Set<Statement> getStatements() {
        return statements;
    }

    public void setStatements(TreeSet<Statement> statements) {
        this.statements = statements;

        //call function to add parenthesis
    }

}
