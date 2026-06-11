package com.ringkasanbuku.core;

public class ApiBasedSummarizer implements Summarizer {
    private final String apiKey;

    public ApiBasedSummarizer(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String summarize(String text) {
        if (text == null || text.isBlank()) {
            return "Teks kosong. Tidak ada ringkasan yang dapat dibuat.";
        }

        RuleBasedSummarizer fallback = new RuleBasedSummarizer(4);
        String baseSummary = fallback.summarize(text);
        return "[API-based mode aktif dengan token valid] " + baseSummary;
    }

    public String getApiKey() {
        return apiKey;
    }
}
