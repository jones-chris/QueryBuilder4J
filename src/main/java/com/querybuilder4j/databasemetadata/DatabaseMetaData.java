package com.querybuilder4j.databasemetadata;

import com.querybuilder4j.config.Constants;
import com.querybuilder4j.statements.DatabaseType;
import com.querybuilder4j.statements.SelectStatement;

import java.util.*;

public class DatabaseMetaData {

    private final Properties properties;

    private TablesMetaData tablesMetaData = new TablesMetaData();

    public DatabaseMetaData(Properties properties, SelectStatement stmt) {
        this.properties = properties;

        // Create list with all columns in it - from both columns and criteria collections.
        List<String> allColumns = new ArrayList<>(stmt.getColumns());
        stmt.getCriteria().forEach((criterion) -> allColumns.add(criterion.getColumn()));
        setTablesMetaData(allColumns);
    }

    public Properties getProperties() {
        return properties;
    }

    public TablesMetaData getTablesMetaData() {
        return tablesMetaData;
    }

    public DatabaseType getDatabaseType() {
        return Enum.valueOf(DatabaseType.class, properties.getProperty(Constants.DATABASE_TYPE));
    }

    /**
     * Gets all table schemas for the tables included in the columns and criteria parameters.
     * The function assumes that the columns are in the "table.column" format.
     */
    //todo:  are columns needed in tablesAndColumns?  Only the table is used in the method.
    private void setTablesMetaData(List<String> tablesAndColumns) {
        MetaDataDaoImpl metaDataDao = new MetaDataDaoImpl(properties);

        for (String col : tablesAndColumns){
            String[] tableAndColumn = col.split("\\.");

            if (tableAndColumn.length != 2) {
                throw new RuntimeException("A column needs to be in the format 'table.column'.  The ill-formatted " +
                        "column was " + col);
            }

            String table = tableAndColumn[0];
            if (! this.tablesMetaData.getTableColumnsTypes().containsKey(table)) {
                Map<String, Integer> tableSchema = metaDataDao.getTableSchema(table);
                this.tablesMetaData.getTableColumnsTypes().put(table, tableSchema);
            }
        }
    }


    public class TablesMetaData {
        /**
         * A Map with the values being the stmt's table columns and the values being their JDBC types.
         */
        private Map<String, Map<String, Integer>> tableColumnsTypes = new HashMap<>();

        public TablesMetaData() { }

        public Map<String, Map<String, Integer>> getTableColumnsTypes() {
            return tableColumnsTypes;
        }

        public void setTableColumnsTypes(Map<String, Map<String, Integer>> tableColumnsTypes) {
            this.tableColumnsTypes = tableColumnsTypes;
        }
    }
}
