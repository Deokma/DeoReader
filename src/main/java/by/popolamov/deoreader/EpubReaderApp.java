package by.popolamov.deoreader;

import by.popolamov.deoreader.strategy.BookReaderContext;
import by.popolamov.deoreader.strategy.BookReaderStrategy;
import by.popolamov.deoreader.strategy.EPUBBookReaderStrategy;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class EpubReaderApp extends Application {
    private WebView webView;
    private Book book;

    @Override
    public void start(Stage primaryStage) {
        webView = new WebView();

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("EPUB Files", "*.epub"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            try {
                EpubReader epubReader = new EpubReader();
                book = epubReader.readEpub(new FileInputStream(selectedFile));

                BookReaderStrategy strategy = new EPUBBookReaderStrategy(webView, book);
                BookReaderContext context = new BookReaderContext(strategy);

                BorderPane root = new BorderPane();
                //root.setLeft(createChapterTreeView(context));
                root.setCenter(webView);
                root.setBottom(createNavigationBox(context));

                Scene scene = new Scene(root, 1000, 600);
                primaryStage.setTitle("EPUB Reader");
                primaryStage.setScene(scene);
                primaryStage.show();

                context.showCurrentPage(0);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    //TODO Нужно починить оглавление
    private VBox createChapterTreeView(BookReaderContext context) {
        TreeView<String> chapterTreeView = new TreeView<>();
        chapterTreeView.setVisible(false);
        chapterTreeView.setOnMouseClicked(e -> {
            TreeItem<String> selectedItem = chapterTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                String selectedChapter = selectedItem.getValue();
                try {
                    int selectedChapterIndex = context.findChapterIndex(selectedChapter);
                    if (selectedChapterIndex != -1) {
                        context.showCurrentPage(selectedChapterIndex);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // Создайте TreeView и заполните его главами

        VBox chapterTreeBox = new VBox(chapterTreeView);
        chapterTreeBox.setStyle("-fx-padding: 10px;");

        return chapterTreeBox;
    }

    private VBox createNavigationBox(BookReaderContext context) {
        Button prevButton = new Button("Previous Page");
        prevButton.setOnAction(e -> {
            try {
                context.showPreviousPage();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        Button nextButton = new Button("Next Page");
        nextButton.setOnAction(e -> {
            try {
                context.showNextPage();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        VBox navigationBox = new VBox(prevButton, nextButton);
        navigationBox.setSpacing(10);
        navigationBox.setStyle("-fx-padding: 10px; -fx-alignment: CENTER;");

        return navigationBox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
