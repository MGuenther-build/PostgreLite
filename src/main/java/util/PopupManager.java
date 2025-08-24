package util;

import controller.DBConfigController;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import service.Excelimport;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;



public class PopupManager {

    public static void openConfigPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(PopupManager.class.getResource("/gui_views/db_config.fxml"));
            Parent view = loader.load();
            DBConfigController controller = loader.getController();

            Stage stage = new Stage();
            stage.setScene(new Scene(view));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Neue Konfiguration");
            stage.showAndWait();
            stage.centerOnScreen();

            if (controller.isConfigSuccessful()) {
                showInfo("Neue Konfiguration erfolgreich übernommen!");
            } else {
                showError("ACHTUNG! Einstellungen wurden NICHT geändert!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Fehler beim Öffnen der Konfiguration:\n" + e.getMessage());
        }
    }

    
    
    public static void openAboutPopup() {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(StageStyle.UTILITY);
        popup.setTitle("Über diese App");

        Text header1 = new Text("Dashboard\n");
        header1.setStyle("-fx-underline: true; -fx-font-weight: bold; -fx-font-size: 18px; -fx-fill: black;");

        Text body1 = new Text("Das Dashboard ist der zentrale Platz, in dem Sie alle Funktionen der App sehen und aufrufen können.\n\n");
        body1.setStyle("-fx-font-size: 16px; -fx-fill: black;");

        Text header2 = new Text("Datenbank erstellen\n");
        header2.setStyle("-fx-underline: true; -fx-font-weight: bold; -fx-font-size: 18px; -fx-fill: black;");

        Text body2 = new Text("In diesem Bereich erstellen Sie mit einem Klick eine Datenbank nach Wahl. Sobald eine Datenbank erstellt ist, können Sie durch den dann entgrauten Weiter-Button auch sofort zum Tabelle erstellen weitergehen.\n\n");
        body2.setStyle("-fx-font-size: 16px; -fx-fill: black;");

        Text header3 = new Text("Tabelle erstellen\n");
        header3.setStyle("-fx-underline: true; -fx-font-weight: bold; -fx-font-size: 18px; -fx-fill: black;");

        Text body3 = new Text("In diesem Bereich erstellen Sie Tabellen und Spalten. Sie können entweder die Maske dafür nutzen oder im Expertenmodus freihand Tabelle und Spalten formulieren. Es ist in diesem Bereich nicht möglich Strukturbefehle wie Delete oder Drop durchzuführen! Im Expertenmodus gilt zu beachten, dass Befehle aus Sicherheitsgründen (SQL-Injektion!) ohne \";\" (= Semikolon) zu beenden sind. Wenn Sie eine Datenbank ausgewählt haben, können Sie im Button rechts daneben auch sofort einsehen, welche Tabellen in dieser Datenbank bereits vorhanden sind.\n\n"
                + "Bitte bedenken Sie, dass Tabellen, die Sie mit einem Großbuchstaben am Anfang erstellt haben, bei der Abfrage in doppelte Anführungsstriche gesetzt werden müssen. Postgre ist da besonders streng und wandelt sog. unquoted Bezeichner konsequent in Kleinbuchstaben um! Man kann das ganz elegant damit umgehen, indem man Tabellennamen grundsätzlich kleinschreibt.\n\n");
        body3.setStyle("-fx-font-size: 16px; -fx-fill: black;");

        Text header4 = new Text("QueryTool\n");
        header4.setStyle("-fx-underline: true; -fx-font-weight: bold; -fx-font-size: 18px; -fx-fill: black;");

        Text body4 = new Text("In diesem Bereich können Sie aus den Datenbanken Ihre Datenbank wählen, die in ihr vorhandenen Tabellen einsehen und anschließend freihändig fast jede Art von Datenabfrage und Datenbearbeitung durchführen. Es ist in diesem Bereich nicht möglich Strukturbefehle wie Delete, Drop und Truncate durchzuführen oder Nutzer anlegen bzw. Nutzerrechte auszusprechen. Dafür bitte den Adminbereich nutzen!\n\n"
                + "Bitte beachten Sie, dass aus Sicherheitsgründen (SQL-Injektion!) Befehle ohne \";\" (= Semikolon) zu beenden sind.\n\n"
        		+"Ein Import von Excel ist möglich.\n\n");
        body4.setStyle("-fx-font-size: 16px; -fx-fill: black;");

        Text header5 = new Text("Adminbereich\n");
        header5.setStyle("-fx-underline: true; -fx-font-weight: bold; -fx-font-size: 18px; -fx-fill: black;");

        Text body5 = new Text("Der Adminbereich ist der sensibelste Bereich und sollte nur genutzt werden, wenn man weiß, was man tut. In diesem besonderen Bereich gibt es keine Verbote. Hier kann man in die Struktur einer Datenbank eindringen und z.B. Drop-, Truncate- und Delete-Befehle ausführen, sowie Nutzer erstellen und Rechte erstellen.\n\n");
        body5.setStyle("-fx-underline: true; -fx-font-size: 16px; -fx-fill: black;");

        Text header6 = new Text("Verbindungsstatus anzeigen\n");
        header6.setStyle("-fx-underline: true; -fx-font-weight: bold; -fx-font-size: 18px; -fx-fill: black;");

        Text body6 = new Text("Ein kleines Widget, um einen Überblick darüber zu behalten, mit welchem Status man gerade eingeloggt ist.\n\n");
        body6.setStyle("-fx-font-size: 16px; -fx-fill: black;");

        Text header7 = new Text("Konfiguration ändern\n");
        header7.setStyle("-fx-underline: true; -fx-font-weight: bold; -fx-font-size: 18px; -fx-fill: black;");

        Text body7 = new Text("Wenn Sie während der Session Host, Benutzer oder Port ändern wollen, dann kann dies über dieses kleine Widget geschehen, ohne die App zu schließen und neu starten zu müssen.\n\n");
        body7.setStyle("-fx-font-size: 16px; -fx-fill: black;");
        
        Text header8 = new Text("Impressum\n");
        header8.setStyle("-fx-underline: true; -fx-font-weight: bold; -fx-font-size: 18px; -fx-fill: black;");

        Text body8 = new Text("www.MGuenther-build.de\n"
                + "Ich hafte nicht für Schäden, die durch die Nutzung dieser App entstehen können. Diese App ist frei zu nutzen und nach Belieben zu verändern, sofern ich aus dieser veränderten App als Urheber entfernt bin!\n\n");
        body8.setStyle("-fx-font-size: 16px; -fx-fill: black;");

        Text header9 = new Text("Schlussanmerkung\n");
        header9.setStyle("-fx-underline: true; -fx-font-weight: bold; -fx-font-size: 18px; -fx-fill: black;");

        Text body9 = new Text("Diese App ist optimiert für Bildschirmauflösungen ab 1920 x 1080 Pixel. Sie ist für die Handynutzung nicht geeignet!\n\n");
        body9.setStyle("-fx-font-size: 16px; -fx-fill: black;");

        TextFlow textFlow = new TextFlow(
        		header1, body1,
        		header2, body2,
        		header3, body3,
        		header4, body4,
        		header5, body5,
        		header6, body6,
        		header7, body7,
        		header8, body8,
        		header9, body9
        );
        textFlow.setPadding(new Insets(15));
        textFlow.setPrefWidth(480);
        textFlow.setLineSpacing(2);
        textFlow.setStyle("-fx-background-color: transparent;");

        ScrollPane scrollPane = new ScrollPane(textFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(500, 400);
        scrollPane.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-control-inner-background: transparent; " +
            "-fx-background-insets: 0; " +
            "-fx-padding: 0;"
        );

        VBox container = new VBox(scrollPane);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(20));
        container.setSpacing(10);
        container.setStyle("-fx-background-color: linear-gradient(to bottom, #665F5F, #000000);");

        Scene popupScene = new Scene(container);
        popup.setScene(popupScene);
        popup.setResizable(false);
        popup.showAndWait();
        popup.centerOnScreen();
    }

    
    
    public static void showConnectionStatus() {
        String host = service.ConnectionManager.getHost();
        String port = service.ConnectionManager.getPort();
        String user = service.ConnectionManager.getUser();

        Text header = new Text("Aktuelle Verbindung\n\n");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-fill: white;");

        // Host
        Text hostLabel = new Text("Host:  ");
        hostLabel.setStyle("-fx-font-size: 16px; -fx-fill: white;");
        Text hostValue = new Text((host != null ? host : "(nicht gesetzt)") + "\n");
        hostValue.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: white;");

        // Port
        Text portLabel = new Text("Port:  ");
        portLabel.setStyle("-fx-font-size: 16px; -fx-fill: white;");
        Text portValue = new Text((port != null ? port : "(nicht gesetzt)") + "\n");
        portValue.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: white;");

        // Benutzer
        Text userLabel = new Text("Benutzer:  ");
        userLabel.setStyle("-fx-font-size: 16px; -fx-fill: white;");
        Text userValue = new Text((user != null ? user : "(nicht gesetzt)") + "\n");
        userValue.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: white;");

        // TextFlow mit getrennten Text-Elementen
        TextFlow textFlow = new TextFlow(
            header,
            hostLabel, hostValue,
            portLabel, portValue,
            userLabel, userValue
        );
        textFlow.setPadding(new Insets(5));
        textFlow.setPrefWidth(400);
        textFlow.setStyle("-fx-background-color: transparent;");

        VBox container = new VBox(textFlow);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(20));
        container.setSpacing(10);
        container.setStyle("-fx-background-color: linear-gradient(to bottom, #665F5F, #000000);");

        Scene scene = new Scene(container);

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(StageStyle.UTILITY);
        popup.setTitle("Verbindungsstatus");
        popup.setScene(scene);
        popup.setResizable(false);
        popup.showAndWait();
        popup.centerOnScreen();
    }
    
    
    
    public static void openAdminZone() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Adminbereich");
        alert.setContentText(
            "Im Adminbereich ist jeder Befehl erlaubt. Es gibt hier keine Hilfen! Alles, was Sie hier ausführen, kann nicht rückgängig gemacht werden.\n\n" +
            "Verwenden Sie dieses Tool nur, wenn Sie genau wissen, was Sie tun."
        );

        ButtonType okButton = new ButtonType("Ok, verstanden");
        ButtonType cancelButton = new ButtonType("Abbrechen", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(okButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == okButton) {
            try {
                // AdminZone.fxml laden
                FXMLLoader loader = new FXMLLoader(PopupManager.class.getResource("/gui_views/adminZone.fxml"));
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Adminbereich");
                stage.setScene(new Scene(root));

                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                stage.setWidth(screenBounds.getWidth() * 0.75);
                stage.setHeight(screenBounds.getHeight() * 0.75);
                stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
                stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);

                stage.initModality(Modality.APPLICATION_MODAL); // blockiert Hauptfenster
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    
    public static void openTableListPopup() {
        try {
            Parent view = FXMLLoader.load(PopupManager.class.getResource("/gui_views/tablesView.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(view));
            stage.setTitle("Tabellen vorhanden");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Fehler beim Öffnen der Tabellenansicht:\n" + e.getMessage());
        }
    }

    
    
    private static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    
    
    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    
    
    public static String askForExcel(String title, String message) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("");
        dialog.setHeaderText(null);
        dialog.setContentText(message);

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
    
    

    public static Map<String, String> openExcelImportPopup(Window owner) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(owner);
        popup.setTitle("Excel-Import");

        // Icon + Titel
        Label title = new Label("Excel-Import starten");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        ImageView icon = new ImageView(new Image("/icons/like_excel.png"));
        icon.setFitHeight(24);
        icon.setFitWidth(24);
        HBox header = new HBox(10, icon, title);
        header.setAlignment(Pos.CENTER_LEFT);

        // Eingabefelder
        TextField tableNameField = new TextField();
        tableNameField.setPromptText("Tabellenname");

        TextField startCellField = new TextField();
        startCellField.setPromptText("Startzelle (z.B. A1)");

        // Datei wählen
        Button chooseFileBtn = new Button("Datei wählen");
        Label fileLabel = new Label("Keine Datei ausgewählt");
        File[] selectedFile = new File[1];

        chooseFileBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel-Dateien", "*.xlsx"));
            File file = chooser.showOpenDialog(popup);
            if (file != null) {
                selectedFile[0] = file;
                fileLabel.setText(file.getName());
            }
        });

        // Vorschau-Tabelle
        TableView<Map<String, String>> previewTable = new TableView<>();
        previewTable.setPrefHeight(200);

        Button previewBtn = new Button("Vorschau laden");
        previewBtn.setOnAction(e -> {
            if (selectedFile[0] == null || startCellField.getText().isBlank()) return;
            try {
                List<Map<String, String>> data = new Excelimport().readExcel(selectedFile[0], startCellField.getText().trim());
                previewTable.getColumns().clear();
                previewTable.getItems().clear();

                if (!data.isEmpty()) {
                    Map<String, String> firstRow = data.get(0);
                    for (String key : firstRow.keySet()) {
                        TableColumn<Map<String, String>, String> col = new TableColumn<>(key);
                        col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(key)));
                        previewTable.getColumns().add(col);
                    }
                    previewTable.getItems().addAll(data.subList(0, Math.min(15, data.size())));
                }
            } catch (Exception ex) {
                previewTable.setPlaceholder(new Label("Fehler beim Laden der Vorschau"));
            }
        });

        // Buttons
        Button importBtn = new Button("Import starten");
        Button cancelBtn = new Button("Abbrechen");

        importBtn.setOnAction(e -> {
            if (tableNameField.getText().isBlank() || startCellField.getText().isBlank()) return;
            Map<String, String> result = new HashMap<>();
            result.put("tableName", tableNameField.getText().trim());
            result.put("startCell", startCellField.getText().trim());
            popup.setUserData(result);
            popup.close();
        });

        cancelBtn.setOnAction(e -> {
            popup.setUserData(null);
            popup.close();
        });

        VBox layout = new VBox(15,
            header,
            new Label("Tabellenname:"), tableNameField,
            new Label("Startzelle:"), startCellField,
            new HBox(10, chooseFileBtn, fileLabel),
            previewBtn,
            previewTable,
            new HBox(10, importBtn, cancelBtn)
        );
        VBox.setVgrow(previewTable, Priority.ALWAYS);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10;");

        Scene scene = new Scene(layout);
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        popup.setWidth(screenBounds.getWidth() * 0.6);
        popup.setHeight(screenBounds.getHeight() * 0.6);
        popup.setScene(scene);
        popup.showAndWait();
        popup.centerOnScreen();

        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) popup.getUserData();
        return result;
    }
}
