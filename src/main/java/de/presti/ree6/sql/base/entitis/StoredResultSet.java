package de.presti.ree6.sql.base.entitis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class used to store all data from a ResultSet for further use.
 */
public class StoredResultSet {

    /**
     * The amount of Columns in the ResultSet.
     */
    private int columnCount;

    /**
     * The amount of Rows in the ResultSet.
     */
    private int rowsCount;

    /**
     * All the data from a ResultSet.
     */
    private final List<List<Object>> data = new ArrayList<>();

    /**
     * Mappings to map a Column-Name to its respected index.
     */
    private final Map<String, Integer> columnMappings = new HashMap<>();

    /**
     * Set the Columns count of the ResultSet.
     * @param columnCount the Columns count of the ResultSet.
     */
    public void setColumns(int columnCount) {
        this.columnCount = columnCount;
    }

    /**
     * Set the Rows count of the ResultSet.
     * @param rowsCount the Rows count of the ResultSet.
     */
    public void setRows(int rowsCount) {
        this.rowsCount = rowsCount;
    }

    /**
     * Set the data from a ResultSet.
     * @param row the Row of the data.
     * @param column the Column of the data.
     * @param value the value of the data.
     */
    public void setValue(int row, int column, Object value) {
        if (row >= data.size()) {
            data.add(new ArrayList<>());
        }

        if (column >= data.get(row).size()) {
            data.get(row).add(null);
        }

        data.get(row).set(column - 1, value);
    }

    /**
     * Add a Column.
     * @param column the Column to add.
     * @param name the name of the Column.
     */
    public void addColumn(int column, String name) {
        columnMappings.put(name, column - 1);
    }

    /**
     * Get the value of a Column.
     * @param name the name of the Column.
     * @return the value of the Column.
     */
    public int getColumnByName(String name) {
        return columnMappings.getOrDefault(name, -1);
    }

    /**
     * Get the value of a Column.
     * @param index the index of the Column.
     * @return the value of the Column.
     */
    public String getColumnName(int index) {
        if (!columnMappings.containsValue(index)) {
            return "";
        }

        return columnMappings.entrySet().stream().filter(e -> e.getValue() == index).findFirst().map(Map.Entry::getKey).orElse("");
    }

    /**
     * Get the value of a specific Column in the first row.
     * @param name the name of the column.
     * @return the value of the row.
     */
    public Object getValue(String name) {
        int columnIndex = getColumnByName(name);

        if (data.isEmpty() || columnIndex == -1) {
            return new Object();
        }

        return data.get(0).get(columnIndex);
    }

    /**
     * Get the value of a specific Column in all rows.
     * @param name the name of the column.
     * @return all values from the column.
     */
    public List<Object> getValues(String name) {
        int columnIndex = getColumnByName(name);

        if (data.isEmpty() || columnIndex == -1) {
            return new ArrayList<>();
        }

        return data.stream().map(l -> l.get(columnIndex)).toList();
    }

    /**
     * Get all data from the ResultSet.
     * @return all data from the ResultSet.
     */
    public List<List<Object>> getData() {
        return data;
    }

    /**
     * Get all parsed data from a row.
     * @return all parsed data from a row.
     */
    public List<StoredData> getStoredData() {
        List<StoredData> storedDataList = new ArrayList<>();


        for (List<Object> entry : data) {

            StoredData storedData = new StoredData();

            for (int i = 0; i < entry.size(); i++) {
                storedData.add(i, entry.get(i));
            }

            storedDataList.add(storedData);
        }

        return storedDataList;
    }

    /**
     * The amount of columns in the ResultSet.
     * @return the amount of columns in the ResultSet.
     */
    public int getColumnCount() {
        return columnCount;
    }

    /**
     * The amount of rows in the ResultSet.
     * @return the amount of rows in the ResultSet.
     */
    public int getRowsCount() {
        return rowsCount;
    }

    /**
     * Check if the ResultSet had any data.
     * @return true if the ResultSet had any data.
     */
    public boolean hasResults() {
        return getRowsCount() > 0;
    }

    /**
     * A class used to represent a Row in the ResultSet.
     */
    public class StoredData {

        /**
         * The data from a Row.
         */
        private final List<Object> rowData = new ArrayList<>();

        /**
         * Get the value of a specific Column in the first row.
         * @param name the name of the column.
         * @return the value of the row.
         */
        public Object getValue(String name) {
            int columnIndex = getColumnByName(name);

            if (rowData.isEmpty() || columnIndex == -1) {
                return new Object();
            }

            return rowData.get(columnIndex);
        }

        /**
         * Add data to a specific Column.
         * @param columIndex the index of the Column.
         * @param object the value of the data.
         */
        public void add(int columIndex, Object object) {
            if (columIndex >= rowData.size()) {
                rowData.add(null);
            }

            rowData.set(columIndex, object);
        }
    }
}
