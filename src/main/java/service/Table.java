package service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import util.SQLSecurity;
import model.TableColumnDefinition;



public class Table {
	public static final Set<String> TYPES_WITH_PARAMS = Set.of(
		    "CHAR", "VARCHAR", "NUMERIC", "DECIMAL", "BIT", "BIT VARYING",
		    "TIME", "TIMESTAMP", "INTERVAL"
	);
	
	public static boolean needsParameter(String type) {
		return type != null && TYPES_WITH_PARAMS.contains(type.toUpperCase());
	}
		
		
	public static final List<String> POSTGRES_TYPES = List.of(
	        "BIGINT", "BIGSERIAL", "BOOLEAN", "BYTEA", "CHAR", "CIDR", "DATE", "DECIMAL",
	        "DOUBLE PRECISION", "FLOAT", "INET", "INTEGER", "INTERVAL", "JSON", "JSONB",
	        "MACADDR", "MACADDR8", "MONEY", "NUMERIC", "POINT", "POLYGON", "REAL", "SERIAL",
	        "SMALLINT", "TEXT", "TIME", "TIME WITH TIME ZONE", "TIMESTAMP", "TIMESTAMP TIME ZONE",
	        "TSQUERY", "TSVECTOR", "UUID", "VARCHAR", "XML"
	);
	
	private boolean isValidDataType(String type) {
		return type != null && POSTGRES_TYPES.stream()
			    .anyMatch(t -> t.equalsIgnoreCase(type));
	}
	
	
	public static String buildFullType(String type, String parameter) {
	    if (type == null) return "";
	    if (parameter != null && !parameter.isEmpty() && needsParameter(type)) {
	        return type + "(" + parameter + ")";
	    }
	    return type;
	}

	    
	public String createTable(String dbName, String tableName, List<TableColumnDefinition> columns) {
	    if (columns == null || columns.isEmpty()) {
	        return "Keine Spalten definiert.";
	    }

	    if (!isValidIdentifier(tableName)) {
	        return "Ungültiger Tabellenname: " + tableName;
	    }

	    Set<String> seen = new HashSet<>();
	    for (TableColumnDefinition col : columns) {
	        if (!isValidIdentifier(col.getColumnName()) || !isValidDataType(col.getBaseType())) {
	            return "Ungültige Spaltenangabe: " + col.getColumnName() + " (" + col.getBaseType() + ")";
	        }
	        if (!seen.add(col.getColumnName().toLowerCase())) {
	            return "Doppelte Spalte: " + col.getColumnName();
	        }
	    }

	    StringBuilder sql = new StringBuilder("CREATE TABLE \"" + tableName + "\" (");

	    for (int i = 0; i < columns.size(); i++) {
	        TableColumnDefinition col = columns.get(i);

	        sql.append("\"")
	           .append(col.getColumnName())
	           .append("\" ")
	           .append(col.toSqlDefinition());

	        if (i < columns.size() - 1) {
	            sql.append(", ");
	        }
	    }

	    sql.append(")");

	    try (Connection conn = ConnectionManager.getConnection(dbName);
	         Statement stmt = conn.createStatement()) {

	        stmt.executeUpdate(sql.toString());
	        return null;

	    } catch (SQLException e) {
	        return "Fehler beim Erstellen der Tabelle: " + e.getMessage();
	    }
	}
	
	
	public String ExpertSQL(String dbName, String sql) {
	    if (sql == null || sql.trim().isEmpty()) {
	        return "Kein SQL-Befehl eingegeben.";
	    }
	    
	    if (sql.trim().endsWith(";")) {
	        return "Kein Semikolon am Ende erlaubt!";
	    }

	    if (!SQLSecurity.isAllowedExpertMode(sql)) {
	        return "Nur CREATE-/ALTER-/ADD-Befehle erlaubt.";
	    }

	    try (Connection conn = ConnectionManager.getConnection(dbName);
	         Statement stmt = conn.createStatement()) {

	        stmt.execute(sql);
	        return null; // Erfolg

	    } catch (SQLException e) {
	        return "Fehler beim Ausführen: " + e.getMessage();
	    }
	}
	
	
	private boolean isValidIdentifier(String name) {
	    return name != null && name.matches("[a-zA-Z_][a-zA-Z0-9_]*");
	}
    
	
    public List<String> listDatabases() {
        List<String> databases = new ArrayList<>();
        String query = "SELECT datname FROM pg_database WHERE datistemplate = false";

        try (Connection conn = ConnectionManager.getConnection(null);
             Statement stmt = conn.createStatement();
             ResultSet result = stmt.executeQuery(query)) {

            while (result.next()) {
                databases.add(result.getString("datname"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return databases;
    }
 

    public List<String> listTables(String dbName) {
        List<String> tables = new ArrayList<>();
        String query = "SELECT tablename FROM pg_tables WHERE schemaname = 'public'";

        try (Connection conn = ConnectionManager.getConnection(dbName);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                tables.add(rs.getString("tablename"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tables;
    }
}
