package com.querybuilder4j.config;

/**
 * Created by chramy on 3/27/2018.
 */
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
    }
}
