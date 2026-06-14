package com.ringkasanbuku.util;

public class TokenValidator {
    public boolean isValid(String apiKey) {
        if (apiKey == null) {
            return false;
        }
        String trimmed = apiKey.trim();
        return !trimmed.isEmpty() && trimmed.length() >= 10;
    }
}