package com.ringkasanbuku.data;

public class HistoryRecord {
    private int id;
    private String title;
    private String method;
    private String summaryLength;
    private String input;
    private String summary;
    private String timestamp;

    public HistoryRecord() {
    }

    public HistoryRecord(int id, String title, String method, String summaryLength, String input, String summary, String timestamp) {
        this.id = id;
        this.title = title;
        this.method = method;
        this.summaryLength = summaryLength;
        this.input = input;
        this.summary = summary;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getMethod() { return method; }
    public String getSummaryLength() { return summaryLength; }
    public String getInput() { return input; }
    public String getSummary() { return summary; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return id + " | " + timestamp + " | " + title + " | " + method + " | " + summaryLength;
    }
}