package com.ringkasanbuku.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Import bawaan Lucene
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.id.IndonesianAnalyzer;

public class RuleBasedSummarizer extends AbstractSummarizer {
    private static final Pattern WORD_SPLIT = Pattern.compile("[^\\p{L}\\p{N}]+", Pattern.UNICODE_CHARACTER_CLASS);

    private static final CharArraySet STOPWORDS;
    static {
        STOPWORDS = new CharArraySet(200, true);
        STOPWORDS.addAll(EnglishAnalyzer.getDefaultStopSet());
        STOPWORDS.addAll(IndonesianAnalyzer.getDefaultStopSet());
    }

    @Override
    public String summarize(String text, int sentenceCount, String lengthOption) throws Exception {
        if (!validateInput(text)) {
            return "Teks kosong. Tidak ada ringkasan yang dapat dibuat.";
        }

        List<String> sentences = splitSentences(text);
        if (sentences.size() <= sentenceCount) {
            return sentences.stream()
                    .map(String::trim)
                    .collect(Collectors.joining(" "));
        }

        Map<String, Integer> frequencies = buildWordFrequency(text);
        
        // Pass 1: Extract basic metrics to find max values for normalization
        List<SentenceMetrics> metricsList = new ArrayList<>();
        double maxFreqScore = 0.0;
        int maxLength = 0;

        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i).trim();
            if (sentence.isEmpty()) {
                continue;
            }
            SentenceMetrics metrics = calculateMetrics(i, sentence, frequencies);
            maxFreqScore = Math.max(maxFreqScore, metrics.freqScore());
            maxLength = Math.max(maxLength, metrics.validWords());
            metricsList.add(metrics);
        }

        // Pass 2: Calculate final scores with heuristic weights
        double wFreq = 0.7; // Faktor utama: frekuensi kata
        double wPos = 0.2;  // Faktor pendukung: posisi kalimat
        double wLen = 0.1;  // Faktor tambahan: panjang kalimat

        List<SentenceScore> scores = new ArrayList<>();
        int totalSentences = sentences.size();

        for (SentenceMetrics metrics : metricsList) {
            double normalizedFreq = maxFreqScore > 0 ? (metrics.freqScore() / maxFreqScore) : 0;
            
            // Position score (U-shape): kalimat di awal dan akhir cenderung lebih penting
            double normalizedPos = totalSentences > 1 ? (double) metrics.index() / (totalSentences - 1) : 0;
            double posScore = Math.abs(normalizedPos - 0.5) * 2.0; 
            
            double lenScore = maxLength > 0 ? ((double) metrics.validWords() / maxLength) : 0;

            double finalScore = (normalizedFreq * wFreq) + (posScore * wPos) + (lenScore * wLen);
            scores.add(new SentenceScore(metrics.index(), metrics.sentence(), finalScore));
        }

        List<SentenceScore> bestSentences = scores.stream()
                .sorted(Comparator.comparingDouble(SentenceScore::score).reversed())
                .limit(Math.max(1, Math.min(sentenceCount, scores.size())))
                .sorted(Comparator.comparingInt(SentenceScore::index))
                .toList();

        return bestSentences.stream()
                .map(SentenceScore::sentence)
                .collect(Collectors.joining(" "));
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

    private SentenceMetrics calculateMetrics(int index, String sentence, Map<String, Integer> frequencies) {
        String[] tokens = WORD_SPLIT.split(sentence.toLowerCase(Locale.ROOT));
        double totalFreq = 0;
        int validWords = 0;
        for (String token : tokens) {
            if (token.isBlank() || STOPWORDS.contains(token)) {
                continue;
            }
            totalFreq += frequencies.getOrDefault(token, 0);
            validWords++;
        }
        double freqScore = validWords == 0 ? 0 : totalFreq / validWords;
        return new SentenceMetrics(index, sentence, freqScore, validWords);
    }

    private record SentenceMetrics(int index, String sentence, double freqScore, int validWords) {}

    private record SentenceScore(int index, String sentence, double score) {}
}

