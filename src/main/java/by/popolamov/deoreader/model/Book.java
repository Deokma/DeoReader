package by.popolamov.deoreader.model;

public class Book {
    private String title;
    private String author;
    private String content; // Текст книги

    public Book(String title, String author, String content) {
        this.title = title;
        this.author = author;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }
}

