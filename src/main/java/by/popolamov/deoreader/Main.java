package by.popolamov.deoreader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load FXML file and create the view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("pages/main-view.fxml"));
        Pane root = loader.load();

        // Create the scene and set it on the primary stage
        Scene scene = new Scene(root, 960, 540);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
