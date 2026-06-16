package com.ringkasanbuku.support;

import com.ringkasanbuku.core.ApiBasedSummarizer;
import com.ringkasanbuku.core.RuleBasedSummarizer;
import com.ringkasanbuku.core.Summarizer;

public class SummarizerFactory {

    public Summarizer create(String method, String apiKey) {
        if ("API-Based - Online".equals(method)) {
            return new ApiBasedSummarizer(apiKey);
        }
        return new RuleBasedSummarizer();
    }
}