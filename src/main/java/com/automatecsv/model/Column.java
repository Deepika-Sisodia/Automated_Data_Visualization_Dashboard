package com.automatecsv.model;

import java.util.List;

public class Column {
    private String name;
    private String type;
    private List<String> uniqueValues;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<String> getUniqueValues() { return uniqueValues; }
    public void setUniqueValues(List<String> uniqueValues) { this.uniqueValues = uniqueValues; }
}
