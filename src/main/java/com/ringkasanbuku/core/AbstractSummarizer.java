package com.ringkasanbuku.core;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class AbstractSummarizer implements Summarizer {
    
    protected String cleanText(String text) {
        if (text == null) {
            return "";
        }
        return text.trim();
    }

    protected boolean validateInput(String text) {
        return text != null && !text.isBlank();
    }

    protected List<String> splitSentences(String text) {
        BreakIterator iterator = BreakIterator.getSentenceInstance(new Locale("id", "ID"));
        iterator.setText(text);

        List<String> sentences = new ArrayList<>();
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String sentence = text.substring(start, end).trim();
            if (!sentence.isEmpty()) {
                sentences.add(sentence);
            }
        }
        return sentences;
    }
}
