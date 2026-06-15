package com.ringkasanbuku.data;

public class HistoryRecord {
    private int id;
    private String timestamp;
    private String summary;
    private String input;

    public HistoryRecord() {
    }

    public HistoryRecord(int id, String timestamp, String summary) {
        this(id, timestamp, summary, "");
    }

    public HistoryRecord(int id, String timestamp, String summary, String input) {
        this.id = id;
        this.timestamp = timestamp;
        this.summary = summary;
        this.input = input;
    }

    public int getId() {
        return id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSummary() {
        return summary;
    }

    public String getInput() {
        return input;
    }

    @Override
    public String toString() {
        return id + " | " + timestamp + " | " + summary;
    }
}