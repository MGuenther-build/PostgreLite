package controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import service.Table;
import util.Session;



public class TableViewController {

    @FXML
    private ListView<String> tableListView;

    private final Table tableService = new Table();

    @FXML
    public void initialize() {
        if (Session.selectedDatabase != null) {
            tableListView.getItems().setAll(tableService.listTables(Session.selectedDatabase));
        }
    }
}
