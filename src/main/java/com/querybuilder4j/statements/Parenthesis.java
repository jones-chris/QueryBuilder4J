package com.querybuilder4j.statements;


public enum Parenthesis {
    FrontParenthesis {
        public String toString() {
            return "(";
        }
    },

    EndParenthesis {
        public String toString() {
            return ")";
        }
    },

    Empty {
        @Override
        public String toString() {
            return "";
        }
    }
}
