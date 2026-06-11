package com.ringkasanbuku.core;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RuleBasedSummarizer implements Summarizer {
    private static final Pattern WORD_SPLIT = Pattern.compile("[^\\p{L}\\p{N}]+", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "dan", "di", "ke", "dari", "yang", "untuk", "dengan", "atau", "pada", "adalah", "ini", "itu",
            "the", "a", "an", "of", "to", "in", "on", "for", "is", "are", "was", "were", "be", "as", "by"
    ));

    private final int maxSentences;

    public RuleBasedSummarizer() {
        this(3);
    }

    public RuleBasedSummarizer(int maxSentences) {
        this.maxSentences = maxSentences;
    }

    @Override
    public String summarize(String text) {
        if (text == null || text.isBlank()) {
            return "Teks kosong. Tidak ada ringkasan yang dapat dibuat.";
        }

        List<String> sentences = splitSentences(text);
        if (sentences.size() <= maxSentences) {
            return sentences.stream()
                    .map(String::trim)
                    .collect(Collectors.joining(" "));
        }

        Map<String, Integer> frequencies = buildWordFrequency(text);
        List<SentenceScore> scores = new ArrayList<>();

        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i).trim();
            if (sentence.isEmpty()) {
                continue;
            }
            double score = scoreSentence(sentence, frequencies);
            scores.add(new SentenceScore(i, sentence, score));
        }

        List<SentenceScore> bestSentences = scores.stream()
                .sorted(Comparator.comparingDouble(SentenceScore::score).reversed())
                .limit(Math.max(2, Math.min(maxSentences, scores.size())))
                .sorted(Comparator.comparingInt(SentenceScore::index))
                .toList();

        return bestSentences.stream()
                .map(SentenceScore::sentence)
                .collect(Collectors.joining(" "));
    }

    private List<String> splitSentences(String text) {
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

    private Map<String, Integer> buildWordFrequency(String text) {
        Map<String, Integer> frequencies = new HashMap<>();
        for (String token : WORD_SPLIT.split(text.toLowerCase(Locale.ROOT))) {
            if (token.isBlank() || STOPWORDS.contains(token)) {
                continue;
            }
            frequencies.merge(token, 1, Integer::sum);
        }
        return frequencies;
    }

    private double scoreSentence(String sentence, Map<String, Integer> frequencies) {
        String[] tokens = WORD_SPLIT.split(sentence.toLowerCase(Locale.ROOT));
        double total = 0;
        int validWords = 0;
        for (String token : tokens) {
            if (token.isBlank() || STOPWORDS.contains(token)) {
                continue;
            }
            total += frequencies.getOrDefault(token, 0);
            validWords++;
        }
        if (validWords == 0) {
            return 0;
        }
        return total / validWords;
    }

    private record SentenceScore(int index, String sentence, double score) {
    }
}
