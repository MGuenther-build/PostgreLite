package app;

import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.util.Optional;
import controller.DBConfigController;



public class Main extends Application {

    public static StackPane rootPane;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Font.loadFont(getClass().getResourceAsStream("/fonts/LeagueGothic-Regular.ttf"), 13);
        Parent startView = FXMLLoader.load(getClass().getResource("/gui_views/start.fxml"));
        rootPane = new StackPane(startView);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width = screenBounds.getWidth();
        double height = screenBounds.getHeight();

        Scene scene = new Scene(rootPane, width, height);
        scene.getStylesheets().add("/css/styles.css");
        scene.getStylesheets().add(getClass().getResource("/css/sql-highlighting.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setTitle("PostgreLite");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/icon_all.png")));
        primaryStage.show();
        
        // Schließen nachfragen
        primaryStage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Info");
            alert.setHeaderText("Programm wirklich beenden?");

            ButtonType yesButton = new ButtonType("Ja");
            ButtonType noButton = new ButtonType("Nein");

            alert.getButtonTypes().setAll(yesButton, noButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() != yesButton) {
                event.consume();
            }
        });

    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui_views/db_config.fxml"));
    	Parent configView = loader.load();
    	DBConfigController controller = loader.getController();

        Stage configStage = new Stage();
        configStage.setScene(new Scene(configView));
        configStage.initModality(Modality.APPLICATION_MODAL);
        configStage.setTitle("In PostgreSQL anmelden");
        configStage.showAndWait();
        
        if (!controller.isConfigSuccessful()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Konfiguration fehlgeschlagen");
            alert.setHeaderText(null);
            alert.setContentText("Anmeldung nicht abgeschlossen! Möchten Sie das Programm wirklich beenden?");

            ButtonType retryButton = new ButtonType("Erneut anmelden");
            ButtonType skipButton = new ButtonType("Jetzt nicht");
            ButtonType exitButton = new ButtonType("Beenden");

            alert.getButtonTypes().setAll(retryButton, skipButton, exitButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
            	if (result.get() == retryButton) {
            		// Popup Config nochmal öffnen
	                while (true) {
	                    FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/gui_views/db_config.fxml"));
	                    Parent configView2 = loader2.load();
	                    DBConfigController controller2 = loader2.getController();

	                    Stage configStage2 = new Stage();
	                    configStage2.setScene(new Scene(configView2));
	                    configStage2.initModality(Modality.APPLICATION_MODAL);
	                    configStage2.setTitle("In PostgreSQL anmelden");
	                    configStage2.showAndWait();
	
	                    if (controller2.isConfigSuccessful()) {
	                        break;
	                    } else {
	                        Alert retryAlert = new Alert(Alert.AlertType.CONFIRMATION);
	                        retryAlert.setTitle("Konfiguration fehlgeschlagen");
	                        retryAlert.setHeaderText(null);
	                        retryAlert.setContentText("Anmeldung nicht abgeschlossen! Möchten Sie das Programm wirklich beenden?");
	                        retryAlert.getButtonTypes().setAll(retryButton, exitButton);
	
	                        Optional<ButtonType> retryResult = retryAlert.showAndWait();
	                        if (!(retryResult.isPresent() && retryResult.get() == retryButton)) {
	                            System.exit(0);
	                        }
	                    }
	                }
            	} else if (result.get() == skipButton) {
            		controller.setConfigSkipped(true);
            	} else {
            		System.exit(0);
            	}
            }
            
            if (controller.isConfigSkipped()) {
                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Keine Verbindung aktiv");
                infoAlert.setHeaderText(null);
                infoAlert.setContentText("Es besteht aktuell keine Verbindung zu einer Datenbank!");
                infoAlert.showAndWait();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
