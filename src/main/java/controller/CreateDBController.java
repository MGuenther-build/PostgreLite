package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import service.Database;
import util.Session;
import util.ViewSwitcher;



public class CreateDBController {

    @FXML
    private TextField dbNameField;
    @FXML
    private Button nextButton;
    @FXML
    public void initialize() {
    	nextButton.setVisible(true);
    	nextButton.setDisable(true);
    }

    private final Database databaseService = new Database();

    @FXML
    public void createDatabase() {
        String dbName = dbNameField.getText().trim();

        if (dbName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Eingabefehler", "Bitte geben Sie einen Datenbanknamen ein.");
            return;
        }

        boolean success = databaseService.createDatabase(dbName);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Erfolg", "Datenbank erfolgreich erstellt.");
            dbNameField.clear();
            Session.selectedDatabase = dbName;
            nextButton.setVisible(true);
            nextButton.setDisable(false);
        } else {
            showAlert(Alert.AlertType.ERROR, "Fehler", "Datenbank existiert bereits oder konnte nicht erstellt werden.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void nextButtonForward(ActionEvent event) {
    	ViewSwitcher.switchTo("/gui_views/createTable.fxml");
    }

    @FXML
    public void backToMain() {
        ViewSwitcher.switchTo("/gui_views/start.fxml");
    }
}
