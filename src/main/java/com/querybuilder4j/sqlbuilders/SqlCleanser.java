package com.querybuilder4j.sqlbuilders;


public class SqlCleanser {
    private static final Character[] charsNeedingEscaping = new Character[] {'\'', '"'};
    private static final String[] stringsNeedingRemoval = new String[] {"-", "=", ">", "<", "!=", "<>", ">=", "<=", ";",
            " DROP ", " CREATE ", " DELETE ", " INSERT ", " UPDATE ", " SELECT ",  " FROM ","`"};

    public static String escape(String sql) {
        for (Character c : charsNeedingEscaping) {
            sql = sql.replaceAll(c.toString(), c.toString() + c.toString());
        }

        return sql;
    }

    public static String remove(String sql) {
        for (String s : stringsNeedingRemoval) {
            sql = sql.replaceAll(s, "");
        }

        return sql;
    }

    public static String escapeAndRemove(String sql) {
        sql = escape(sql);
        sql = remove(sql);
        return sql;
    }
}
