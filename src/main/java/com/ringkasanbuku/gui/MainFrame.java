package com.ringkasanbuku.gui;

import com.ringkasanbuku.core.ApiBasedSummarizer;
import com.ringkasanbuku.core.Summarizer;
import com.ringkasanbuku.data.HistoryRecord;
import com.ringkasanbuku.data.SummaryHistoryManager;
import com.ringkasanbuku.support.SummarizerFactory;
import com.ringkasanbuku.support.SummaryFormatter;
import com.ringkasanbuku.support.TextInputHandler;
import com.ringkasanbuku.util.ConnectivityChecker;
import com.ringkasanbuku.util.TokenValidator;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainFrame extends JFrame {
    private final JTextField titleField = new JTextField();
    private final JTextArea inputArea = new JTextArea();
    private final JTextArea outputArea = new JTextArea();
    private final JComboBox<String> methodDropdown = new JComboBox<>(new String[] { "Rule-based", "API-based" });
    private final JComboBox<String> lengthDropdown = new JComboBox<>(new String[] { "Pendek", "Sedang", "Panjang" });
    private final JLabel statusLabel = new JLabel("Status: Siap");

    private final TextInputHandler textInputHandler;
    private final SummaryFormatter summaryFormatter;
    private final SummaryHistoryManager historyManager;
    private final ConnectivityChecker connectivityChecker;
    private final TokenValidator tokenValidator;

    private String currentSummary = "";
    private String apiKey = "";

    public MainFrame(TextInputHandler textInputHandler, SummaryFormatter summaryFormatter,
            SummaryHistoryManager historyManager, ConnectivityChecker connectivityChecker,
            TokenValidator tokenValidator) {
        this.textInputHandler = textInputHandler;
        this.summaryFormatter = summaryFormatter;
        this.historyManager = historyManager;
        this.connectivityChecker = connectivityChecker;
        this.tokenValidator = tokenValidator;

        setTitle("Aplikasi Ringkasan Buku Otomatis");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1000, 650));
        setLayout(new BorderLayout());

        initComponents();
        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        Color bgColor = new Color(245, 245, 245);
        Color primaryBlue = new Color(0, 120, 215);
        Color textColor = new Color(50, 50, 50);
        Font mainFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font boldFont = new Font("Segoe UI", Font.BOLD, 13);
        
        // Fallback font jika Segoe UI tidak tersedia
        if (!GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()[0].equals("Segoe UI")) {
             mainFont = new Font("SansSerif", Font.PLAIN, 13);
             boldFont = new Font("SansSerif", Font.BOLD, 13);
        }

        // Apply base background
        getContentPane().setBackground(bgColor);

        // --- BAGIAN ATAS ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(bgColor);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(bgColor);

        JPanel titleFieldPanel = new JPanel(new BorderLayout(10, 0));
        titleFieldPanel.setBackground(bgColor);
        JLabel titleLabel = new JLabel("Judul Buku/Artikel:");
        titleLabel.setFont(boldFont);
        titleLabel.setForeground(textColor);
        titleField.setFont(mainFont);
        titleField.setPreferredSize(new Dimension(300, 28));
        
        titleFieldPanel.add(titleLabel, BorderLayout.WEST);
        titleFieldPanel.add(titleField, BorderLayout.CENTER);

        JPanel topButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topButtonsPanel.setBackground(bgColor);
        JButton historyButton = createStyledButton("Riwayat", Color.WHITE, textColor, mainFont);
        JButton helpButton = createStyledButton("Bantuan", Color.WHITE, textColor, mainFont);
        topButtonsPanel.add(historyButton);
        topButtonsPanel.add(helpButton);

        headerPanel.add(titleFieldPanel, BorderLayout.CENTER);
        headerPanel.add(topButtonsPanel, BorderLayout.EAST);

        topPanel.add(headerPanel, BorderLayout.NORTH);
        add(topPanel, BorderLayout.NORTH);

        // --- BAGIAN TENGAH ---
        inputArea.setFont(mainFont);
        outputArea.setFont(mainFont);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setEditable(false);

        JPanel middlePanel = new JPanel(new GridLayout(1, 2, 15, 0));
        middlePanel.setBackground(bgColor);
        middlePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));

        // Kiri: Input Teks
        JPanel inputPanel = new JPanel(new BorderLayout(0, 8));
        inputPanel.setBackground(bgColor);
        
        JPanel inputHeaderPanel = new JPanel(new BorderLayout());
        inputHeaderPanel.setBackground(bgColor);
        JLabel inputLabel = new JLabel("Input Teks:");
        inputLabel.setFont(boldFont);
        inputLabel.setForeground(textColor);
        inputHeaderPanel.add(inputLabel, BorderLayout.WEST);
        
        JPanel inputButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        inputButtonsPanel.setBackground(bgColor);
        JButton loadFileButton = createStyledButton("Load File", Color.WHITE, textColor, mainFont);
        JButton clipboardButton = createStyledButton("Load Clipboard", Color.WHITE, textColor, mainFont);
        JButton clearButton = createStyledButton("Bersihkan", new Color(220, 53, 69), Color.WHITE, mainFont);
        inputButtonsPanel.add(loadFileButton);
        inputButtonsPanel.add(clipboardButton);
        inputButtonsPanel.add(clearButton);
        inputHeaderPanel.add(inputButtonsPanel, BorderLayout.EAST);

        inputPanel.add(inputHeaderPanel, BorderLayout.NORTH);
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        inputPanel.add(inputScroll, BorderLayout.CENTER);

        // Kanan: Hasil Ringkasan
        JPanel outputPanel = new JPanel(new BorderLayout(0, 8));
        outputPanel.setBackground(bgColor);
        JLabel outputLabel = new JLabel("Hasil Ringkasan:");
        outputLabel.setFont(boldFont);
        outputLabel.setForeground(textColor);
        outputPanel.add(outputLabel, BorderLayout.NORTH);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        outputPanel.add(outputScroll, BorderLayout.CENTER);

        middlePanel.add(inputPanel);
        middlePanel.add(outputPanel);
        add(middlePanel, BorderLayout.CENTER);

        // --- BAGIAN BAWAH ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(bgColor);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        controlPanel.setBackground(bgColor);
        
        methodDropdown.setFont(mainFont);
        methodDropdown.setBackground(Color.WHITE);
        lengthDropdown.setFont(mainFont);
        lengthDropdown.setBackground(Color.WHITE);
        
        JButton summarizeButton = createStyledButton("RINGKAS", primaryBlue, Color.WHITE, boldFont);
        summarizeButton.setPreferredSize(new Dimension(120, 35));
        
        JButton saveTxtButton = createStyledButton("Simpan TXT", Color.WHITE, textColor, mainFont);
        JButton savePdfButton = createStyledButton("Simpan PDF", Color.WHITE, textColor, mainFont);
        JButton setApiKeyButton = createStyledButton("Set API Key", Color.WHITE, textColor, mainFont);

        JLabel methodLabel = new JLabel("Metode:");
        methodLabel.setFont(mainFont);
        JLabel lengthLabel = new JLabel("Panjang:");
        lengthLabel.setFont(mainFont);

        controlPanel.add(methodLabel);
        controlPanel.add(methodDropdown);
        controlPanel.add(lengthLabel);
        controlPanel.add(lengthDropdown);
        controlPanel.add(summarizeButton);
        controlPanel.add(saveTxtButton);
        controlPanel.add(savePdfButton);
        controlPanel.add(setApiKeyButton);

        statusLabel.setFont(mainFont);
        statusLabel.setForeground(new Color(100, 100, 100));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(235, 235, 235));
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 210, 210)));
        statusPanel.add(statusLabel, BorderLayout.WEST);

        bottomPanel.add(controlPanel, BorderLayout.CENTER);
        
        JPanel footerContainer = new JPanel(new BorderLayout());
        footerContainer.setBackground(bgColor);
        footerContainer.add(bottomPanel, BorderLayout.CENTER);
        footerContainer.add(statusPanel, BorderLayout.SOUTH);
        
        add(footerContainer, BorderLayout.SOUTH);

        // --- EVENT LISTENERS ---
        loadFileButton.addActionListener(e -> handleLoadFile());
        clipboardButton.addActionListener(e -> handleLoadClipboard());
        summarizeButton.addActionListener(e -> handleSummarize());
        historyButton.addActionListener(e -> handleShowHistory());
        helpButton.addActionListener(e -> handleShowHelp());
        saveTxtButton.addActionListener(e -> handleSaveTxt());
        savePdfButton.addActionListener(e -> handleSavePdf());
        setApiKeyButton.addActionListener(e -> handleSetApiKey());
        clearButton.addActionListener(e -> handleClear());
    }

    private JButton createStyledButton(String text, Color bg, Color fg, Font font) {
        JButton btn = new JButton(text);
        btn.setFont(font);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        
        // Custom simple border
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        
        // Hilangkan border untuk tombol warna utama (biru dan merah) agar lebih "flat"
        if (bg.equals(new Color(0, 120, 215)) || bg.equals(new Color(220, 53, 69))) {
             btn.setBorder(BorderFactory.createEmptyBorder(6, 13, 6, 13));
        }
        
        return btn;
    }

    private void handleLoadFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Text and PDF", "txt", "pdf"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                inputArea.setText(textInputHandler.loadFromFile(chooser.getSelectedFile()));
                setStatus("File dimuat: " + chooser.getSelectedFile().getName());
            } catch (IOException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void handleLoadClipboard() {
        try {
            inputArea.setText(textInputHandler.loadFromClipboard());
            setStatus("Teks dimuat dari clipboard.");
        } catch (IOException ex) {
            showError(ex.getMessage());
        }
    }

    private void handleSummarize() {
        String inputText = inputArea.getText().trim();
        if (inputText.isEmpty()) {
            showError("Silakan masukkan teks terlebih dahulu.");
            return;
        }

        String method = (String) methodDropdown.getSelectedItem();
        if ("API-based".equals(method) && !tokenValidator.isValid(apiKey)) {
            handleSetApiKey();
        }

        setStatus("Meringkas...");

        SummarizerFactory factory = new SummarizerFactory(method, connectivityChecker, tokenValidator);
        Summarizer summarizer = factory.create(apiKey);

        if ("API-based".equals(method) && !(summarizer instanceof ApiBasedSummarizer)) {
            JOptionPane.showMessageDialog(this,
                    "API-based tidak dapat digunakan. Sistem otomatis menggunakan Rule-based.", "Info Fallback",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        String summary = summarizer.summarize(inputText);
        currentSummary = summaryFormatter.format(summary);
        outputArea.setText(currentSummary);
        
        historyManager.addRecord(currentSummary);
        setStatus("Selesai meringkas teks.");
    }

    private void handleShowHistory() {
        List<HistoryRecord> history = historyManager.getHistory();
        if (history.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Belum ada riwayat ringkasan.");
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (HistoryRecord record : history) {
            builder.append("ID: ").append(record.getId()).append('\n')
                    .append("Waktu: ").append(record.getTimestamp()).append('\n')
                    .append("Ringkasan: ").append(record.getSummary()).append("\n\n");
        }
        JTextArea historyArea = new JTextArea(builder.toString(), 20, 50);
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        historyArea.setEditable(false);
        historyArea.setCaretPosition(0);
        JOptionPane.showMessageDialog(this, new JScrollPane(historyArea), "Riwayat Ringkasan",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleShowHelp() {
        String helpText = "Panduan Penggunaan:\n" +
                "1. Masukkan judul buku/artikel (opsional).\n" +
                "2. Masukkan teks yang ingin diringkas di panel kiri atau gunakan Load File / Load Clipboard.\n" +
                "3. Pilih Metode (Rule-based / API-based).\n" +
                "4. Pilih Panjang ringkasan yang diinginkan.\n" +
                "5. Klik tombol RINGKAS.\n" +
                "6. Hasil akan muncul di panel kanan dan dapat disimpan ke TXT atau PDF.";
        JOptionPane.showMessageDialog(this, helpText, "Bantuan", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleSaveTxt() {
        if (currentSummary.isBlank()) {
            showError("Belum ada ringkasan untuk disimpan.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("ringkasan.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                summaryFormatter.exportToTxt(currentSummary, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Ringkasan berhasil disimpan ke TXT.");
                setStatus("Disimpan ke TXT: " + chooser.getSelectedFile().getName());
            } catch (IOException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void handleSavePdf() {
        if (currentSummary.isBlank()) {
            showError("Belum ada ringkasan untuk disimpan.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("ringkasan.pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                summaryFormatter.exportToPdf(currentSummary, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Ringkasan berhasil disimpan ke PDF.");
                setStatus("Disimpan ke PDF: " + chooser.getSelectedFile().getName());
            } catch (IOException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void handleSetApiKey() {
        String input = JOptionPane.showInputDialog(this, "Masukkan API Key:", apiKey);
        if (input != null) {
            apiKey = input.trim();
            JOptionPane.showMessageDialog(this,
                    apiKey.isEmpty() ? "API key dikosongkan." : "API key berhasil disimpan sementara.");
            setStatus(apiKey.isEmpty() ? "API Key dihapus." : "API Key diset.");
        }
    }

    private void handleClear() {
        titleField.setText("");
        inputArea.setText("");
        outputArea.setText("");
        currentSummary = "";
        setStatus("Siap");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        setStatus("Error terjadi.");
    }
    
    private void setStatus(String message) {
        statusLabel.setText("Status: " + message);
    }
}