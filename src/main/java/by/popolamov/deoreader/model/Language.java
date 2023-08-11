package by.popolamov.deoreader.model;

public enum Language {
    ENGLISH("English"),
    RUSSIAN("Russian");

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
