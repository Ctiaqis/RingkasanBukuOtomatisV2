package com.ringkasanbuku.support;

import com.ringkasanbuku.core.ApiBasedSummarizer;
import com.ringkasanbuku.core.RuleBasedSummarizer;
import com.ringkasanbuku.core.Summarizer;

public class SummarizerFactory {

    public Summarizer create(String method, String apiKey) {
        System.out.println("Factory method = " + method);
        if ("API-Based - Online".equals(method)) {
            System.out.println("Membuat ApiBasedSummarizer");
            return new ApiBasedSummarizer(apiKey);
        }
        System.out.println("Membuat RuleBasedSummarizer");
        
        return new RuleBasedSummarizer();
    }
}