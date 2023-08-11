package by.popolamov.deoreader.exceptions;

public class ResourceNotFoundException extends Exception {
    public ResourceNotFoundException(String href) {
        super("Resource not found for href: " + href);
    }
}
