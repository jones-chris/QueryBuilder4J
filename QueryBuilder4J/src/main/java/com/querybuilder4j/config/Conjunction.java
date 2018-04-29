package com.querybuilder4j.config;


public enum Conjunction {
    And {
        @Override
        public String toString() {
            return "And";
        }
    },
    Or {
        @Override
        public String toString() {
            return "Or";
        }
    },
    Empty {
        @Override
        public String toString() {
            return "";
        }
    }
}
