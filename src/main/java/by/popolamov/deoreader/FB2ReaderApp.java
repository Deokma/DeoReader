package by.popolamov.deoreader;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class FB2ReaderApp extends Application {

    private WebView webView;
    private WebEngine webEngine;
    private Document book;
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
            if (selectedChapterIndex >= 0 && selectedChapterIndex < book.getElementsByTag("section").size()) {
                currentPageIndex = selectedChapterIndex;
                try {
                    showCurrentPage();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("FB2 Files", "*.fb2"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            try {
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                book = Jsoup.parse(fileInputStream, "UTF-8", "");
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

        BorderPane root = new BorderPane();
        root.setLeft(chapterListView);
        root.setBottom(navigationBox);
        root.setCenter(webView);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("FB2 Reader");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateChapterListView() {
        chapterListView.getItems().clear();
        Elements sections = book.getElementsByTag("section");
        for (Element section : sections) {
            String title = section.getElementsByTag("title").text();
            chapterListView.getItems().add(title);
        }
    }

    private void showCurrentPage() throws IOException {
        if (currentPageIndex >= 0 && currentPageIndex < book.getElementsByTag("section").size()) {
            Element section = book.getElementsByTag("section").get(currentPageIndex);
            String content = section.html();
            String contentWithEmbeddedImages = replaceImageReferences(content);
            webEngine.loadContent(contentWithEmbeddedImages);
        }
    }

    private void showNextPage() throws IOException {
        if (currentPageIndex < book.getElementsByTag("section").size() - 1) {
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
        Elements sections = book.getElementsByTag("section");
        for (int i = 0; i < sections.size(); i++) {
            Element section = sections.get(i);
            if (href.equals(section.id())) {
                return i;
            }
        }
        return -1;
    }

    private String replaceImageReferences(String htmlContent) {
        Document doc = Jsoup.parse(htmlContent);

        // Find all image elements in the HTML content
        Elements imgElements = doc.select("img");

        // Create a copy of the imgElements list to avoid ConcurrentModificationException
        List<Element> imgElementsCopy = new ArrayList<>(imgElements);

        // Replace the image URLs with Base64-encoded data
        for (Element imgElement : imgElementsCopy) {
            String src = imgElement.attr("src");
            if (!src.startsWith("data:")) {
                try {
                    Element parent = imgElement.parent();
                    byte[] imageData = readImageResource(src);
                    String base64Image = Base64.getEncoder().encodeToString(imageData);

                    // Set the new image source with Base64 data
                    imgElement.attr("src", "data:image/png;base64," + base64Image);

                    // Update the parent element with the modified image element
                    parent.empty();
                    parent.appendChild(imgElement);
                } catch (IOException ex) {
                    // If an image resource is not found, remove the imgElement
                    imgElement.remove();
                }
            }
        }

        // Return the modified HTML content
        return doc.outerHtml();
    }


    private byte[] readImageResource(String src) throws IOException {
        String basePath = src.contains("/") ? src.substring(0, src.lastIndexOf("/") + 1) : "";

        // Check if the src is not empty
        if (!src.isEmpty()) {
            Element section = book.getElementsByAttributeValue("id", src).first();
            if (section != null) {
                String type = section.attr("content-type");
                String href = section.attr("href");

                String imageUrl = basePath + href;

                InputStream inputStream = new FileInputStream(imageUrl);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                return byteArrayOutputStream.toByteArray();
            }
        }

        // If the src is empty or image resource not found, return an empty byte array
        return new byte[0];
    }


    public static void main(String[] args) {
        launch(args);
    }
}
