package com.ringkasanbuku.support;

import com.ringkasanbuku.core.ApiBasedSummarizer;
import com.ringkasanbuku.core.RuleBasedSummarizer;
import com.ringkasanbuku.core.Summarizer;
import com.ringkasanbuku.util.ConnectivityChecker;
import com.ringkasanbuku.util.TokenValidator;

public class SummarizerFactory {
    private boolean hasInternet;
    private boolean tokenValid;
    private final String preferredMethod;
    private final ConnectivityChecker connectivityChecker;
    private final TokenValidator tokenValidator;

    public SummarizerFactory(String preferredMethod, ConnectivityChecker connectivityChecker,
            TokenValidator tokenValidator) {
        this.preferredMethod = preferredMethod;
        this.connectivityChecker = connectivityChecker;
        this.tokenValidator = tokenValidator;
    }

    public Summarizer create(String apiKey) {
        hasInternet = connectivityChecker.hasInternet();
        tokenValid = tokenValidator.isValid(apiKey);

        if ("API-based".equalsIgnoreCase(preferredMethod) && hasInternet && tokenValid) {
            return new ApiBasedSummarizer(apiKey);
        }
        return new RuleBasedSummarizer();
    }
}