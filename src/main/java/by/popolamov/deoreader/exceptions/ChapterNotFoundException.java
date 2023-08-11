package by.popolamov.deoreader.exceptions;

import nl.siegmann.epublib.domain.Resource;

public class ChapterNotFoundException extends Exception {
    public ChapterNotFoundException(String chapterTitle) {
        super("Chapter not found: " + chapterTitle);
    }
}

