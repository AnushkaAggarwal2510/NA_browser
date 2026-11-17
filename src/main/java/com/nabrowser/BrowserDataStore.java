package com.nabrowser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class BrowserDataStore {

    private static final Path DATA_FOLDER =
            Path.of(System.getProperty("user.home"), ".na-browser");

    private static final Path HISTORY_FILE =
            DATA_FOLDER.resolve("history.txt");

    private static final Path BOOKMARK_FILE =
            DATA_FOLDER.resolve("bookmarks.txt");

    public record Visit(
            String title,
            String url,
            String visitTime
    ) {}

    public record Bookmark(
            String title,
            String url
    ) {}

    public BrowserDataStore() {
        try {
            Files.createDirectories(DATA_FOLDER);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create browser data folder", e);
        }
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(
                value.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String decode(String value) {
        return new String(
                Base64.getDecoder().decode(value),
                StandardCharsets.UTF_8
        );
    }

    // SAVE BROWSING HISTORY

    public void addHistory(String title, String url) {

        try {
            String entry = encode(title) + "|"
                    + encode(url) + "|"
                    + encode(LocalDateTime.now().toString());

            Files.writeString(
                    HISTORY_FILE,
                    entry + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    Files.exists(HISTORY_FILE)
                            ? java.nio.file.StandardOpenOption.APPEND
                            : java.nio.file.StandardOpenOption.CREATE
            );

        } catch (IOException e) {
            System.err.println("Could not save history: " + e.getMessage());
        }
    }

    // READ BROWSING HISTORY

    public List<Visit> getHistory() {

        List<Visit> visits = new ArrayList<>();

        if (!Files.exists(HISTORY_FILE)) {
            return visits;
        }

        try {
            for (String line : Files.readAllLines(HISTORY_FILE)) {

                String[] parts = line.split("\\|", -1);

                if (parts.length == 3) {
                    try {
                        visits.add(new Visit(
                                decode(parts[0]),
                                decode(parts[1]),
                                decode(parts[2])
                        ));
                    } catch (IllegalArgumentException ignored) {
                        // Ignore corrupted entries.
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read history: " + e.getMessage());
        }

        return visits;
    }

    // CLEAR HISTORY

    public void clearHistory() {

        try {
            Files.deleteIfExists(HISTORY_FILE);
        } catch (IOException e) {
            System.err.println("Could not clear history: " + e.getMessage());
        }
    }

    // SAVE BOOKMARK

    public void addBookmark(String title, String url) {

        if (isBookmarked(url)) {
            return;
        }

        try {
            String entry = encode(title) + "|" + encode(url);

            Files.writeString(
                    BOOKMARK_FILE,
                    entry + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND
            );

        } catch (IOException e) {
            System.err.println("Could not save bookmark: " + e.getMessage());
        }
    }

    // READ BOOKMARKS

    public List<Bookmark> getBookmarks() {

        List<Bookmark> bookmarks = new ArrayList<>();

        if (!Files.exists(BOOKMARK_FILE)) {
            return bookmarks;
        }

        try {
            for (String line : Files.readAllLines(BOOKMARK_FILE)) {

                String[] parts = line.split("\\|", -1);

                if (parts.length == 2) {
                    try {
                        bookmarks.add(new Bookmark(
                                decode(parts[0]),
                                decode(parts[1])
                        ));
                    } catch (IllegalArgumentException ignored) {
                        // Ignore corrupted entries.
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read bookmarks: " + e.getMessage());
        }

        return bookmarks;
    }

    // CHECK BOOKMARK

    public boolean isBookmarked(String url) {

        return getBookmarks().stream()
                .anyMatch(bookmark -> bookmark.url().equals(url));
    }

    // REMOVE BOOKMARK

    public void removeBookmark(String url) {

        List<Bookmark> bookmarks = getBookmarks();

        bookmarks.removeIf(bookmark -> bookmark.url().equals(url));

        try {
            List<String> lines = new ArrayList<>();

            for (Bookmark bookmark : bookmarks) {
                lines.add(
                        encode(bookmark.title()) + "|"
                        + encode(bookmark.url())
                );
            }

            Files.write(
                    BOOKMARK_FILE,
                    lines,
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            System.err.println("Could not remove bookmark: " + e.getMessage());
        }
    }
}
