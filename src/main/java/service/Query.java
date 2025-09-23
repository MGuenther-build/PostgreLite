package service;

import java.math.BigDecimal;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import util.Normalizer;



public class Query {

	public QueryResult executeQuery(String dbName, String sql) throws SQLException {
	    List<Map<String, Object>> result = new ArrayList<>();
	    List<String> columnNames = new ArrayList<>();
	    List<String> sqlTypes = new ArrayList<>();

	    try (Connection conn = ConnectionManager.getConnection(dbName);
	         Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(sql)) {

	        ResultSetMetaData meta = rs.getMetaData();
	        int columnCount = meta.getColumnCount();

	        // Spaltennamen + SQL-Typen kopieren
	        for (int i = 1; i <= columnCount; i++) {
	            columnNames.add(meta.getColumnLabel(i));
	            String typeName = meta.getColumnTypeName(i);
	            int precision = meta.getPrecision(i);          // z. B. 100 bei VARCHAR(100)
	            int scale = meta.getScale(i);                  // z. B. 2 bei NUMERIC(10,2)
	            
	            String fullType;
	            if (precision > 0) {
	                if (scale > 0) {
	                    fullType = typeName + "(" + precision + "," + scale + ")";
	                } else {
	                    fullType = typeName + "(" + precision + ")";
	                }
	            } else {
	                fullType = typeName;
	            }
	            sqlTypes.add(fullType);
	        }

	        // Daten auslesen
	        while (rs.next()) {
	            Map<String, Object> row = new LinkedHashMap<>();
	            for (int i = 1; i <= columnCount; i++) {
	                Object value = rs.getObject(i);
	                int columnType = meta.getColumnType(i);

	                if (value instanceof BigDecimal) {
	                    BigDecimal bd = (BigDecimal) value;
	                    switch (columnType) {
	                        case Types.INTEGER:
	                        case Types.SMALLINT:
	                        case Types.TINYINT:
	                            value = bd.intValue();
	                            break;
	                        case Types.BIGINT:
	                            value = bd.longValue();
	                            break;
	                        case Types.NUMERIC:
	                        case Types.DECIMAL:
	                            if (bd.stripTrailingZeros().scale() <= 0) {
	                                if (bd.compareTo(BigDecimal.valueOf(Integer.MIN_VALUE)) >= 0 &&
	                                    bd.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0) {
	                                    value = bd.intValue();
	                                } else {
	                                    value = bd.longValue();
	                                }
	                            } else {
	                                value = bd.doubleValue();
	                            }
	                            break;
	                        default:
	                            value = Normalizer.normalize(bd);
	                    }
	                } else {
	                    value = Normalizer.normalize(value);
	                }
	                row.put(meta.getColumnLabel(i), value);
	            }
	            result.add(row);
	        }
	    }
	    return new QueryResult(result, columnNames, sqlTypes);
	}

    
    
    public int executeUpdate(String dbName, String sql) throws SQLException {
        try (Connection conn = ConnectionManager.getConnection(dbName);
             Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }
    
    
    
    public void insertData(String dbName, String tableName, List<Map<String, Object>> rows) throws SQLException {
        if (rows.isEmpty())
            return;

        String columns = rows.get(0).keySet().stream()
        	    .map(col -> "\"" + col + "\"")
        	    .collect(Collectors.joining(", "));
        
        String placeholders = rows.get(0).keySet().stream()
            .map(k -> "?").collect(Collectors.joining(", "));

        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
        // System.out.println("SQL: " + sql);
        // for (Map<String, Object> row : rows) {
            // System.out.println("Row: " + row);
        // }
        
        try (Connection conn = ConnectionManager.getConnection(dbName);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Map<String, Object> row : rows) {
                int index = 1;
                for (Object value : row.values()) {
                    stmt.setObject(index++, value);
                }
                stmt.addBatch();
            }
            stmt.executeBatch();
        
	    } catch (BatchUpdateException bue) {
	        System.err.println("BatchUpdateException: " + bue.getMessage());
	        SQLException next = bue.getNextException();
	        if (next != null) {
	            next.printStackTrace();
	        }
	    }
    }  
    
    
    
    public boolean tableExists(String dbName, String tableName) throws SQLException {
        String sql = "SELECT EXISTS (" +
                     "SELECT FROM information_schema.tables " +
                     "WHERE table_schema = 'public' AND table_name = ?" +
                     ")";
        try (Connection conn = ConnectionManager.getConnection(dbName);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return result.getBoolean(1);
                }
            }
        }
        return false;
    }

    
    
    public void createTableFromExcel(String dbName, String tableName, QueryResult result) throws SQLException {
        List<Map<String, Object>> rows = result.getRows();
        List<String> columnNames = result.getColumnNames();
        List<String> sqlTypes = result.getSqlTypes();

        if (rows == null || rows.isEmpty())
        	return;

        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sb.append(tableName).append(" (");

        for (int i = 0; i < columnNames.size(); i++) {
            sb.append("\"").append(columnNames.get(i)).append("\" ").append(sqlTypes.get(i)).append(", ");
        }

        sb.setLength(sb.length() - 2);
        sb.append(");");

        try (Connection conn = ConnectionManager.getConnection(dbName);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sb.toString());
        }
    }
}
