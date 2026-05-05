package com.automatecsv.model;

import java.util.List;
import java.util.Map;

public class Dataset {
    private String id;
    private String fileName;
    private List<Column> columns;
    private List<Map<String, Object>> rows;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public List<Column> getColumns() { return columns; }
    public void setColumns(List<Column> columns) { this.columns = columns; }

    public List<Map<String, Object>> getRows() { return rows; }
    public void setRows(List<Map<String, Object>> rows) { this.rows = rows; }
}
