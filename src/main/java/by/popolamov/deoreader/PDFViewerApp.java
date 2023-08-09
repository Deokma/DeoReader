package by.popolamov.deoreader;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class PDFViewerApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        WebView webView = new WebView();

        Button openButton = new Button("Open PDF File");
        openButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );

            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                String pdfPath = selectedFile.toURI().toString();
                webView.getEngine().load(
                        "https://docs.google.com/viewer?url=" + pdfPath
                );
            }
        });

        BorderPane root = new BorderPane();
        root.setTop(openButton);
        root.setCenter(webView);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("PDF Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
