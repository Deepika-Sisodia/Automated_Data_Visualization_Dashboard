package com.automatecsv.service;

import com.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

@Service
public class FileParserService {

    public List<Map<String, Object>> parse(MultipartFile file) throws Exception {
        String name = file.getOriginalFilename();
        if (name != null && name.toLowerCase().endsWith(".csv")) {
            return parseCsv(file);
        }
        return parseExcel(file);
    }

    private List<Map<String, Object>> parseCsv(MultipartFile file) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csv = new CSVReader(reader)) {
            String[] headers = csv.readNext();
            if (headers == null) return rows;
            String[] line;
            while ((line = csv.readNext()) != null) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.length && i < line.length; i++) {
                    row.put(headers[i].trim(), smartParse(line[i]));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    private List<Map<String, Object>> parseExcel(MultipartFile file) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() == 0) throw new Exception("Empty sheet");

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new Exception("No headers");

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) headers.add(cell.getStringCellValue().trim());

            DataFormatter fmt = new DataFormatter();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Map<String, Object> data = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null) {
                        data.put(headers.get(j), null);
                    } else if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                        data.put(headers.get(j), cell.getDateCellValue().toString());
                    } else if (cell.getCellType() == CellType.NUMERIC) {
                        data.put(headers.get(j), cell.getNumericCellValue());
                    } else {
                        data.put(headers.get(j), smartParse(fmt.formatCellValue(cell)));
                    }
                }
                rows.add(data);
            }
        }
        return rows;
    }

    private Object smartParse(String val) {
        if (val == null || val.trim().isEmpty()) return null;
        val = val.trim();
        try {
            return Double.parseDouble(val.replace(",", ""));
        } catch (NumberFormatException e) {
            return val;
        }
    }
}
