package controller;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import javafx.animation.FadeTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import service.Database;
import service.Query;
import service.Excelimport;
import service.ExportData;
import util.PopupManager;
import util.QuerySecurity;
import util.SQLHighlighter;
import util.Session;
import util.ViewSwitcher;



public class QueryController {

    @FXML
    private ComboBox<String> databaseComboBox;
    @FXML
    private Button showTablesButton;
    @FXML
    private Button executeSQLButton;
    @FXML
    private VBox queryBox;
    @FXML
    private TableView<ObservableList<String>> resultTable;
    @FXML
    private Button importExcelButton;
    @FXML
    private Button exportButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label infoLabel;
    @FXML
    private TableView<String> sqlHistory;
    @FXML
    private TitledPane historyBoard;

    
    
    private CodeArea queryCodeArea;
    
    private final Database databaseService = new Database();
    
    private final Query queryService = new Query();
    
    private final ObservableList<String> queryHistory = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        queryCodeArea = new CodeArea();
        queryCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(queryCodeArea));
        queryCodeArea.setPrefHeight(200);
        queryCodeArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14;");

        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(queryCodeArea);
        queryBox.getChildren().add(scrollPane);

        // Highlighting
        queryCodeArea.textProperty().addListener((obs, oldText, newText) -> {
            queryCodeArea.setStyleSpans(0, SQLHighlighter.computeHighlighting(newText));
        });

        updateDatabaseList();
        
        TableColumn<String, String> queryColumn = new TableColumn<>("SQL-Abfrage");
        queryColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        queryColumn.setPrefWidth(400);

        resultTable.setPlaceholder(new Label ("noch keine Daten vorhanden"));
        sqlHistory.setPlaceholder(new Label ("noch keine Abfragen in dieser Session"));
        sqlHistory.getColumns().add(queryColumn);
        addDeleteColumnSQLHistory();
        sqlHistory.setItems(queryHistory);

        // Doppelklick zum Wiederverwenden
        sqlHistory.setRowFactory(tv -> {
            TableRow<String> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    queryCodeArea.replaceText(row.getItem());
                }
            });
            return row;
        });
    }

    private void updateDatabaseList() {
        ObservableList<String> databases = FXCollections.observableArrayList();
        databases.add("-- Datenbank wählen --");
        databases.addAll(databaseService.listDatabases());
        databaseComboBox.setItems(databases);
        databaseComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    public void onDatabaseSelected() {
        String selectedDB = databaseComboBox.getValue();
        if (selectedDB == null || selectedDB.equals("-- Datenbank wählen --")) {
            Session.selectedDatabase = null;
            return;
        }
        Session.selectedDatabase = selectedDB;
    }

    @FXML
    public void executeSQL() {
    	resetInfos();
        String sql = queryCodeArea.getText().trim();
        String database = Session.selectedDatabase;

        String safetyMessage = QuerySecurity.checkQuerySafety(sql, database);
        if (safetyMessage != null) {
            showError("Sicherheitsfehler", safetyMessage);
            return;
        }

        try {
            if (sql.toLowerCase().startsWith("select")) {
                List<Map<String, Object>> results =
                        queryService.executeQuery(database, sql);
                buildTable(results);

                if (results.isEmpty()) {
                    showInfo("Info", "Keine Ergebnisse gefunden.");
                }
                historyBoard.setExpanded(false);
                
            } else {
                int updateCount = queryService.executeUpdate(database, sql);
                showInfo("Erfolg", updateCount + " Zeilen betroffen.");
                resultTable.getColumns().clear();
                resultTable.getItems().clear();
                historyBoard.setExpanded(false);
            }
            
        } catch (SQLException e) {
            showError("SQL-Fehler", e.getMessage());
        }
        
        if (!sql.isBlank() && !queryHistory.contains(sql)) {
            queryHistory.add(sql);
            historyBoard.setExpanded(true);
        }
    }

    private void buildTable(List<Map<String, Object>> rows) {
    	resetInfos();
        resultTable.getColumns().clear();
        resultTable.getItems().clear();

        if (rows == null || rows.isEmpty())
        	return;

        // Spalten dynamisch aus Keys der ersten Zeile erzeugen
        Map<String, Object> firstRow = rows.get(0);
        List<String> columnNames = new ArrayList<>(firstRow.keySet());

        for (int colIndex = 0; colIndex < columnNames.size(); colIndex++) {
            final int index = colIndex;
            TableColumn<ObservableList<String>, String> col =
                    new TableColumn<>(columnNames.get(colIndex));
            col.setCellValueFactory(param ->
                    new ReadOnlyObjectWrapper<>(param.getValue().get(index)));
            resultTable.getColumns().add(col);
        }

        // Daten füllen
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (Map<String, Object> row : rows) {
            ObservableList<String> observableRow = FXCollections.observableArrayList();
            for (String colName : columnNames) {
                Object val = row.get(colName);
                observableRow.add(val == null ? "" : val.toString());
            }
            data.add(observableRow);
        }
        resultTable.setItems(data);
        autoResizeColumns();
    }
    
    
    
    private void addDeleteColumnSQLHistory() {
        boolean exists = sqlHistory.getColumns().stream()
            .anyMatch(col -> "Löschen".equals(col.getText()));
        if (exists) return;

        TableColumn<String, Void> deleteColumn = new TableColumn<>("Löschen");

        Callback<TableColumn<String, Void>, TableCell<String, Void>> cellFactory = param -> new TableCell<String, Void>() {
            private final Button deleteButton = new Button("X");

            {
                deleteButton.setOnAction(event -> {
                    String item = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(item);
                });
                deleteButton.getStyleClass().add("delete-button");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new StackPane(deleteButton));
            }
        };
        deleteColumn.setCellFactory(cellFactory);
        deleteColumn.setPrefWidth(55);
        deleteColumn.setStyle("-fx-alignment: CENTER;");
        sqlHistory.getColumns().add(deleteColumn);
    }
    
    
    
    @FXML
    public void importExcel() {
        resetInfos();

        // Datei auswählen
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Excel-Datei auswählen");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel-Dateien", "*.xlsx"));
        File file = chooser.showOpenDialog(queryBox.getScene().getWindow());

        if (file == null)
            return;

        // Eingabe Tabellenname und Startzelle
        Map<String, String> input = PopupManager.openExcelImportPopup(queryBox.getScene().getWindow());
        if (input == null || input.get("tableName").isBlank() || input.get("startCell").isBlank()) {
            showWarning("Ungültige Eingabe", "Tabellenname und Startzelle dürfen nicht leer sein.");
            return;
        }

        String tableName = input.get("tableName").trim();
        String startCell = input.get("startCell").trim();

        // Zellformat
        if (!startCell.matches("^[A-Z]+[0-9]+$")) {
            showWarning("Ungültiges Format", "Bitte gib eine gültige Zellreferenz ein (z. B. A1, B2, AA10).");
            return;
        }

        // Daten einlesen
        Excelimport importer = new Excelimport();
        List<Map<String, String>> excelData;
        try {
            excelData = importer.readExcel(file, startCell);
        } catch (RuntimeException ex) {
            showError("Fehlerhafte Datei", ex.getMessage());
            return;
        } catch (IOException ex) {
            showError("Dateifehler", "Die Datei konnte nicht gelesen werden.");
            return;
        }

        if (excelData.isEmpty()) {
            showWarning("Leere Datei", "Keine Daten gefunden.");
            return;
        }

        // Datenbank prüfen
        String database = Session.selectedDatabase;
        if (database == null) {
            showWarning("Keine Datenbank ausgewählt", "Bitte Datenbank auswählen.");
            return;
        }

        // Import starten
        progressBar.setVisible(true);
        Task<Void> importTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    queryService.createTableFromExcel(database, tableName, excelData);

                    int totalRows = excelData.size();
                    int processed = 0;

                    for (int i = 0; i < totalRows; i += 100) {
                        int end = Math.min(i + 100, totalRows);
                        List<Map<String, String>> batch = excelData.subList(i, end);
                        queryService.insertData(database, tableName, batch);
                        processed += batch.size();
                        updateProgress(processed, totalRows);
                    }

                } catch (Exception e) {
                    throw e;
                }
                return null;
            }
        };

        importTask.setOnSucceeded(e -> {
            progressBar.setVisible(false);
            showInfo("Import erfolgreich", "Tabelle '" + tableName + "' wurde erstellt.");
            buildTableExcel(excelData);
        });

        importTask.setOnFailed(e -> {
            progressBar.setVisible(false);
            try {
                queryService.dropTable(database, tableName);
            } catch (SQLException ignored) {}
            showError("Fehler beim Import", importTask.getException().getMessage() + "\nTabelle wurde entfernt.");
        });

        progressBar.progressProperty().bind(importTask.progressProperty());
        new Thread(importTask).start();
    }


    
    private void buildTableExcel(List<Map<String, String>> rows) {
        resultTable.getColumns().clear();
        resultTable.getItems().clear();

        Map<String, String> firstRow = rows.get(0);
        List<String> columnNames = new ArrayList<>(firstRow.keySet());

        for (int colIndex = 0; colIndex < columnNames.size(); colIndex++) {
            final int index = colIndex;
            TableColumn<ObservableList<String>, String> col =
                    new TableColumn<>(columnNames.get(colIndex));
            col.setCellValueFactory(param ->
                    new ReadOnlyObjectWrapper<>(param.getValue().get(index)));
            resultTable.getColumns().add(col);
        }

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (Map<String, String> row : rows) {
            ObservableList<String> observableRow = FXCollections.observableArrayList();
            for (String colName : columnNames) {
                observableRow.add(row.getOrDefault(colName, ""));
            }
            data.add(observableRow);
        }

        resultTable.setItems(data);
        autoResizeColumns();
    }
    
    
    
    @FXML
    public void exportExcelOrCSV() {
        if (resultTable.getItems().isEmpty()) {
            showWarning("Kein Inhalt", "Es gibt keine Daten zu exportieren!");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportieren als...");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Excel-Datei (*.xlsx)", "*.xlsx"),
            new FileChooser.ExtensionFilter("CSV-Datei (*.csv)", "*.csv")
        );
        File file = chooser.showSaveDialog(queryBox.getScene().getWindow());
        
        if (file == null)
        	return;
        
        if (resultTable.getItems().isEmpty()) {
            showWarning("Kein Inhalt", "Es gibt keine Daten zu exportieren!");
            return;
        }

        try {
            List<String> headers = resultTable.getColumns().stream()
                .map(col -> col.getText())
                .toList();

            ObservableList<ObservableList<String>> data = resultTable.getItems();
            ExportData exporter = new ExportData();

            if (file.getName().endsWith(".xlsx")) {
                exporter.exportToExcel(file, headers, data);
            } else if (file.getName().endsWith(".csv")) {
                exporter.exportToCSV(file, headers, data);
            } else {
                showError("Fehler", "Unbekanntes Dateiformat.");
                return;
            }

            showInfo("Export abgeschlossen", "Datei erstellt in:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            showError("Export fehlgeschlagen", e.getMessage());
        }
    }

    
    
    @FXML
    public void showTables() {
        PopupManager.openTableListPopup();
    }

    
    @FXML
    private void toggleHistory() {
        historyBoard.setExpanded(!historyBoard.isExpanded());
    }
    
    
    private void autoResizeColumns() {
        for (TableColumn<?, ?> column : resultTable.getColumns()) {
            column.setPrefWidth(Math.max(100, column.getWidth()));
        }
    }

    
    @FXML
    public void backToMain() {
        ViewSwitcher.switchTo("/gui_views/start.fxml");
    }
    
    
    private void showError(String title, String message) {
    	infoLabel.setOpacity(1);
        infoLabel.setText("" + message);
        infoLabel.setStyle("-fx-text-fill: red;");
    }
    private void showInfo(String title, String message) {
    	infoLabel.setOpacity(1); 
        infoLabel.setText("" + message);
        infoLabel.setStyle("-fx-text-fill: green;");
    }
    private void showWarning(String title, String message) {
    	infoLabel.setOpacity(1);
        infoLabel.setText("" + title + ": " + message);
        infoLabel.setStyle("-fx-text-fill: orange;");
    }
    private void resetInfos() {
        clearInfos();
    }
    private void clearInfos() {
        FadeTransition fade = new FadeTransition(Duration.seconds(2), infoLabel);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            infoLabel.setText("");
            infoLabel.setOpacity(1);
        });
        fade.play();
    }
}
