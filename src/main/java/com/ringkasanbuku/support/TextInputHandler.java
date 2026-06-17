package com.ringkasanbuku.support;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class TextInputHandler {
    public String loadFromFile(File file) throws IOException {
        if (file == null)
            throw new IllegalArgumentException("File tidak boleh null.");
        String name = file.getName().toLowerCase();

        if (name.endsWith(".txt")) {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        }
        if (name.endsWith(".pdf")) {
            PDDocument document = null;
            try {
                document = PDDocument.load(file);
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            } finally {
                if (document != null)
                    document.close();
            }
        }
        throw new IOException("Format file tidak didukung. Gunakan .txt atau .pdf.");
    }

    public String loadFromClipboard() throws IOException {
        try {
            Object data = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            return data == null ? "" : data.toString();
        } catch (UnsupportedFlavorException e) {
            throw new IOException("Clipboard tidak berisi teks.", e);
        }
    }
}