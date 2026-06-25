package com.ringkasanbuku.support;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class SummaryFormatter {
    public String format(String summary) {
        if (summary == null)
            return "";
        return summary.trim().replaceAll("\\s+", " ");
    }

    public void exportToTxt(String content, File file) throws IOException {
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
    }

    public void exportToPdf(String content, File file) throws IOException {
        PDDocument document = new PDDocument();
        try {
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.moveTextPositionByAmount(50, 750);

                // Pisahkan berdasarkan newline eksplisit, lalu wrap masing-masing
                String[] paragraphs = content.split("\n");
                boolean firstLine = true;

                for (String paragraph : paragraphs) {
                    if (paragraph.isEmpty()) {
                        contentStream.moveTextPositionByAmount(0, -16);
                        continue;
                    }
                    String[] lines = wrapText(paragraph, 80);
                    for (String line : lines) {
                        if (!firstLine) {
                            contentStream.moveTextPositionByAmount(0, -16);
                        }
                        contentStream.drawString(line);
                        firstLine = false;
                    }
                }
                contentStream.endText();
            } finally {
                contentStream.close();
            }
            document.save(file);
        } finally {
            document.close();
        }
    }

    private String[] wrapText(String text, int maxLength) {
        if (text == null || text.isEmpty())
            return new String[] { "" };
        StringBuilder builder = new StringBuilder();
        String[] words = text.split(" ");
        int currentLength = 0;
        for (String word : words) {
            if (currentLength + word.length() + 1 > maxLength) {
                builder.append('\n');
                currentLength = 0;
            }
            if (currentLength > 0) {
                builder.append(' ');
                currentLength++;
            }
            builder.append(word);
            currentLength += word.length();
        }
        return builder.toString().split("\\n");
    }
}