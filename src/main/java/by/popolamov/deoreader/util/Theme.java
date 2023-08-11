package by.popolamov.deoreader.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Theme {
    private Properties themes;

    public Theme() {
        themes = new Properties();
        try (InputStream inputStream = getClass().getResourceAsStream("/se.properties")) {
            themes.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDefaultTheme() {
        return themes.getProperty("theme.default");
    }

    public String getThemePath(String themeName) {
        return themes.getProperty("theme." + themeName);
    }
}
