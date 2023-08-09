package by.popolamov.deoreader.model;

public enum Language {
    ENGLISH("English"),
    RUSSIAN("Russian");
    // Добавьте другие языки, если необходимо

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    }
