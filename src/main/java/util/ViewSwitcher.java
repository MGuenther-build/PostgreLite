package util;

import app.Main;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.util.Duration;



public class ViewSwitcher {

    public static void switchTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewSwitcher.class.getResource(fxmlPath));
            Parent newView = loader.load();
            newView.setOpacity(0);

            Node oldView = Main.rootPane.getChildren().isEmpty() ? null : Main.rootPane.getChildren().get(0);

            if (oldView != null) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(1000), oldView);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    Main.rootPane.getChildren().remove(oldView);
                    Main.rootPane.getChildren().add(newView);

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), newView);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });
                fadeOut.play();
            } else {
                Main.rootPane.getChildren().add(newView);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), newView);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Fehler beim Laden von: " + fxmlPath);
        }
    }
}
