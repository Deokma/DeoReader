package by.popolamov.deoreader;

import by.popolamov.deoreader.strategy.BookReaderContext;
import by.popolamov.deoreader.strategy.BookReaderStrategy;
import by.popolamov.deoreader.strategy.FB2BookReaderStrategy;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FB2ReaderApp extends Application {
    private WebView webView;
    private Document book;
    //private TreeView<String> chapterTreeView;

    @Override
    public void start(Stage primaryStage) {
        webView = new WebView();

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("FB2 Files", "*.fb2"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            try {
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                book = Jsoup.parse(fileInputStream, "UTF-8", "");

                BookReaderStrategy strategy = new FB2BookReaderStrategy(webView, book);
                BookReaderContext context = new BookReaderContext(strategy);

                BorderPane root = new BorderPane();
                root.setCenter(webView);
                //root.setLeft(createChapterTreeView(context));
                root.setBottom(createNavigationBox(context));

                Scene scene = new Scene(root, 1000, 600);
                primaryStage.setTitle("FB2 Reader");
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

    //TODO Нужно доработать этот метод, обязательно
    /*private VBox createChapterTreeView(BookReaderContext context) {
        chapterTreeView = new TreeView<>(); // Создаем TreeView
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

        // Заполните TreeView главами, аналогично коду в EPUBReaderApp

        VBox chapterTreeBox = new VBox(chapterTreeView);
        chapterTreeBox.setStyle("-fx-padding: 10px;");

        return chapterTreeBox;
    }*/

    private HBox createNavigationBox(BookReaderContext context) {
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

        HBox navigationBox = new HBox(prevButton, nextButton);
        navigationBox.setSpacing(10);
        navigationBox.setStyle("-fx-padding: 10px; -fx-alignment: CENTER;");

        return navigationBox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
