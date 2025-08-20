package controller;

import javafx.fxml.FXML;
import util.ViewSwitcher;
import util.PopupManager;



public class MainController {

    @FXML
    public void backToMain() {
        ViewSwitcher.switchTo("/gui_views/start.fxml");
    }

    @FXML
    public void createDatabase() {
        ViewSwitcher.switchTo("/gui_views/createDB.fxml");
    }

    @FXML
    public void createTable() {
        ViewSwitcher.switchTo("/gui_views/createTable.fxml");
    }

    @FXML
    public void editDatabase() {
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
}
