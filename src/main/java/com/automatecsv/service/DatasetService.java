package com.automatecsv.service;

import com.automatecsv.model.Dataset;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DatasetService {

    private final Map<String, Dataset> storage = new ConcurrentHashMap<>();

    public void save(Dataset dataset) {
        storage.put(dataset.getId(), dataset);
    }

    public Dataset findById(String id) {
        return storage.get(id);
    }

    public List<Map<String, Object>> getFilteredRows(String id, Map<String, Object> filters) {
        Dataset ds = findById(id);
        if (ds == null) return new ArrayList<>();
        if (filters == null || filters.isEmpty()) return ds.getRows();
        return ds.getRows().stream().filter(row -> matchesAll(row, filters)).collect(Collectors.toList());
    }

    public boolean updateRow(String id, int rowIndex, Map<String, Object> newData) {
        Dataset ds = findById(id);
        if (ds == null || rowIndex < 0 || rowIndex >= ds.getRows().size()) return false;
        ds.getRows().set(rowIndex, newData);
        return true;
    }

    public boolean deleteRow(String id, int rowIndex) {
        Dataset ds = findById(id);
        if (ds == null || rowIndex < 0 || rowIndex >= ds.getRows().size()) return false;
        ds.getRows().remove(rowIndex);
        return true;
    }

    public boolean addRow(String id, Map<String, Object> newRow) {
        Dataset ds = findById(id);
        if (ds == null) return false;
        ds.getRows().add(newRow);
        return true;
    }

    private boolean matchesAll(Map<String, Object> row, Map<String, Object> filters) {
        for (var entry : filters.entrySet()) {
            Object filterVal = entry.getValue();
            Object cellVal = row.get(entry.getKey());

            if (filterVal instanceof List<?> list) {
                if (!list.isEmpty()) {
                    String cell = cellVal != null ? cellVal.toString() : null;
                    if (!list.contains(cell)) return false;
                }
            } else if (filterVal instanceof Map<?, ?> range) {
                String cellStr = cellVal != null ? cellVal.toString() : "";

                if (range.containsKey("start") || range.containsKey("end")) {
                    String start = range.get("start") != null ? range.get("start").toString() : "";
                    String end = range.get("end") != null ? range.get("end").toString() : "";
                    if (!start.isEmpty() && cellStr.compareTo(start) < 0) return false;
                    if (!end.isEmpty() && cellStr.compareTo(end) > 0) return false;
                } else {
                    double cell = toDouble(cellVal);
                    if (range.get("min") != null && !range.get("min").toString().isEmpty()) {
                        if (cell < Double.parseDouble(range.get("min").toString())) return false;
                    }
                    if (range.get("max") != null && !range.get("max").toString().isEmpty()) {
                        if (cell > Double.parseDouble(range.get("max").toString())) return false;
                    }
                }
            }
        }
        return true;
    }

    private double toDouble(Object val) {
        if (val instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0; }
    }
}
