package controller;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import service.Database;
import service.Query;
import service.Excelimport;
import service.ExportData;
import service.QueryResult;
import util.PopupManager;
import util.QuerySecurity;
import util.SQLHighlighter;
import util.Session;
import util.ViewSwitcher;
import util.Formatter;
import util.SQLTypeMapper;



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
    private TableView<Map<String, Object>> resultTable;
    @FXML
    private Button importExcelButton;
    @FXML
    private Button exportButton;
    @FXML
    private TableView<String> sqlHistory;
    @FXML
    private TitledPane historyBoard;
    @FXML
    private StackPane toastPane;
    @FXML

    
    
    private CodeArea queryCodeArea;
    
    private final Database databaseService = new Database();
    
    private final Query queryService = new Query();
    
    private final ObservableList<String> queryHistory = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        queryCodeArea = new CodeArea();
        queryCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(queryCodeArea));
        queryCodeArea.setPrefHeight(300);
        queryCodeArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 1.5em; -fx-border-color: white; -fx-border-width: 3;");

        // Highlighting
        queryCodeArea.textProperty().addListener((obs, oldText, newText) -> {
            queryCodeArea.setStyleSpans(0, SQLHighlighter.computeHighlighting(newText));
        });
        
        // Abstand Zeilenanzeige zum Text
        queryCodeArea.setParagraphGraphicFactory(line -> {
            Label lineNo = new Label(String.format("%4d", line + 1));
            lineNo.setStyle("-fx-text-fill: gray; -fx-font-size: 12; -fx-font-weight: bold;");
            StackPane spacer = new StackPane(lineNo);
            spacer.setPadding(new Insets(0, 15, 0, 0));
            return spacer;
        });
        
        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(queryCodeArea);
        queryBox.getChildren().add(scrollPane);

        updateDatabaseList();
        
        TableColumn<String, String> queryColumn = new TableColumn<>("SQL-Abfrage");
        queryColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        queryColumn.setPrefWidth(400);

        resultTable.setPlaceholder(new Label ("noch keine Daten vorhanden"));
        resultTable.setStyle("-fx-font-size: 1.5em;");
        sqlHistory.setPlaceholder(new Label ("noch keine Abfragen in dieser Session"));
        sqlHistory.getColumns().add(queryColumn);
        addDeleteColumnSQLHistory();
        sqlHistory.setItems(queryHistory);
        sqlHistory.setStyle("-fx-font-size: 1.25em;");

        // Doppelklick zum Wiederverwenden
        sqlHistory.setRowFactory(tv -> {
            TableRow<String> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    String sql = row.getItem();
                    queryCodeArea.replaceText(sql);
                    queryCodeArea.moveTo(queryCodeArea.getLength());
                    queryCodeArea.requestFocus();
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
        String sql = queryCodeArea.getText().trim();
        String database = Session.selectedDatabase;

        String safetyMessage = QuerySecurity.checkQuerySafety(sql, database);
        if (safetyMessage != null) {
            showError("Sicherheitsfehler", safetyMessage);
            return;
        }

        try {
        	if (sql.toLowerCase().startsWith("select")) {
        	    QueryResult qr = queryService.executeQuery(database, sql);
        	    List<Map<String, Object>> results = qr.getRows();
        	    List<String> columnNames = qr.getColumnNames();
        	    List<String> sqlTypes = qr.getSqlTypes();

        	    buildTable(results, columnNames, sqlTypes);

        	    if (results.isEmpty()) {
        	        showWarning("Info", "Keine Spalteneinträge gefunden.");
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
        }
    }

    
    
    private void buildTable(List<Map<String, Object>> rows, List<String> columnNames, List<String> sqlTypes) {
        resultTable.getColumns().clear();
        resultTable.getItems().clear();

        for (int i = 0; i < columnNames.size(); i++) {
            final String colName = columnNames.get(i);
            final String typeName = SQLTypeMapper.friendlySqlType(sqlTypes.get(i));
            String headerType = typeName; // z.B. VARCHAR(100) oder TEXT

            TableColumn<Map<String, Object>, Object> col = new TableColumn<>();
            Label nameLabel = new Label(colName);
            nameLabel.setStyle("-fx-text-fill: black;");

            Label typeLabel = new Label(headerType);
            typeLabel.setStyle("-fx-text-fill: gray;");

            VBox header = new VBox(nameLabel, typeLabel);
            header.setAlignment(Pos.CENTER);
            col.setGraphic(header);
            
            col.setId(colName);
            col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(colName)));
            col.setCellFactory(column -> new TableCell<Map<String, Object>, Object>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                        return;
                    }
                    setText(Formatter.format(item, colName));

                    if (item instanceof Boolean) {
                        setStyle("-fx-alignment: CENTER;");
                    } else if (item instanceof Integer || item instanceof Long) {
                        setStyle("-fx-alignment: CENTER;");
                    } else if (item instanceof Double || item instanceof Float) {
                        setStyle("-fx-alignment: CENTER;");
                    } else if (item instanceof LocalDate || item instanceof LocalDateTime || item instanceof Date) {
                        setStyle("-fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-alignment: CENTER;");
                    }
                }
            });
            resultTable.getColumns().add(col);
        }
        resultTable.setItems(FXCollections.observableArrayList(rows));
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
        Map<String, String> input = PopupManager.openExcelImportPopup(queryBox.getScene().getWindow());
        if (input == null) {
        	return;
        }
        
        String tableName = input.get("tableName").trim();
        String startCell = input.get("startCell").trim();
        File selectedFile = new File(input.get("filePath"));
        
        if (input.get("tableName").isBlank() || input.get("startCell").isBlank()) {
            showWarning("Ungültige Eingabe", "Tabellenname und Startzelle dürfen nicht leer sein.");
            return;
        }

        // Zellformat
        if (!startCell.matches("^[A-Z]+[0-9]+$")) {
            showWarning("Ungültiges Format", "Bitte gültige Zellreferenz eingeben (z. B. A1, B2, AA10).");
            return;
        }

        // Daten einlesen
        final Excelimport importer = new Excelimport();
        final QueryResult result;
        try {
            result = importer.readExcel(selectedFile, startCell);
        } catch (IOException | RuntimeException ex) {
            showError("Fehler beim Einlesen der Datei", ex.getMessage());
            return;
        }

        if (result.getRows().isEmpty()) {
            showWarning("Leere Datei", "Keine Daten gefunden.");
            return;
        }

        // Datenbank prüfen
        String database = Session.selectedDatabase;
        if (database == null) {
            showWarning("Keine Datenbank ausgewählt", "Bitte Datenbank auswählen.");
            return;
        }
        
        boolean exists;
        try {
            exists = queryService.tableExists(database, tableName);
        } catch (SQLException e) {
            showError("Fehler bei Tabellenprüfung", e.getMessage());
            return;
        }

        if (exists) {
            showWarning("Tabelle existiert", "Die Tabelle '" + tableName + "' existiert bereits.\nBitte anderen Namen wählen.");
            return;
        }
        startExcelImport(database, tableName, result);
    }

    
    
    private void startExcelImport(String dbName, String tableName, QueryResult result) {

        Task<Void> importTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Tabelle mit den Typen aus QueryResult erstellen
                queryService.createTableFromExcel(dbName, tableName, result);

                int total = result.getRows().size();
                int processed = 0;

                // Daten in Batches einfügen
                for (int i = 0; i < total; i += 100) {
                    int end = Math.min(i + 100, total);
                    List<Map<String, Object>> batch = result.getRows().subList(i, end);
                    queryService.insertData(dbName, tableName, batch);
                    processed += batch.size();
                    updateProgress(processed, total);
                }
                return null;
            }
        };

        importTask.setOnSucceeded(e -> {
            showInfo("Import erfolgreich", "Tabelle '" + tableName + "' erstellt.");
            // TableView anzeigen mit zweizeiligem Header
            buildTable(result.getRows(), result.getColumnNames(), result.getSqlTypes());
        });

        importTask.setOnFailed(e -> {
            showError("Import fehlgeschlagen", importTask.getException().getMessage());
        });

        new Thread(importTask).start();
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

        if (file == null) {
            showWarning("Info", "Datei ist leer.");
            return;
        }
        // Holt die Daten-ID für Export aus dem createTable (columnKey, getId)
        List<String> headers = resultTable.getColumns().stream()
        		.map(col -> col.getId())
        		.toList();

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (Map<String, Object> row : resultTable.getItems()) {
            ObservableList<String> rowData = FXCollections.observableArrayList();
            for (String header : headers) {
                Object value = row.get(header);
                rowData.add(value != null ? value.toString() : "");
            }
            data.add(rowData);
        }

        try {
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
    
    

    private void showToast(String message, String color) {
        Label toast = new Label(message);
        toast.getStyleClass().add("toast");
        toast.setTextFill(Color.WHITE);
        toast.setStyle("-fx-background-color: " + color + ";");
        toastPane.getChildren().add(toast);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        // Meldung automatisch nach 5 Sek. ausfaden
        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        FadeTransition fadeOutAuto = new FadeTransition(Duration.millis(1000), toast);
        fadeOutAuto.setFromValue(1);
        fadeOutAuto.setToValue(0);
        fadeOutAuto.setOnFinished(e -> toastPane.getChildren().remove(toast));
        
        SequentialTransition seq = new SequentialTransition(fadeIn, pause, fadeOutAuto);
        seq.play();
        
        // Meldung durch Klick verschwinden lassen
        toast.setOnMouseClicked(e -> {
            if (toast.getUserData() != null && "closing".equals(toast.getUserData())) {
                return;
            }
            toast.setUserData("closing");
            seq.stop();

            FadeTransition clickFadeOut = new FadeTransition(Duration.millis(500), toast);
            clickFadeOut.setFromValue(toast.getOpacity());
            clickFadeOut.setToValue(0);
            clickFadeOut.setOnFinished(ev -> toastPane.getChildren().remove(toast));
            clickFadeOut.play();
        });
    }
    private void showError(String title, String message) {
        showToast(message, "#ba2d2d");
    }
    private void showInfo(String title, String message) {
        showToast(message, "#4ea312");
    }
    private void showWarning(String title, String message) {
        showToast(message, "#e06c0b");
    }
}
