package com.ringkasanbuku.core;

import com.ringkasanbuku.service.OpenRouterClient;

public class ApiBasedSummarizer extends AbstractSummarizer {

    private final String apiKey;
    private final OpenRouterClient client;

    public ApiBasedSummarizer(String apiKey) {
        System.out.println("ApiBasedSummarizer dibuat");
        this.apiKey = apiKey;
        this.client = new OpenRouterClient(apiKey);
    }

    @Override
    public String summarize(String text, int sentenceCount) throws Exception {
        
        System.out.println("Masuk ApiBasedSummarizer");

        if (!validateInput(text)) {
            return "Teks kosong. Tidak ada ringkasan yang dapat dibuat.";
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new Exception("API Key tidak valid atau kosong.");
        }

        System.out.println("API KEY = " + apiKey);

        try {
            System.out.println("Akan memanggil OpenRouterClient");
            return client.summarize(text);
        } catch (Exception e) {
            System.out.println(
                "API gagal, menggunakan RuleBasedSummarizer..."
            );
            e.printStackTrace();
            RuleBasedSummarizer fallback =
                    new RuleBasedSummarizer();
            return fallback.summarize(text, sentenceCount);
        }
    }

    public String getApiKey() {
        return apiKey;
    }
}