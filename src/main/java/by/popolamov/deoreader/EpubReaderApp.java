package by.popolamov.deoreader;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Scanner;

public class EpubReaderApp extends Application {

    private WebView webView;
    private WebEngine webEngine;
    private Book book;
    private int currentPageIndex = 0;
    private ListView<String> chapterListView;

    @Override
    public void start(Stage primaryStage) {
        webView = new WebView();
        webEngine = webView.getEngine();
        webView.getEngine().locationProperty().addListener((observable, oldValue, newValue) -> {
            handleHyperlinkClick(newValue);
        });

        chapterListView = new ListView<>();
        chapterListView.setOnMouseClicked(e -> {
            int selectedChapterIndex = chapterListView.getSelectionModel().getSelectedIndex();
            if (selectedChapterIndex >= 0 && selectedChapterIndex < book.getContents().size()) {
                currentPageIndex = selectedChapterIndex;
                try {
                    showCurrentPage();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("EPUB Files", "*.epub"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            try {
                EpubReader epubReader = new EpubReader();
                book = epubReader.readEpub(new FileInputStream(selectedFile));
                updateChapterListView();
                showCurrentPage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Button prevButton = new Button("Previous Page");
        prevButton.setOnAction(e -> {
            try {
                showPreviousPage();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        Button nextButton = new Button("Next Page");
        nextButton.setOnAction(e -> {
            try {
                showNextPage();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        HBox navigationBox = new HBox(prevButton, nextButton);
        navigationBox.setSpacing(10);
        navigationBox.setStyle("-fx-padding: 10px; -fx-alignment: CENTER;");

        VBox leftPanel = new VBox(chapterListView);
        leftPanel.setStyle("-fx-padding: 10px;");

        BorderPane root = new BorderPane();
        root.setLeft(leftPanel);
        root.setBottom(navigationBox);
        root.setCenter(webView);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("EPUB Reader");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateChapterListView() {
        chapterListView.getItems().clear();
        for (TOCReference tocReference : book.getTableOfContents().getTocReferences()) {
            String title = tocReference.getTitle();
            chapterListView.getItems().add(title);
        }
    }


    private void showCurrentPage() throws IOException {
        if (currentPageIndex >= 0 && currentPageIndex < book.getContents().size()) {
            Resource resource = book.getContents().get(currentPageIndex);
            String mediaType = resource.getMediaType() != null ? resource.getMediaType().getName() : null;
            System.out.println(resource);
            if (mediaType != null && mediaType.startsWith("image")) {
                byte[] imageData = resource.getData();
                String base64Image = Base64.getEncoder().encodeToString(imageData);
                String htmlContent = "<html><body><img src=\"data:" + mediaType + ";base64," + base64Image + "\"/></body></html>";
                webEngine.loadContent(htmlContent);
            } else {
                InputStream inputStream = resource.getInputStream();
                Scanner scanner = new Scanner(inputStream, "UTF-8");
                StringBuilder contentBuilder = new StringBuilder();
                while (scanner.hasNextLine()) {
                    contentBuilder.append(scanner.nextLine());
                }
                scanner.close();
                String contentWithEmbeddedImages = replaceImageReferences(contentBuilder.toString());
                webEngine.loadContent(contentWithEmbeddedImages);
            }
        }
    }

    private void showNextPage() throws IOException {
        if (currentPageIndex < book.getContents().size() - 1) {
            currentPageIndex++;
            showCurrentPage();
        }
    }

    private void showPreviousPage() throws IOException {
        if (currentPageIndex > 0) {
            currentPageIndex--;
            showCurrentPage();
        }
    }

    private void handleHyperlinkClick(String location) {
        int resourceIndex = findResourceIndexByHref(location);
        if (resourceIndex != -1) {
            currentPageIndex = resourceIndex;
            try {
                showCurrentPage();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private int findResourceIndexByHref(String href) {
        for (int i = 0; i < book.getContents().size(); i++) {
            Resource resource = book.getContents().get(i);
            if (href.equals(resource.getHref())) {
                return i;
            }
        }
        return -1;
    }

    private String replaceImageReferences(String htmlContent) {
        StringBuilder contentBuilder = new StringBuilder();
        int index = 0;
        int startIndex = htmlContent.indexOf("<img", index);
        while (startIndex != -1) {
            int endIndex = htmlContent.indexOf(">", startIndex + 1);
            if (endIndex != -1) {
                contentBuilder.append(htmlContent, index, startIndex);
                String imgTag = htmlContent.substring(startIndex, endIndex + 1);
                String updatedImgTag = embedImageAsBase64(imgTag);
                contentBuilder.append(updatedImgTag);
                index = endIndex + 1;
                startIndex = htmlContent.indexOf("<img", index);
            } else {
                break;
            }
        }
        contentBuilder.append(htmlContent.substring(index));
        return contentBuilder.toString();
    }

    private String embedImageAsBase64(String imgTag) {
        int srcIndex = imgTag.indexOf("src=\"");
        int srcEndIndex = imgTag.indexOf("\"", srcIndex + 5);
        if (srcIndex != -1 && srcEndIndex != -1) {
            String src = imgTag.substring(srcIndex + 5, srcEndIndex);
            if (!src.startsWith("data:")) {
                try {
                    Resource resource = book.getResources().getByHref(src);
                    if (resource != null) {
                        String mediaType = resource.getMediaType() != null ? resource.getMediaType().getName() : null;
                        byte[] imageData = resource.getData();
                        String base64Image = Base64.getEncoder().encodeToString(imageData);

                        // Embed images based on their media types
                        if (mediaType != null && mediaType.startsWith("image")) {
                            imgTag = imgTag.replace(src, "data:" + mediaType + ";base64," + base64Image);
                        } else if (src.toLowerCase().endsWith(".png")) {
                            imgTag = imgTag.replace(src, "data:image/png;base64," + base64Image);
                        } else if (src.toLowerCase().endsWith(".jpg") || src.toLowerCase().endsWith(".jpeg")) {
                            imgTag = imgTag.replace(src, "data:image/jpeg;base64," + base64Image);
                        } else if (src.toLowerCase().endsWith(".gif")) {
                            imgTag = imgTag.replace(src, "data:image/gif;base64," + base64Image);
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return imgTag;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
