package by.popolamov.deoreader.util;

import by.popolamov.deoreader.model.Language;

import java.io.*;
import java.util.Properties;

public class Settings {
    private Properties settings;

    public Settings() {
        settings = new Properties();
        try (InputStream inputStream = getClass().getResourceAsStream("/settings.properties")) {
            settings.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Language getSelectedLanguage() {
        String languageName = settings.getProperty("selectedLanguage");
        return Language.valueOf(languageName);
    }

    public void setSelectedLanguage(Language language) {
        settings.setProperty("selectedLanguage", language.name());
        saveSettings();
    }

    private void saveSettings() {
        try (OutputStream outputStream = new FileOutputStream("settings.properties")) {
            settings.store(outputStream, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
