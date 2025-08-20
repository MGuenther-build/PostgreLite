package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;



public class Database {
    public boolean createDatabase(String dbName) {
        String checkSql = "SELECT 1 FROM pg_database WHERE datname = ?";
        
        try (Connection conn = ConnectionManager.getConnection(null);
        		PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
        	checkStmt.setString(1, dbName);
            
            try (ResultSet result = checkStmt.executeQuery()) {
                if (result.next()) {
                    System.out.println("Datenbank existiert bereits.");
                    return false;
                }
            }
            try (Statement createStmt = conn.createStatement()) {
                createStmt.executeUpdate("CREATE DATABASE \"" + dbName + "\"");
                System.out.println("Datenbank " + dbName + " wurde erstellt.");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
    
    // für Anbindung QuerTool an die DB-Verbindung
    public Connection connect(String dbName) throws SQLException {
        return ConnectionManager.getConnection(dbName);
    }

}
