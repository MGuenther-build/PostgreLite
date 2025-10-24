package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.ConnectionManager;



public class DBConfigController {

    @FXML
    private TextField hostField;
    @FXML
    private TextField portField;
    @FXML
    private TextField userField;
    @FXML
    private PasswordField passwordField;

    private boolean success = false;
    
    private boolean configSkipped = false;

    public boolean isConfigSuccessful() {
        return success;
    }
    
    public boolean isConfigSkipped() {
    	return configSkipped;
    }
    
    public void setConfigSkipped(boolean skipped) {
    	this.configSkipped = skipped;
    }

    @FXML
    public void initialize() {
        javafx.application.Platform.runLater(() -> {
            hostField.getParent().requestFocus();
        });
    }
    
    @FXML
    public void FirstConnect() {
        String host = hostField.getText().trim();
        String port = portField.getText().trim();
        String user = userField.getText().trim();
        String password = passwordField.getText().trim();

        if (host.isBlank() || port.isBlank() || user.isBlank() || password.isBlank()) {
            showError("Bitte alle Felder ausfüllen");
            return;
        }

        try {
            // Testverbindung mit temporären Werten
        	ConnectionManager.testConnection(host, port, user, password, null);

            // Nur bei Erfolg Configwerte übernehmen
        	ConnectionManager.setCredentials(host, port, user, password);
            success = true;
            showInfo("Verbindung zur PostgreSQL-Datenbank erfolgreich hergestellt.");
            ((Stage) hostField.getScene().getWindow()).close();

        } catch (Exception e) {
            String rawMessage = e.getMessage().toLowerCase();

            String userMessage;
            if (rawMessage.contains("authentication failed")) {
                userMessage = "Authentifizierung fehlgeschlagen. Bitte Benutzername und Passwort prüfen.";
            } else if (rawMessage.contains("connection refused") || rawMessage.contains("timeout")) {
                userMessage = "Keine Verbindung möglich. Host oder Port prüfen.";
            } else if (rawMessage.contains("unknownhost")) {
                userMessage = "Der angegebene Hostname ist ungültig.";
            } else {
                userMessage = "Verbindung fehlgeschlagen. Bitte Eingaben prüfen.";
            }
            showError(userMessage);
        }
    }
    
    @FXML
    public void AbortConnect() {
        success = false;
        ((Stage) hostField.getScene().getWindow()).close();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Verbindung erfolgreich");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
