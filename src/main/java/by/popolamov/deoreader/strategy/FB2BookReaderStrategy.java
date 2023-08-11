package by.popolamov.deoreader.strategy;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.web.WebView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class FB2BookReaderStrategy extends BookReaderStrategy {
    private Document book;
    private int currentPageIndex = 0;

    public FB2BookReaderStrategy(WebView webView, Document book) {
        super(webView);
        this.book = book;
    }

    @Override
    public void showCurrentPage(int pageIndex) throws Exception {
        // Реализация для FB2-формата
        if (currentPageIndex >= 0 && currentPageIndex < book.getElementsByTag("section").size()) {
            Element section = book.getElementsByTag("section").get(currentPageIndex);
            String content = section.html();
            String contentWithEmbeddedImages = replaceImageReferences(content);
            webEngine.loadContent(contentWithEmbeddedImages);
        }
    }

    @Override
    public void showNextPage() throws Exception {
        // Реализация для FB2-формата
        if (currentPageIndex < book.getElementsByTag("section").size() - 1) {
            currentPageIndex++;
            showCurrentPage(currentPageIndex);
        }
    }

    @Override
    public void showPreviousPage() throws Exception {
        // Реализация для FB2-формата
        if (currentPageIndex > 0) {
            currentPageIndex--;
            showCurrentPage(currentPageIndex);
        }
    }

    @Override
    public void handleHyperlinkClick(String location) throws Exception {
        // Реализация для FB2-формата
        int resourceIndex = findResourceIndexByHref(location);
        if (resourceIndex != -1) {
            currentPageIndex = resourceIndex;
            try {
                showCurrentPage(currentPageIndex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public int findResourceIndexByHref(String href) {
        Elements sections = book.getElementsByTag("section");
        for (int i = 0; i < sections.size(); i++) {
            Element section = sections.get(i);
            if (href.equals(section.id())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String replaceImageReferences(String htmlContent) {
        Document doc = Jsoup.parse(htmlContent);

        Elements imgElements = doc.select("img");
        for (Element imgElement : imgElements) {
            String src = imgElement.attr("src");
            if (!src.startsWith("data:")) {
                try {
                    Element parent = imgElement.parent();
                    byte[] imageData = readImageResource(src);
                    String base64Image = Base64.getEncoder().encodeToString(imageData);

                    imgElement.attr("src", "data:image/png;base64," + base64Image);

                    parent.empty();
                    parent.appendChild(imgElement);
                } catch (IOException ex) {
                    imgElement.remove();
                }
            }
        }

        return doc.outerHtml();
    }

    @Override
    public int findChapterIndex(String chapterTitle) {
        Elements sections = book.getElementsByTag("section");
        for (int i = 0; i < sections.size(); i++) {
            Element section = sections.get(i);
            String title = section.getElementsByTag("title").text();
            if (chapterTitle.equals(title)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void updateChapterTreeView(TreeView<String> chapterTreeView) {
        TreeItem<String> rootItem = new TreeItem<>("Chapters");
        chapterTreeView.setRoot(rootItem);
        chapterTreeView.setShowRoot(false);

        Elements sections = book.getElementsByTag("section");
        for (Element section : sections) {
            String title = section.getElementsByTag("title").text();
            //title = Jsoup.parse(title).text(); // Удаление тегов из названия
            TreeItem<String> chapterItem = new TreeItem<>(title);
            rootItem.getChildren().add(chapterItem);
        }
    }

    private byte[] readImageResource(String src) throws IOException {
        String basePath = src.contains("/") ? src.substring(0, src.lastIndexOf("/") + 1) : "";

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

        return new byte[0];
    }
}
