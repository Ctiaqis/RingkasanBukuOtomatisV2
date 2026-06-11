package com.ringkasanbuku.data;

public class HistoryRecord {
    private int id;
    private String timestamp;
    private String summary;

    public HistoryRecord() {
    }

    public HistoryRecord(int id, String timestamp, String summary) {
        this.id = id;
        this.timestamp = timestamp;
        this.summary = summary;
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

    @Override
    public String toString() {
        return id + " | " + timestamp + " | " + summary;
    }
}
