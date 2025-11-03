package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import service.ConnectionManager;
import util.ViewSwitcher;
import util.PopupManager;



public class MainController {

    @FXML
    public void backToMain() {
        ViewSwitcher.switchTo("/gui_views/start.fxml");
    }

    @FXML
    public void createDatabase() {
        if (!ConnectionManager.isConfigured()) {
            showBlockedAlert("Datenbank erstellen");
            return;
        }
        ViewSwitcher.switchTo("/gui_views/createDB.fxml");
    }

    @FXML
    public void createTable() {
        if (!ConnectionManager.isConfigured()) {
            showBlockedAlert("Tabelle erstellen");
            return;
        }
        ViewSwitcher.switchTo("/gui_views/createTable.fxml");
    }

    @FXML
    public void editDatabase() {
        if (!ConnectionManager.isConfigured()) {
            showBlockedAlert("QueryTool");
            return;
        }
        ViewSwitcher.switchTo("/gui_views/queryTool.fxml");
    }
    
    @FXML
    public void ChangeConfig() {
        PopupManager.openConfigPopup();
    }
    
    @FXML
    public void ShowConnectionStatus() {
        PopupManager.showConnectionStatus();
    }
    
    @FXML
    public void ShowAbout() {
        PopupManager.openAboutPopup();
    }
    
    @FXML
    public void showTableListPopup() {
        PopupManager.openTableListPopup();
    }
    
    @FXML
    public void AdminZone() {
        PopupManager.openAdminZone();
    }
    
    private void showBlockedAlert(String funktion) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Kein Zugriff");
        alert.setHeaderText(null);
        alert.setContentText("\"" + funktion + "\" ist ohne Datenbankkonfiguration nicht verfügbar.");
        alert.showAndWait();
    }
}
