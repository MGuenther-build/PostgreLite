package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import service.ConnectionManager;
import util.Session;
import util.ViewSwitcher;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;



public class AdminController {

    @FXML
    private TextArea commandInput;
    @FXML
    private ListView<TextFlow> consoleOutput;
    @FXML
    private Button clearConsoleButton;
    
    private final ObservableList<String> history = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        consoleOutput.setItems(FXCollections.observableArrayList());
        commandInput.setPromptText("SQL-Befehl eingeben und Enter drücken...");

        commandInput.setStyle("-fx-control-inner-background: #252526; -fx-text-fill: #d4d4d4; -fx-font-family: Consolas; -fx-font-size: 14;");
        consoleOutput.setStyle("-fx-control-inner-background: #1e1e1e; -fx-font-family: Consolas; -fx-font-size: 13;");

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

        appendToConsole("> Willkommen im Adminbereich\n", Color.LIGHTBLUE);
    }

    
    
    @FXML
    public void executeCommand() {
        String sql = commandInput.getText().trim();
        if (sql.isEmpty())
        	return;

        appendToConsole("> " + sql, Color.LIGHTBLUE);
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
                strbuild.append("OK: Abfrage erfolgreich (ResultSet vorhanden)").append(System.lineSeparator());
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
                appendToConsole("OK: " + updateCount + " Zeilen betroffen.", Color.LIGHTGREEN);
            }

        } catch (SQLException e) {
            appendToConsole("Fehler: " + e.getMessage(), Color.RED);
        }

        commandInput.clear();
        consoleOutput.scrollTo(consoleOutput.getItems().size() - 1);
    }


    
    @FXML
    public void clearConsole() {
        consoleOutput.getItems().clear();
        appendToConsole("> Konsole geleert\n", Color.LIGHTBLUE);
    }

    
    
    @FXML
    public void showHistory() {
        if (history.isEmpty()) {
            appendToConsole("Keine Historie vorhanden.", Color.LIGHTGRAY);
            return;
        }

        StringBuilder strbuild2 = new StringBuilder();
        strbuild2.append("=== SQL-Historie ===").append(System.lineSeparator());
        for (String cmd : history) {
            strbuild2.append(cmd).append(System.lineSeparator());
        }
        appendToConsole(strbuild2.toString(), Color.LIGHTBLUE);
        consoleOutput.scrollTo(consoleOutput.getItems().size() - 1);
    }
    
    
    
    private void appendToConsole(String message, Color color) {
        Text text = new Text(message);
        text.setFill(color);
        consoleOutput.getItems().add(new TextFlow(text));
    }
    

    
    @FXML
    public void backToMain() {
        ViewSwitcher.switchTo("/gui_views/start.fxml");
    }
}
