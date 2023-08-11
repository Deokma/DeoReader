package by.popolamov.deoreader.strategy;

import by.popolamov.deoreader.exceptions.ChapterNotFoundException;
import by.popolamov.deoreader.exceptions.ResourceNotFoundException;
import javafx.scene.control.TreeView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public abstract class BookReaderStrategy {
    protected WebView webView;
    protected WebEngine webEngine;

    public BookReaderStrategy(WebView webView) {
        this.webView = webView;
        this.webEngine = webView.getEngine();
    }

    public abstract void showCurrentPage(int pageIndex) throws Exception;

    public abstract void showNextPage() throws Exception;

    public abstract void showPreviousPage() throws Exception;

    public abstract void handleHyperlinkClick(String location) throws Exception;

    public abstract int findResourceIndexByHref(String href) throws ResourceNotFoundException;

    public abstract String replaceImageReferences(String htmlContent);

    public abstract int findChapterIndex(String chapterTitle) throws ChapterNotFoundException;

    public abstract void updateChapterTreeView(TreeView<String> chapterTreeView);
}
