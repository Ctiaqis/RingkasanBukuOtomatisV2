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
    private static final Pattern RECORD_PATTERN = Pattern.compile(
            "\\{\\s*\\\"id\\\":\\s*(\\d+),\\s*\\\"timestamp\\\":\\s*\\\"(.*?)\\\",\\s*\\\"summary\\\":\\s*\\\"(.*?)\\\"(?:,\\s*\\\"input\\\":\\s*\\\"(.*?)\\\")?\\s*\\}",
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

    public void addRecord(String summary) {
        addRecord("", summary);
    }

    public void addRecord(String input, String summary) {
        int newId = historyList.size() + 1;
        historyList.add(new HistoryRecord(newId, LocalDateTime.now().format(formatter), summary, input));
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
                String timestamp = unescape(matcher.group(2));
                String summary = unescape(matcher.group(3));
                String input = matcher.group(4) != null ? unescape(matcher.group(4)) : "";
                historyList.add(new HistoryRecord(id, timestamp, summary, input));
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
                    .append(", \"timestamp\": \"").append(escape(record.getTimestamp())).append("\"")
                    .append(", \"summary\": \"").append(escape(record.getSummary())).append("\"")
                    .append(", \"input\": \"").append(escape(record.getInput())).append("\"}");
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