package by.popolamov.deoreader.strategy;

import javafx.scene.control.TreeView;

public class BookReaderContext {
    private BookReaderStrategy bookReaderStrategy;

    public BookReaderContext(BookReaderStrategy bookReaderStrategy) {
        this.bookReaderStrategy = bookReaderStrategy;
    }

    public void setBookReaderStrategy(BookReaderStrategy bookReaderStrategy) {
        this.bookReaderStrategy = bookReaderStrategy;
    }

    public void showCurrentPage(int pageIndex) throws Exception {
        if (bookReaderStrategy != null) {
            bookReaderStrategy.showCurrentPage(pageIndex);
        }
    }

    public void showNextPage() throws Exception {
        if (bookReaderStrategy != null) {
            bookReaderStrategy.showNextPage();
        }
    }

    public void showPreviousPage() throws Exception {
        if (bookReaderStrategy != null) {
            bookReaderStrategy.showPreviousPage();
        }
    }

    public void handleHyperlinkClick(String location) throws Exception {
        if (bookReaderStrategy != null) {
            bookReaderStrategy.handleHyperlinkClick(location);
        }
    }

    public void findResourceIndexByHref(String href) throws Exception {
        if (bookReaderStrategy != null) {
            bookReaderStrategy.findResourceIndexByHref(href);
        }
    }

    public void replaceImageReferences(String htmlContent) throws Exception {
        if (bookReaderStrategy != null) {
            bookReaderStrategy.replaceImageReferences(htmlContent);
        }
    }

    public int findChapterIndex(String chapterTitle) throws Exception {
        if (bookReaderStrategy != null) {
            bookReaderStrategy.findChapterIndex(chapterTitle);
        }
        return 0;
    }

    public void updateChapterTreeView(TreeView<String> chapterTreeView) throws Exception {
        if (bookReaderStrategy != null) {
            bookReaderStrategy.updateChapterTreeView(chapterTreeView);
        }
    }
}

