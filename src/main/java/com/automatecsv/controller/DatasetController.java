package com.automatecsv.controller;

import com.automatecsv.model.Column;
import com.automatecsv.model.Dataset;
import com.automatecsv.service.DatasetService;
import com.automatecsv.service.FileParserService;
import com.automatecsv.service.TypeInferenceService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class DatasetController {

    private final FileParserService fileParser;
    private final TypeInferenceService typeInference;
    private final DatasetService datasetService;
    private final ObjectMapper mapper;

    public DatasetController(FileParserService fileParser, TypeInferenceService typeInference,
                             DatasetService datasetService, ObjectMapper mapper) {
        this.fileParser = fileParser;
        this.typeInference = typeInference;
        this.datasetService = datasetService;
        this.mapper = mapper;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            List<Map<String, Object>> rows = fileParser.parse(file);
            if (rows.isEmpty()) return error("Empty file");

            List<Column> columns = inferColumns(rows);

            Dataset ds = new Dataset();
            ds.setId(UUID.randomUUID().toString());
            ds.setFileName(file.getOriginalFilename());
            ds.setColumns(columns);
            ds.setRows(rows);
            datasetService.save(ds);

            Map<String, Object> res = new HashMap<>();
            res.put("datasetId", ds.getId());
            res.put("columns", columns);
            res.put("rowCount", rows.size());
            res.put("preview", rows.subList(0, Math.min(5, rows.size())));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return error("Parse error: " + e.getMessage());
        }
    }

    @GetMapping("/dataset/{id}")
    public ResponseEntity<?> getData(@PathVariable String id, @RequestParam(required = false) String filters) {
        try {
            Map<String, Object> f = (filters != null && !filters.isEmpty())
                ? mapper.readValue(filters, new TypeReference<>() {}) : new HashMap<>();
            return ResponseEntity.ok(datasetService.getFilteredRows(id, f));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/dataset/{id}/columns")
    public ResponseEntity<?> getColumns(@PathVariable String id) {
        Dataset ds = datasetService.findById(id);
        if (ds == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ds.getColumns());
    }

    @PutMapping("/dataset/{id}/row/{index}")
    public ResponseEntity<?> updateRow(@PathVariable String id, @PathVariable int index,
                                       @RequestBody Map<String, Object> rowData) {
        boolean ok = datasetService.updateRow(id, index, rowData);
        if (!ok) return error("Row not found");
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/dataset/{id}/row/{index}")
    public ResponseEntity<?> deleteRow(@PathVariable String id, @PathVariable int index) {
        boolean ok = datasetService.deleteRow(id, index);
        if (!ok) return error("Row not found");
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/dataset/{id}/row")
    public ResponseEntity<?> addRow(@PathVariable String id, @RequestBody Map<String, Object> rowData) {
        boolean ok = datasetService.addRow(id, rowData);
        if (!ok) return error("Dataset not found");
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/export/{id}")
    public ResponseEntity<byte[]> export(@PathVariable String id) {
        try {
            List<Map<String, Object>> rows = datasetService.getFilteredRows(id, new HashMap<>());
            if (rows.isEmpty()) return ResponseEntity.notFound().build();

            Workbook wb = new XSSFWorkbook();
            Sheet sheet = wb.createSheet("Report");
            List<String> headers = new ArrayList<>(rows.get(0).keySet());

            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) header.createCell(i).setCellValue(headers.get(i));

            for (int i = 0; i < rows.size(); i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < headers.size(); j++) {
                    Object val = rows.get(i).get(headers.get(j));
                    if (val instanceof Number) row.createCell(j).setCellValue(((Number) val).doubleValue());
                    else if (val != null) row.createCell(j).setCellValue(val.toString());
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            wb.close();

            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            h.setContentDispositionFormData("attachment", "report.xlsx");
            return new ResponseEntity<>(out.toByteArray(), h, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    private List<Column> inferColumns(List<Map<String, Object>> rows) {
        List<Column> cols = new ArrayList<>();
        for (String key : rows.get(0).keySet()) {
            List<Object> values = rows.stream().map(r -> r.get(key)).collect(Collectors.toList());
            cols.add(typeInference.inferType(key, values));
        }
        return cols;
    }

    private ResponseEntity<?> error(String msg) {
        return ResponseEntity.badRequest().body(Map.of("message", msg));
    }
}
