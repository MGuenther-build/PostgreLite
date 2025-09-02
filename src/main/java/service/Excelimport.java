package service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import util.Normalizer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;



public class Excelimport {
	
	public List<Map<String, Object>> readExcel(File file, String startCellRef) throws IOException {
	    List<Map<String, Object>> rows = new ArrayList<>();

	    int startRow = getRowIndex(startCellRef);
	    int startCol = getColIndex(startCellRef);

	    try (FileInputStream fileinput = new FileInputStream(file);
	         Workbook workbook = new XSSFWorkbook(fileinput)) {

	        Sheet sheet = workbook.getSheetAt(0);
	        Row headerRow = sheet.getRow(startRow);
	        if (headerRow == null) {
	            throw new RuntimeException("Keine Header-Zeile gefunden bei " + startCellRef);
	        }

	        List<String> headers = new ArrayList<>();
	        for (int col = startCol; col < headerRow.getLastCellNum(); col++) {
	            Cell cell = headerRow.getCell(col);
	            headers.add(cell != null ? String.valueOf(getCellValue(cell)) : "Spalte_" + col);
	        }

	        for (int rowIndex = startRow + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
	            Row row = sheet.getRow(rowIndex);
	            if (row == null)
	                continue;

	            Map<String, Object> rowData = new LinkedHashMap<>();
	            for (int col = 0; col < headers.size(); col++) {
	                Cell cell = row.getCell(startCol + col);
	                String header = headers.get(col);

	                Object value;
	                if (header.equalsIgnoreCase("PLZ") && cell != null && cell.getCellType() == CellType.NUMERIC) {
	                    value = String.format("%.0f", cell.getNumericCellValue()); // PLZ als String ohne Nachkommastellen
	                } else {
	                    value = getCellValue(cell);
	                }
	                
	                value = Normalizer.normalize(value);
	                rowData.put(header, value);
	            }
	            rows.add(rowData);
	        }
	    }
	    return rows;
	}
	
	private int getRowIndex(String cellRef) {
	    return Integer.parseInt(cellRef.replaceAll("[A-Z]", "")) - 1;
	}

	private int getColIndex(String cellRef) {
	    String letters = cellRef.replaceAll("[0-9]", "").toUpperCase();
	    int col = 0;
	    for (int i = 0; i < letters.length(); i++) {
	        col *= 26;
	        col += letters.charAt(i) - 'A' + 1;
	    }
	    return col - 1;
	}


	
	private Object getCellValue(Cell cell) {
	    if (cell == null)
	    	return "";

	    switch (cell.getCellType()) {
	        case STRING:
	            return cell.getStringCellValue();
	        case NUMERIC:
	            if (DateUtil.isCellDateFormatted(cell)) {
	                return cell.getDateCellValue();
	            } else {
	                return cell.getNumericCellValue();
	            }
	        case BOOLEAN:
	            return cell.getBooleanCellValue();
	        case FORMULA:
	            switch (cell.getCachedFormulaResultType()) {
	                case STRING:
	                    return cell.getStringCellValue();
	                case NUMERIC:
	                    return DateUtil.isCellDateFormatted(cell) ? cell.getDateCellValue() : cell.getNumericCellValue();
	                case BOOLEAN:
	                    return cell.getBooleanCellValue();
	                default:
	                    return cell.getCellFormula(); // Fallback
	            }
	        default:
	            return "";
	    }
	}
}
