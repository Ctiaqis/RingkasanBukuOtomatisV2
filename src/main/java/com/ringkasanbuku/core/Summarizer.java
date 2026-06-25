package com.ringkasanbuku.core;

public interface Summarizer {
    String summarize(String text, int sentenceCount, String lengthOption) throws Exception;
}