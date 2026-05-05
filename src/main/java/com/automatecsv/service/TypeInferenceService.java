package com.automatecsv.service;

import com.automatecsv.model.Column;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class TypeInferenceService {

    public Column inferType(String name, List<Object> values) {
        Column col = new Column();
        col.setName(name);

        if (values.isEmpty()) {
            col.setType("TEXT");
            return col;
        }

        int numCount = 0;
        int dateCount = 0;
        int total = 0;
        Set<String> uniques = new HashSet<>();

        for (Object v : values) {
            if (v == null) continue;
            String s = v.toString().trim();
            if (s.isEmpty()) continue;
            total++;
            uniques.add(s);
            if (isNumber(s)) numCount++;
            else if (isDate(s)) dateCount++;
        }

        if (total == 0) {
            col.setType("TEXT");
            return col;
        }

        if (numCount >= total * 0.8) {
            col.setType("NUMERICAL");
            return col;
        }

        if (dateCount >= total * 0.8) {
            col.setType("TEMPORAL");
            return col;
        }

        col.setType("CATEGORICAL");
        col.setUniqueValues(new ArrayList<>(uniques));
        return col;
    }

    private boolean isNumber(String s) {
        try {
            Double.parseDouble(s.replace(",", ""));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isDate(String s) {
        return s.matches("^\\d{4}-\\d{2}-\\d{2}.*")
            || s.matches("^\\d{1,2}/\\d{1,2}/\\d{4}.*")
            || s.matches("^\\d{1,2}-\\w{3}-\\d{2,4}.*");
    }
}
