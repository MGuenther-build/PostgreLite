package service;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



public class Query {

    public List<Map<String, Object>> executeQuery(String dbName, String sql) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection(dbName);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                result.add(row);
            }
        }

        return result;
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

        System.out.println("SQL: " + sql);
        for (Map<String, Object> row : rows) {
            System.out.println("Row: " + row);
        }
        
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
    
    
    
    private Map<String, String> detectColumnTypes(List<Map<String, Object>> rows) {
        Map<String, String> types = new LinkedHashMap<>();

        if (rows.isEmpty())
            return types;

        for (String column : rows.get(0).keySet()) {
            Class<?> type = null;

            for (Map<String, Object> row : rows) {
                Object value = row.get(column);
                if (value != null) {
                    type = value.getClass();
                    break;
                }
            }

            if (type == null) {
                types.put(column, "TEXT"); // Fallback
            } else if (Number.class.isAssignableFrom(type)) {
                types.put(column, "REAL");
            } else if (type == Boolean.class) {
                types.put(column, "BOOLEAN");
            } else if (type == java.sql.Date.class || type == java.time.LocalDate.class) {
                types.put(column, "DATE");
            } else {
                types.put(column, "TEXT");
            }
        }

        return types;
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
       
    
    
    public void createTableFromExcel(String dbName, String tableName, List<Map<String, Object>> rows) throws SQLException {
        if (rows == null || rows.isEmpty())
            return;

        Map<String, String> columnTypes = detectColumnTypes(rows);

        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sb.append(tableName).append(" (");

        for (Map.Entry<String, String> entry : columnTypes.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\" ").append(entry.getValue()).append(", ");
        }

        sb.setLength(sb.length() - 2);
        sb.append(");");

        try (Connection conn = ConnectionManager.getConnection(dbName);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sb.toString());
        }
    }
}
