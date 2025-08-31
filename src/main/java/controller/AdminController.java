package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import service.ConnectionManager;
import util.Session;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;



public class AdminController {

    @FXML
    private TextArea commandInput;
    @FXML
    private ListView<TextFlow> consoleOutput;
    @FXML
    private Button clearConsoleButton;
    @FXML
    private Button historyButton;
    
    private final ObservableList<String> history = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        consoleOutput.setItems(FXCollections.observableArrayList());
        commandInput.setPromptText("Befehl eingeben und Enter drücken...");
        
        // Textfeldmarkierung deaktiviert
        consoleOutput.setOnMouseClicked(e -> consoleOutput.getSelectionModel().clearSelection());

        commandInput.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (event.isShiftDown()) {
                    commandInput.appendText(System.lineSeparator());
                } else {
                    executeCommand();
                }
                event.consume();
            }
        });
        
        commandInput.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                commandInput.setPromptText("");
            } else if (commandInput.getText().isEmpty()) {
                commandInput.setPromptText("Befehl eingeben und Enter drücken...");
            }
        });
        
        // aktueller Pfad in Konsole
        Platform.runLater(() -> {
            try (Connection conn = ConnectionManager.getConnection(Session.selectedDatabase);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT current_database();")) {
                if (rs.next()) {
                    appendToConsole("Verbunden mit DB: " + rs.getString(1), Color.LIGHTBLUE);
                }
            } catch (SQLException e) {
                appendToConsole("Fehler beim Ermitteln der aktuellen DB: " + e.getMessage(), Color.RED);
            }
        });
        
        // Schließen verhindern, bevor nicht nachgefragt
        Platform.runLater(() -> {
            Stage stage = (Stage) commandInput.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                event.consume();
                confirmClose(stage);
            });
        });
    }

    
    
    @FXML
    public void executeCommand() {
        String sql = commandInput.getText().trim();
        String sqlUpper = sql.toUpperCase();

        if (sql.isEmpty())
        	return;

        history.add(sql);

        try (Connection conn = ConnectionManager.getConnection(Session.selectedDatabase);
             Statement stmt = conn.createStatement()) {

            boolean myResultSet = stmt.execute(sql);

            if (myResultSet) {
                ResultSet result = stmt.getResultSet();
                ResultSetMetaData meta = result.getMetaData();
                int columnCount = meta.getColumnCount();
                
                // Header+Zeilen in Arrays sammeln
                String[] headers = new String[columnCount];
                int[] colWidths = new int[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    headers[i] = meta.getColumnLabel(i + 1);
                    colWidths[i] = headers[i].length();
                }

                ObservableList<String[]> rows = FXCollections.observableArrayList();
                while (result.next()) {
                    String[] row = new String[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        row[i] = result.getString(i + 1);
                        if (row[i] == null) row[i] = "NULL";
                        colWidths[i] = Math.max(colWidths[i], row[i].length());
                    }
                    rows.add(row);
                }
                
                // Headerausgabe
                StringBuilder strbuild = new StringBuilder();
                strbuild.append("Abfrage erfolgreich").append(System.lineSeparator());
                for (int i = 0; i < columnCount; i++) {
                	strbuild.append(String.format("%-" + colWidths[i] + "s  ", headers[i]));
                }
                strbuild.append(System.lineSeparator());
                
                // Trennung Header/Zeilen
                for (int i = 0; i < columnCount; i++) {
                    strbuild.append("-".repeat(colWidths[i])).append("  ");
                }
                strbuild.append(System.lineSeparator());

                // Zeilenausgabe
                for (String[] row : rows) {
                    for (int i = 0; i < columnCount; i++) {
                    	strbuild.append(String.format("%-" + colWidths[i] + "s  ", row[i]));
                    }
                    strbuild.append(System.lineSeparator());
                }

                appendToConsole(strbuild.toString(), Color.LIGHTGRAY);

            } else {
            	int updateCount = stmt.getUpdateCount();
            	if (updateCount == -1) {
            	    if (sqlUpper.startsWith("DROP") || sqlUpper.startsWith("CREATE") || 
            	        sqlUpper.startsWith("ALTER") || sqlUpper.startsWith("TRUNCATE")) {
            	        appendToConsole("DDL-Befehl erfolgreich ausgeführt.", Color.LIGHTGREEN);
            	    } else {
            	        appendToConsole("Befehl ausgeführt, keine Zeilen betroffen.", Color.LIGHTGREEN);
            	    }
            	} else {
            	    if (sqlUpper.startsWith("DELETE") && updateCount == 0) {
            	        appendToConsole("DELETE ausgeführt, aber keine Zeilen betroffen.", Color.ORANGE);
            	    } else {
            	        appendToConsole("OK: " + updateCount + " Zeilen betroffen.", Color.LIGHTGREEN);
            	    }
            	}
            }

        } catch (SQLException e) {
            appendToConsole("Fehler: " + e.getMessage(), Color.RED);
        }

        commandInput.clear();
        consoleOutput.scrollTo(consoleOutput.getItems().size() - 1);
    }

    
    
    private boolean historyVisible = false;
    @FXML
    public void switcherHistory() {
        if (historyVisible) {
            // History ausblenden
            consoleOutput.getItems().removeIf(item -> {
                if (item.getChildren().isEmpty()) return false;
                Text t = (Text) item.getChildren().get(0);
                return t.getText().startsWith("===") || history.contains(t.getText().trim());
            });
            historyVisible = false;
            historyButton.setText("Show History");
        } else {
            if (history.isEmpty()) {
                TextFlow tf = appendToConsole("Keine Historie vorhanden.", Color.LIGHTGRAY);

                // Timer: nach 5 Sekunden wieder entfernen
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ignored) {}
                    Platform.runLater(() -> consoleOutput.getItems().remove(tf));
                }).start();

                return;
            }

            StringBuilder strbuild2 = new StringBuilder();
            strbuild2.append("=== SQL-Historie ===").append(System.lineSeparator());
            for (String cmd : history) {
                strbuild2.append(cmd).append(System.lineSeparator());
            }
            appendToConsole(strbuild2.toString(), Color.LIGHTBLUE);
            consoleOutput.scrollTo(consoleOutput.getItems().size() - 1);

            historyVisible = true;
            historyButton.setText("Hide History");
        }
    }
    
    
    
    private TextFlow appendToConsole(String message, Color color) {
        if (consoleCleared) {
            consoleOutput.getItems().clear();
            consoleCleared = false;
        }
        Text text = new Text(message);
        text.setFill(color);
        TextFlow textflow = new TextFlow(text);
        consoleOutput.getItems().add(textflow);
        return textflow;
    }
    
    
    
    private boolean consoleCleared = false;
    @FXML
    public void clearConsole() {
        consoleOutput.getItems().clear();
        appendToConsole("> Konsole geleert\n", Color.LIGHTBLUE);
        consoleCleared = true;
    }
    
    
    
    private void confirmClose(Stage stage) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Adminbereich schließen");
        confirm.setHeaderText("Wirklich schließen?");

        ButtonType yes = new ButtonType("Ja, schließen");
        ButtonType no = new ButtonType("Nein", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(yes, no);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == yes) {
            stage.close();
        }
    }
    

    
    @FXML
    public void closeAdminZone() {
        Stage stage = (Stage) commandInput.getScene().getWindow();
        confirmClose(stage);
    }
}
