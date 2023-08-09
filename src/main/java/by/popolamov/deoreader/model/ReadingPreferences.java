package by.popolamov.deoreader.model;

public class ReadingPreferences {
    private String font;
    private int fontSize;
    private String backgroundColor;

    public ReadingPreferences(String font, int fontSize, String backgroundColor) {
        this.font = font;
        this.fontSize = fontSize;
        this.backgroundColor = backgroundColor;
    }

    public String getFont() {
        return font;
    }

    public int getFontSize() {
        return fontSize;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }
}
