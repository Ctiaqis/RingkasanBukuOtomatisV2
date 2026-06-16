package com.ringkasanbuku.core;

public class ApiBasedSummarizer extends AbstractSummarizer {
    private final String apiKey;

    public ApiBasedSummarizer(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String summarize(String text, int sentenceCount) throws Exception {
        if (!validateInput(text)) {
            return "Teks kosong. Tidak ada ringkasan yang dapat dibuat.";
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new Exception("API Key tidak valid atau kosong.");
        }

        RuleBasedSummarizer fallback = new RuleBasedSummarizer();
        String baseSummary = fallback.summarize(text, sentenceCount);
        return "[API-based mode aktif dengan token valid] " + baseSummary;
    }

    public String getApiKey() {
        return apiKey;
    }
}
