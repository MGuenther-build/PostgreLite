package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



public class ConnectionManager {

    private static String host;
    private static String port;
    private static String user;
    private static String password;
    
    public static boolean isConfigured() {
        return host != null && !host.isBlank()
            && port != null && !port.isBlank()
            && user != null && !user.isBlank()
            && password != null && !password.isBlank();
    }
    
    public static void testConnection(String host, String port, String user, String password, String dbName) throws SQLException {
        String url = dbName == null || dbName.isBlank()
            ? String.format("jdbc:postgresql://%s:%s/", host, port)
            : String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Verbindung erfolgreich (kein catch nötig)
        }
    }

    public static void setCredentials(String h, String p, String u, String pw) {
        host = h;
        port = p;
        user = u;
        password = pw;
    }

    public static Connection getConnection(String dbName) throws SQLException {
        if (host == null || port == null || user == null || password == null) {
            throw new IllegalStateException("Zugangsdaten nicht vorhanden!");
        }
        String url = dbName == null || dbName.isBlank()
            ? String.format("jdbc:postgresql://%s:%s/", host, port) : String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);
        return DriverManager.getConnection(url, user, password);
    }
    
    public static String getHost() {
        return host;
    }

    public static String getPort() {
        return port;
    }

    public static String getUser() {
        return user;
    }
}
