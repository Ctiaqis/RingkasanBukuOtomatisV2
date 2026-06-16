package com.ringkasanbuku.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SummaryHistoryManager {
    // Regex adjustment for the added fields
    private static final Pattern RECORD_PATTERN = Pattern.compile(
            "\\{\\s*\\\"id\\\":\\s*(\\d+),\\s*\\\"title\\\":\\s*\\\"(.*?)\\\",\\s*\\\"method\\\":\\s*\\\"(.*?)\\\",\\s*\\\"summaryLength\\\":\\s*\\\"(.*?)\\\",\\s*\\\"input\\\":\\s*\\\"(.*?)\\\",\\s*\\\"summary\\\":\\s*\\\"(.*?)\\\",\\s*\\\"timestamp\\\":\\s*\\\"(.*?)\\\"\\s*\\}",
            Pattern.DOTALL);

    private final List<HistoryRecord> historyList = new ArrayList<>();
    private final Path storageFile;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public SummaryHistoryManager() {
        this(Path.of("history.json"));
    }

    public SummaryHistoryManager(Path storageFile) {
        this.storageFile = storageFile;
        loadFromFile();
    }

    public void addRecord(String title, String method, String summaryLength, String input, String summary) {
        int newId = historyList.size() + 1;
        historyList.add(new HistoryRecord(newId, title, method, summaryLength, input, summary, LocalDateTime.now().format(formatter)));
        saveToFile();
    }

    public List<HistoryRecord> getHistory() {
        return Collections.unmodifiableList(historyList);
    }

    public void clearHistory() {
        historyList.clear();
        saveToFile();
    }

    private void loadFromFile() {
        if (!Files.exists(storageFile)) {
            return;
        }

        try {
            String json = Files.readString(storageFile, StandardCharsets.UTF_8);
            Matcher matcher = RECORD_PATTERN.matcher(json);
            historyList.clear();
            while (matcher.find()) {
                int id = Integer.parseInt(matcher.group(1));
                String title = unescape(matcher.group(2));
                String method = unescape(matcher.group(3));
                String summaryLength = unescape(matcher.group(4));
                String input = unescape(matcher.group(5));
                String summary = unescape(matcher.group(6));
                String timestamp = unescape(matcher.group(7));
                historyList.add(new HistoryRecord(id, title, method, summaryLength, input, summary, timestamp));
            }
        } catch (IOException e) {
            System.err.println("Gagal memuat riwayat: " + e.getMessage());
        }
    }

    private void saveToFile() {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < historyList.size(); i++) {
            HistoryRecord record = historyList.get(i);
            json.append("  {\"id\": ").append(record.getId())
                    .append(", \"title\": \"").append(escape(record.getTitle())).append("\"")
                    .append(", \"method\": \"").append(escape(record.getMethod())).append("\"")
                    .append(", \"summaryLength\": \"").append(escape(record.getSummaryLength())).append("\"")
                    .append(", \"input\": \"").append(escape(record.getInput())).append("\"")
                    .append(", \"summary\": \"").append(escape(record.getSummary())).append("\"")
                    .append(", \"timestamp\": \"").append(escape(record.getTimestamp())).append("\"}");
            if (i < historyList.size() - 1) {
                json.append(',');
            }
            json.append('\n');
        }
        json.append(']');

        try {
            Files.writeString(storageFile, json.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Gagal menyimpan riwayat: " + e.getMessage());
        }
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private String unescape(String value) {
        return value.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }
}