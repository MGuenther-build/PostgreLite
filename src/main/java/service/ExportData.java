package service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javafx.collections.ObservableList;
import java.io.*;
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
                cell.setCellValue(rowData.get(j));
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
                writer.write(String.join(";", row));
                writer.newLine();
            }
        }
    }
}
