package service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javafx.collections.ObservableList;
import java.io.*;
import java.util.Date;
import java.util.List;



public class ExportData {

    public void exportToExcel(File file, List<String> headers, ObservableList<ObservableList<String>> data) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Export");

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
        }
        
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i + 1);
            ObservableList<String> rowData = data.get(i);
            for (int j = 0; j < rowData.size(); j++) {
                Cell cell = row.createCell(j);
                setTypedCellValue(cell, rowData.get(j));
            }
        }

        try (FileOutputStream out = new FileOutputStream(file)) {
            workbook.write(out);
        }
        workbook.close();
    }

    
    
    public void exportToCSV(File file, List<String> headers, ObservableList<ObservableList<String>> data) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(String.join(";", headers));
            writer.newLine();

            for (ObservableList<String> row : data) {
                List<String> escaped = row.stream()
                    .map(this::escapeCSV)
                    .toList();
                writer.write(String.join(";", escaped));
                writer.newLine();
            }
        }
    }
    
    
    
    private void setTypedCellValue(Cell cell, String value) {
        if (value == null || value.isEmpty()) {
            cell.setBlank();
            return;
        }

        // Versuche Zahl
        try {
            double number = Double.parseDouble(value);
            cell.setCellValue(number);
            return;
        } catch (NumberFormatException ignored) {}

        // Versuche Boolean
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            cell.setCellValue(Boolean.parseBoolean(value));
            return;
        }

        // Versuche Datum
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(value);
            cell.setCellValue(date);
            CellStyle dateStyle = cell.getSheet().getWorkbook().createCellStyle();
            CreationHelper createHelper = cell.getSheet().getWorkbook().getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));
            cell.setCellStyle(dateStyle);
            return;
        // Fallback zu String
        } catch (Exception ignored) {}
        cell.setCellValue(value);
    }
    
    
    
    private String escapeCSV(String value) {
        if (value == null) return "";

        boolean needsQuotes = value.contains(";") || value.contains("\"") || value.contains("\n");

        if (needsQuotes) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }
}
