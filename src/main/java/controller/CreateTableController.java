package controller;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import model.TableColumnDefinition;
import service.Table;
import service.Database;
import util.PopupManager;
import util.SQLHighlighter;
import util.Session;
import util.ViewSwitcher;



public class CreateTableController {
	
	@FXML
	private TextField tableNameField;
	@FXML
	private ComboBox<String> databaseComboBox;
	@FXML
	private TextField typeParameterField;
	@FXML
	private CheckBox notNullCheckBox;
	@FXML
	private CheckBox primaryKeyCheckBox;
	@FXML
	private CheckBox uniqueCheckBox;
	@FXML
	private TextField defaultValueField;
	@FXML
	private TextField checkConstraintField;
	@FXML
	private TextField OptionsField;
	@FXML
	private TableView<TableColumnDefinition> columnTableView;
	@FXML
	private TableColumn<TableColumnDefinition, String> columnNameColumn;
	@FXML
	private TableColumn<TableColumnDefinition, String> dataTypeColumn;
	@FXML
	private TableColumn<TableColumnDefinition, Boolean> notNullColumn;
	@FXML
	private TableColumn<TableColumnDefinition, Boolean> primaryKeyColumn;
	@FXML
	private TableColumn<TableColumnDefinition, Boolean> uniqueColumn;
	@FXML
	private TableColumn<TableColumnDefinition, String> defaultValueColumn;
	@FXML
	private TableColumn<TableColumnDefinition, String> checkConstraintColumn;
	@FXML
	private TableColumn<TableColumnDefinition, String> optionsColumn;
	@FXML
	private TextField columnNameField;
	@FXML
	private ComboBox<String> dataTypeComboBox;
	@FXML
	private Button showTables;
	@FXML
	private Button ExpertModeButton;
	@FXML
	private Button addColumnButton;
	@FXML
	private Button createTableButton;
	@FXML
	private Button executeExpertSQLButton;
	@FXML
	private HBox expertBox;

	
	
	private CodeArea expertCodeArea;

	private final ObservableList<TableColumnDefinition> columnData = FXCollections.observableArrayList();
	
	private final Database databaseService = new Database();
	
	private final Table tableService = new Table();

	private void updateDatabaseList() {
	    ObservableList<String> databases = FXCollections.observableArrayList();
	    databases.add("-- Datenbank wählen --");
	    databases.addAll(databaseService.listDatabases());
	    databaseComboBox.setItems(databases);
	    databaseComboBox.getSelectionModel().selectFirst(); // Dummy vorausgewählt
	}
    
    @FXML
    public void onDatabaseSelected() {
    	String selectedDB = databaseComboBox.getValue();
        if (selectedDB == null || selectedDB.equals("-- Datenbank wählen --")) {
            Session.selectedDatabase = null;
            return;
        }
        Session.selectedDatabase = selectedDB;

    	if (dataTypeComboBox.getItems().isEmpty()) {
            dataTypeComboBox.getItems().add("-- Datentyp --");
            dataTypeComboBox.getItems().addAll(Table.POSTGRES_TYPES);
        }
        String selectedDBfromComboBox = databaseComboBox.getValue();
        Session.selectedDatabase = selectedDBfromComboBox;
    }
	
    @FXML
    public void initialize() {
        // CodeArea erstellen
        expertCodeArea = new CodeArea();
        expertCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(expertCodeArea));
        expertCodeArea.setPrefHeight(275);
        expertCodeArea.setPrefWidth(900);
        expertCodeArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 16; -fx-padding: 10 10 10 10; -fx-border-color: #665F5F; -fx-border-width: 2;");

        // Placeholder-Label erstellen
        Label placeholderLabel = new Label("Hier können Sie Tabellen und Spalten selbst erstellen.\n"
                + "Beispiel:\n"
                + "CREATE TABLE kunden (\n"
                + "    id SERIAL PRIMARY KEY,\n"
                + "    name VARCHAR(100) NOT NULL\n"
                + ")\n"
                + "Vergessen Sie bitte nicht, einen Befehl NICHT mit einem Semikolon abzuschließen");
        placeholderLabel.setStyle("-fx-text-fill: #bfbebb; -fx-font-size: 16; -fx-padding: 10;");
        placeholderLabel.setMouseTransparent(true);
        placeholderLabel.setAlignment(Pos.TOP_LEFT);
        placeholderLabel.setWrapText(true);
        placeholderLabel.setMaxWidth(850);

        // ScrollPane + StackPane
        VirtualizedScrollPane<CodeArea> scrollableCodeArea = new VirtualizedScrollPane<>(expertCodeArea);
        StackPane stack = new StackPane(scrollableCodeArea, placeholderLabel);
        expertBox.getChildren().add(stack);

        // Highlighting
        expertCodeArea.textProperty().addListener((obs, oldText, newText) -> {
            expertCodeArea.setStyleSpans(0, SQLHighlighter.computeHighlighting(newText));
            placeholderLabel.setVisible(newText.isEmpty());
        });

        // Placeholder-Verhalten
        expertCodeArea.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && expertCodeArea.getText().isEmpty()) {
                placeholderLabel.setVisible(false);
            } else if (!newVal && expertCodeArea.getText().isEmpty()) {
                placeholderLabel.setVisible(true);
            }
        });

        // Abstand Zeilenanzeige zum Text
        expertCodeArea.setParagraphGraphicFactory(line -> {
            Label lineNo = new Label(String.format("%4d", line + 1));
            lineNo.setStyle("-fx-text-fill: gray; -fx-font-size: 12; -fx-font-weight: bold;");
            StackPane spacer = new StackPane(lineNo);
            spacer.setPadding(new Insets(0, 15, 0, 0));
            return spacer;
        });

        // ComboBox für Datentypen
        ObservableList<String> types = FXCollections.observableArrayList();
        types.add("-- Datentyp --");
        types.addAll(Table.POSTGRES_TYPES);
        dataTypeComboBox.setItems(types);

        dataTypeComboBox.setOnAction(e -> {
        	String selected = dataTypeComboBox.getValue();
        	boolean parameterNeeded = selected != null && !selected.equals("-- Datentyp --") && Table.needsParameter(selected);
        	typeParameterField.setDisable(!parameterNeeded);
        	typeParameterField.setPromptText(parameterNeeded ? "Parameter eingeben" : "Parameter eingeben");
        });

        if (databaseComboBox != null) {
            updateDatabaseList();
        }

        autoResizeColumns();
        setupTableView();
        addDeleteColumn();
    }
	
	private void setupTableView() {
	    columnTableView.setEditable(true);

	    columnNameColumn.setCellValueFactory(cellData -> cellData.getValue().columnNameProperty());
	    columnNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
	    columnNameColumn.setEditable(true);

	    dataTypeColumn.setCellValueFactory(cellData -> cellData.getValue().fullTypeProperty());
	    dataTypeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
	    dataTypeColumn.setEditable(true);

	    notNullColumn.setText("Not Null");
	    notNullColumn.setCellValueFactory(cellData -> cellData.getValue().notNullProperty());
	    notNullColumn.setCellFactory(CheckBoxTableCell.forTableColumn(notNullColumn));
	    notNullColumn.setEditable(true);

	    primaryKeyColumn.setText("Primary Key");
	    primaryKeyColumn.setCellValueFactory(cellData -> cellData.getValue().primaryKeyProperty());
	    primaryKeyColumn.setCellFactory(CheckBoxTableCell.forTableColumn(primaryKeyColumn));
	    primaryKeyColumn.setEditable(true);

	    uniqueColumn.setText("Unique");
	    uniqueColumn.setCellValueFactory(cellData -> cellData.getValue().uniqueProperty());
	    uniqueColumn.setCellFactory(CheckBoxTableCell.forTableColumn(uniqueColumn));
	    uniqueColumn.setEditable(true);

	    defaultValueColumn.setCellValueFactory(cellData -> cellData.getValue().defaultValueProperty());
	    defaultValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
	    defaultValueColumn.setEditable(true);

	    checkConstraintColumn.setCellValueFactory(cellData -> cellData.getValue().checkConstraintProperty());
	    checkConstraintColumn.setCellFactory(TextFieldTableCell.forTableColumn());
	    checkConstraintColumn.setEditable(true);

	    optionsColumn.setCellValueFactory(cellData -> cellData.getValue().optionsProperty());
	    optionsColumn.setCellFactory(TextFieldTableCell.forTableColumn());
	    optionsColumn.setEditable(true);

	    columnTableView.getColumns().setAll(
	    	    List.of(
	    	        columnNameColumn,
	    	        dataTypeColumn,
	    	        notNullColumn,
	    	        primaryKeyColumn,
	    	        uniqueColumn,
	    	        defaultValueColumn,
	    	        checkConstraintColumn,
	    	        optionsColumn
	    	    )
	    	);

	    columnTableView.itemsProperty().addListener((obs, oldVal, newVal) -> autoResizeColumns());
	    columnTableView.setItems(columnData);
	}
	
	
	public void executeExpertSQL() {
	    String sql = expertCodeArea.getText().trim();

	    if (sql.isEmpty()) {
	        showAlert(Alert.AlertType.WARNING, "Eingabefehler", "SQL-Befehl eingeben");
	        return;
	    }

	    if (Session.selectedDatabase == null) {
	        showAlert(Alert.AlertType.ERROR, "Fehler", "Keine Datenbank ausgewählt.");
	        return;
	    }

	    try {
	        String result = tableService.ExpertSQL(Session.selectedDatabase, sql);
	        if (result == null) {
	            showAlert(Alert.AlertType.INFORMATION, "Erfolg", "Tabellenbefehl erfolgreich!");
	            expertCodeArea.clear();
	        } else {
	            showAlert(Alert.AlertType.ERROR, "Fehler", result);
	        }
	    } catch (Exception e) {
	        showAlert(Alert.AlertType.ERROR, "SQL-Fehler", "Fehler beim Ausführen des Befehls:\n" + e.getMessage());
	        e.printStackTrace();
	    }
	}

	
	@FXML
	public void ExpertMode() {
		boolean expertModeActive = !expertBox.isVisible();

	    // Expertenmodus sichtbar/unsichtbar Schaltung
	    expertBox.setVisible(expertModeActive);
	    expertBox.setManaged(expertModeActive);
	    executeExpertSQLButton.setVisible(expertModeActive);
	    executeExpertSQLButton.setManaged(expertModeActive);
	    ExpertModeButton.setText(expertModeActive ? "Modus beenden" : "Expertenmodus");

	    // Normalmodus deaktiviert, solange Expertenmodus aktiv
	    tableNameField.setDisable(expertModeActive);
	    columnNameField.setDisable(expertModeActive);
	    dataTypeComboBox.setDisable(expertModeActive);
	    typeParameterField.setDisable(expertModeActive);
	    defaultValueField.setDisable(expertModeActive);
	    checkConstraintField.setDisable(expertModeActive);
	    OptionsField.setDisable(expertModeActive);
	    notNullCheckBox.setDisable(expertModeActive);
	    primaryKeyCheckBox.setDisable(expertModeActive);
	    uniqueCheckBox.setDisable(expertModeActive);
	    columnTableView.setVisible(!expertModeActive);
	    columnTableView.setManaged(!expertModeActive);

	    addColumnButton.setVisible(!expertModeActive);
	    addColumnButton.setManaged(!expertModeActive);

	    createTableButton.setVisible(!expertModeActive);
	    createTableButton.setManaged(!expertModeActive);

	}


	
	@FXML
	public void addColumn(ActionEvent e) {
	    String name = columnNameField.getText().trim();
	    String type = dataTypeComboBox.getValue();
	    String parameter = typeParameterField.getText().trim();


	    if (name.isEmpty()) {
	        showAlert(Alert.AlertType.WARNING, "Fehler", "Spaltenname darf nicht leer sein.");
	        return;
	    }

	    if (type == null || type.isEmpty() || type.equals("-- Datentyp --")) {
	        showAlert(Alert.AlertType.WARNING, "Fehler", "Datentyp wählen!");
	        return;
	    }
	    
	    if (type.equals("VARCHAR") && (parameter == null || parameter.isEmpty())) {
	    	showAlert(Alert.AlertType.ERROR, "Ungültiger Datentyp", "Bitte Länge für VARCHAR angeben.");
	        return;
	    }

	    columnData.add(new TableColumnDefinition(
	    	    name,
	    	    type,
	    	    parameter,
	    	    notNullCheckBox.isSelected(),
	    	    primaryKeyCheckBox.isSelected(),
	    	    uniqueCheckBox.isSelected(),
	    	    defaultValueField.getText().trim(),
	    	    checkConstraintField.getText().trim(),
	    	    OptionsField.getText().trim()
	    	));

	    columnNameField.clear();
	    dataTypeComboBox.setValue("-- Datentyp --");
	    typeParameterField.clear();
	    defaultValueField.clear();
	    checkConstraintField.clear();
	    OptionsField.clear();
	    notNullCheckBox.setSelected(false);
	    primaryKeyCheckBox.setSelected(false);
	    uniqueCheckBox.setSelected(false);
	}
	
	
	@FXML
    public void createTable() {
        String tableName = tableNameField.getText().trim();

        if (tableName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Eingabefehler", "Bitte geben Sie einen Tabellennamen ein.");
            return;
        }

        if (columnData.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Eingabefehler", "Bitte fügen Sie mindestens eine Spalte hinzu.");
            return;
        }

        if (Session.selectedDatabase == null) {
            showAlert(Alert.AlertType.ERROR, "Fehler", "Keine Datenbank ausgewählt.");
            return;
        }
        
        try {
        	String error = tableService.createTable(Session.selectedDatabase, tableName, columnData);
        	if (error == null) {
        	    showAlert(Alert.AlertType.INFORMATION, "Erfolg", "Tabelle erfolgreich erstellt.");
        	    tableNameField.clear();
        	    columnData.clear();
        	    columnNameField.clear();
        	    typeParameterField.clear();
        	    defaultValueField.clear();
        	    checkConstraintField.clear();
        	    OptionsField.clear();
        	    dataTypeComboBox.getSelectionModel().clearSelection();
        	    notNullCheckBox.setSelected(false);
        	    primaryKeyCheckBox.setSelected(false);
        	    uniqueCheckBox.setSelected(false);
        	} else {
        	    showAlert(Alert.AlertType.ERROR, "Fehler", error);
        	}
        } catch (Exception e) {
        	showAlert(Alert.AlertType.ERROR, "Datenbankfehler", "Fehler beim Erstellen der Tabelle:\n" + e.getMessage());
            e.printStackTrace();
        }
    }
	
	
	// TableView Spalte löschen
	private void addDeleteColumn() {
	    // Prüfen, ob Spalte existiert (Vermeidung Doppelung)
	    boolean deleteColumnExists = columnTableView.getColumns().stream()
	        .anyMatch(col -> "Löschen".equals(col.getText()));
	    if (deleteColumnExists)
	    	return;

	    TableColumn<TableColumnDefinition, Void> deleteColumn = new TableColumn<>("Löschen");

	    Callback<TableColumn<TableColumnDefinition, Void>, TableCell<TableColumnDefinition, Void>> cellFactory =
	        new Callback<TableColumn<TableColumnDefinition, Void>, TableCell<TableColumnDefinition, Void>>() {
	            @Override
	            public TableCell<TableColumnDefinition, Void> call(final TableColumn<TableColumnDefinition, Void> param) {
	                return new TableCell<TableColumnDefinition, Void>() {
	                    private final Button deleteButton = new Button("X");

	                    {
	                        deleteButton.setOnAction((ActionEvent event) -> {
	                            		TableColumnDefinition item = getTableView().getItems().get(getIndex());
	                            		getTableView().getItems().remove(item);
	                        });
	                        deleteButton.getStyleClass().add("delete-button");
	                    }

	                    @Override
	                    public void updateItem(Void item, boolean empty) {
	                        super.updateItem(item, empty);
	                        if (empty) {
	                            setGraphic(null);
	                            setText(null);
	                        } else {
	                            deleteButton.getStyleClass().add("delete-button");

	                            StackPane wrapper = new StackPane(deleteButton);
	                            wrapper.setPrefSize(20, 18);
	                            wrapper.setMaxSize(24, 22);
	                            setGraphic(wrapper);
	                            setText(null);
	                        }
	                    }
	                };
	            }
	        };
	    deleteColumn.setCellFactory(cellFactory);
	    deleteColumn.setText("Löschen");
	    deleteColumn.setPrefWidth(55);
	    deleteColumn.setStyle("-fx-alignment: CENTER;");
	    columnTableView.getColumns().add(deleteColumn);
	}
	
	
	private void autoResizeColumns() {
	    for (TableColumn<?, ?> column : columnTableView.getColumns()) {
	        column.setPrefWidth(Math.max(100, column.getWidth()));
	    }
	}
	
	
	@FXML
	public void showTables() {
	    PopupManager.openTableListPopup();
	}
	
	@FXML
    public void backToMain() {
        ViewSwitcher.switchTo("/gui_views/start.fxml");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
