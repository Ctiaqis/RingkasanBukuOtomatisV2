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
    private final JLabel statusLabel = new JLabel(" Status: Siap");

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
        setLayout(new BorderLayout(10, 10));

        initComponents();
        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        // --- BAGIAN ATAS ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        // Title dan Tombol Kanan Atas
        JPanel titleAndButtonsPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Aplikasi Ringkasan Buku Otomatis");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        
        JPanel topButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JButton historyButton = new JButton("Riwayat");
        JButton helpButton = new JButton("Bantuan");
        topButtonsPanel.add(historyButton);
        topButtonsPanel.add(helpButton);

        titleAndButtonsPanel.add(titleLabel, BorderLayout.WEST);
        titleAndButtonsPanel.add(topButtonsPanel, BorderLayout.EAST);

        // Field Judul Buku/Artikel
        JPanel titleFieldPanel = new JPanel(new BorderLayout(5, 5));
        titleFieldPanel.add(new JLabel("Judul Buku/Artikel:"), BorderLayout.WEST);
        titleFieldPanel.add(titleField, BorderLayout.CENTER);

        topPanel.add(titleAndButtonsPanel, BorderLayout.NORTH);
        topPanel.add(titleFieldPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // --- BAGIAN TENGAH ---
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setEditable(false);

        JPanel middlePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        middlePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // Kiri: Input Teks
        JPanel inputPanel = new JPanel(new BorderLayout(0, 5));
        JPanel inputHeaderPanel = new JPanel(new BorderLayout());
        inputHeaderPanel.add(new JLabel("Input Teks:"), BorderLayout.WEST);
        
        JPanel inputButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JButton loadFileButton = new JButton("Load File");
        JButton clipboardButton = new JButton("Load Clipboard");
        JButton clearButton = new JButton("Bersihkan");
        inputButtonsPanel.add(loadFileButton);
        inputButtonsPanel.add(clipboardButton);
        inputButtonsPanel.add(clearButton);
        inputHeaderPanel.add(inputButtonsPanel, BorderLayout.EAST);

        inputPanel.add(inputHeaderPanel, BorderLayout.NORTH);
        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);

        // Kanan: Hasil Ringkasan
        JPanel outputPanel = new JPanel(new BorderLayout(0, 5));
        outputPanel.add(new JLabel("Hasil Ringkasan:"), BorderLayout.NORTH);
        outputPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        middlePanel.add(inputPanel);
        middlePanel.add(outputPanel);
        add(middlePanel, BorderLayout.CENTER);

        // --- BAGIAN BAWAH ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton summarizeButton = new JButton("RINGKAS");
        summarizeButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        JButton saveTxtButton = new JButton("Simpan TXT");
        JButton savePdfButton = new JButton("Simpan PDF");
        JButton setApiKeyButton = new JButton("Set API Key");

        controlPanel.add(new JLabel("Metode:"));
        controlPanel.add(methodDropdown);
        controlPanel.add(new JLabel("Panjang:"));
        controlPanel.add(lengthDropdown);
        controlPanel.add(summarizeButton);
        controlPanel.add(saveTxtButton);
        controlPanel.add(savePdfButton);
        controlPanel.add(setApiKeyButton);

        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());

        bottomPanel.add(controlPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

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
        statusLabel.setText(" Status: " + message);
    }
}