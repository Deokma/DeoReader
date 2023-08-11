package by.popolamov.deoreader.strategy;

import by.popolamov.deoreader.exceptions.ChapterNotFoundException;
import by.popolamov.deoreader.exceptions.ResourceNotFoundException;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.web.WebView;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class EPUBBookReaderStrategy extends BookReaderStrategy {
    private Book book;
    private int currentPageIndex = 0;
    private Map<String, Integer> chapterPageMap = new HashMap<>();

    public EPUBBookReaderStrategy(WebView webView, Book book) {
        super(webView);
        this.book = book;
    }

    @Override
    public void showCurrentPage(int pageIndex) throws Exception {
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
                String chapterTitle = book.getContents().get(currentPageIndex).getTitle();
                int chapterPage = chapterPageMap.getOrDefault(chapterTitle, 0);
                System.out.println("Chapter: " + chapterTitle + ", Page: " + chapterPage);
                webEngine.loadContent(contentWithEmbeddedImages);
            }
        }
    }

    @Override
    public void showNextPage() throws Exception {
        if (currentPageIndex < book.getContents().size() - 1) {
            currentPageIndex++;
            showCurrentPage(currentPageIndex);
        }
    }

    @Override
    public void showPreviousPage() throws Exception {
        if (currentPageIndex > 0) {
            currentPageIndex--;
            showCurrentPage(currentPageIndex);
        }
    }

    @Override
    public void handleHyperlinkClick(String location) throws Exception {
        System.out.println("Hyperlink clicked: " + location); // Отладочное сообщение
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
    public int findResourceIndexByHref(String href) throws ResourceNotFoundException {
        for (int i = 0; i < book.getContents().size(); i++) {
            Resource resource = book.getContents().get(i);
            if (href.equals(resource.getHref())) {
                return i;
            }
        }
        throw new ResourceNotFoundException(href);
    }


    @Override
    public String replaceImageReferences(String htmlContent) {
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

    @Override
    public int findChapterIndex(String chapterTitle) throws ChapterNotFoundException {
        for (int i = 0; i < book.getContents().size(); i++) {
            Resource resource = book.getContents().get(i);
            if (chapterTitle.equals(resource.getTitle())) {
                return i;
            }
        }

        throw new ChapterNotFoundException(chapterTitle);
    }


    @Override
    public void updateChapterTreeView(TreeView<String> chapterTreeView) {
        TreeItem<String> rootItem = new TreeItem<>("Table of Contents");
        chapterTreeView = new TreeView<>(rootItem);

        int pageIndex = 0;
        for (TOCReference tocReference : book.getTableOfContents().getTocReferences()) {
            TreeItem<String> chapterItem = createChapterItem(tocReference);
            rootItem.getChildren().add(chapterItem);

            // Сохраняем номер страницы для данной главы
            chapterPageMap.put(tocReference.getTitle(), pageIndex);

            // Увеличиваем счетчик страниц
            pageIndex += chapterItem.getChildren().size();
        }
        chapterTreeView.setShowRoot(false);
    }

    private TreeItem<String> createChapterItem(TOCReference tocReference) {
        TreeItem<String> chapterItem = new TreeItem<>(tocReference.getTitle());
        for (TOCReference childReference : tocReference.getChildren()) {
            chapterItem.getChildren().add(createChapterItem(childReference));
        }
        return chapterItem;
    }
}
