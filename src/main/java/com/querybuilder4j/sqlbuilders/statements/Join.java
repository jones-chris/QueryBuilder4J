package com.querybuilder4j.sqlbuilders.statements;


import java.util.ArrayList;
import java.util.List;

public class Join {

    private JoinType joinType;
    private String parentTable;
    private String targetTable;
    private List<String> parentJoinColumns = new ArrayList<>();
    private List<String> targetJoinColumns = new ArrayList<>();

    public enum JoinType {
        LEFT_EXCLUDING {
            @Override
            public String toString() {
                return " LEFT JOIN ";
            }
        },
        LEFT {
            @Override
            public String toString() {
                return " LEFT JOIN ";
            }
        },
        INNER {
            @Override
            public String toString() {
                return " INNER JOIN ";
            }
        },
        FULL_OUTER {
            @Override
            public String toString() {
                return " FULL OUTER JOIN ";
            }
        },
        FULL_OUTER_EXCLUDING {
            @Override
            public String toString() {
                return " FULL OUTER JOIN ";
            }
        },
        RIGHT_EXCLUDING {
            @Override
            public String toString() {
                return " RIGHT JOIN ";
            }
        },
        RIGHT {
            @Override
            public String toString() {
                return " RIGHT JOIN ";
            }
        }
    }

    public Join() { }

    public JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }

    public String getParentTable() {
        return parentTable;
    }

    public void setParentTable(String parentTable) {
        this.parentTable = parentTable;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public List<String> getParentJoinColumns() {
        return parentJoinColumns;
    }

    public void setParentJoinColumns(List<String> parentJoinColumns) {
        this.parentJoinColumns = parentJoinColumns;
    }

    public List<String> getTargetJoinColumns() {
        return targetJoinColumns;
    }

    public void setTargetJoinColumns(List<String> targetJoinColumns) {
        this.targetJoinColumns = targetJoinColumns;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("");
        sb.append("Join Type:  " + joinType + " | ");
        sb.append("Parent Table:  " + parentTable + " | ");
        sb.append("Target Table:  " + targetTable + " | ");

        if (parentJoinColumns.size() != targetJoinColumns.size()) {
            sb.append(String.format("The number of parent join columns (%s) does not match the number of child join columns (%s)",
                    parentJoinColumns.size(),
                    targetJoinColumns.size()));
        }

        for (int i=0; i<parentJoinColumns.size(); i++) {
                sb.append(String.format("Parent Join Column:  %s to Target Join Column:  %s | ",
                        parentJoinColumns.get(i),
                        targetJoinColumns.get(i)));
        }

        return sb.toString();

    }

}
