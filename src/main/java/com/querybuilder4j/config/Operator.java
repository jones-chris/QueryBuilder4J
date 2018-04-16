package com.querybuilder4j.config;


public enum Operator {

    equalTo {
        public String toString() {
            return "=";
        }
    },

    notEqualTo {
        public String toString() {
            return "<>";
        }
    },

    greaterThanOrEquals {
        public String toString() {
            return ">=";
        }
    },

    lessThanOrEquals {
        public String toString() {
            return "<=";
        }
    },

    greaterThan {
        public String toString() {
            return ">";
        }
    },

    lessThan {
        public String toString() {
            return "<";
        }
    },

    like {
        public String toString() {
            return "like";
        }
    },

    notLike {
        public String toString() {
            return "not like";
        }
    },

    in {
        public String toString() {
            return "in";
        }
    },

    notIn {
        public String toString() {
            return "not in";
        }
    },

    isNull {
        public String toString() {
            return "is null";
        }
    },

    isNotNull {
        public String toString() {
            return "is not null";
        }
    },

    between {
        public String toString() {
            return "between";
        }
    },

    notBetween {
        public String toString() {
            return "not between";
        }
    }
}
