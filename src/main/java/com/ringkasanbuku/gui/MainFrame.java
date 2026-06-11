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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainFrame extends JFrame {
    private final JTextArea inputArea = new JTextArea();
    private final JTextArea outputArea = new JTextArea();
    private final JComboBox<String> methodDropdown = new JComboBox<>(new String[]{"Rule-based", "API-based"});

    private final TextInputHandler textInputHandler;
    private final SummaryFormatter summaryFormatter;
    private final SummaryHistoryManager historyManager;
    private final ConnectivityChecker connectivityChecker;
    private final TokenValidator tokenValidator;

    private String currentSummary = "";
    private String apiKey = "";

    public MainFrame(TextInputHandler textInputHandler,
                     SummaryFormatter summaryFormatter,
                     SummaryHistoryManager historyManager,
                     ConnectivityChecker connectivityChecker,
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
        JLabel title = new JLabel("Aplikasi Ringkasan Buku Otomatis", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        add(title, BorderLayout.NORTH);

        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setEditable(false);

        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(BorderFactory.createTitledBorder("Input Teks"));
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Output Ringkasan"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputScroll, outputScroll);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        JPanel controls = new JPanel(new BorderLayout(10, 10));
        controls.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JPanel topButtons = new JPanel(new GridLayout(2, 4, 8, 8));
        JButton loadFileButton = new JButton("Load File");
        JButton clipboardButton = new JButton("Load Clipboard");
        JButton summarizeButton = new JButton("Ringkas");
        JButton historyButton = new JButton("Riwayat");
        JButton saveTxtButton = new JButton("Simpan TXT");
        JButton savePdfButton = new JButton("Simpan PDF");
        JButton setApiKeyButton = new JButton("Set API Key");
        JButton clearButton = new JButton("Bersihkan");

        topButtons.add(loadFileButton);
        topButtons.add(clipboardButton);
        topButtons.add(summarizeButton);
        topButtons.add(historyButton);
        topButtons.add(saveTxtButton);
        topButtons.add(savePdfButton);
        topButtons.add(setApiKeyButton);
        topButtons.add(clearButton);

        JPanel methodPanel = new JPanel(new BorderLayout(8, 0));
        methodPanel.setBorder(BorderFactory.createTitledBorder("Metode Ringkasan"));
        methodPanel.add(new JLabel("Pilih metode:"), BorderLayout.WEST);
        methodPanel.add(methodDropdown, BorderLayout.CENTER);

        controls.add(topButtons, BorderLayout.CENTER);
        controls.add(methodPanel, BorderLayout.SOUTH);
        add(controls, BorderLayout.SOUTH);

        loadFileButton.addActionListener(e -> handleLoadFile());
        clipboardButton.addActionListener(e -> handleLoadClipboard());
        summarizeButton.addActionListener(e -> handleSummarize());
        historyButton.addActionListener(e -> handleShowHistory());
        saveTxtButton.addActionListener(e -> handleSaveTxt());
        savePdfButton.addActionListener(e -> handleSavePdf());
        setApiKeyButton.addActionListener(e -> handleSetApiKey());
        clearButton.addActionListener(e -> handleClear());
    }

    private void handleLoadFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Text and PDF", "txt", "pdf"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                String text = textInputHandler.loadFromFile(chooser.getSelectedFile());
                inputArea.setText(text);
            } catch (IOException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void handleLoadClipboard() {
        try {
            String text = textInputHandler.loadFromClipboard();
            inputArea.setText(text);
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

        SummarizerFactory factory = new SummarizerFactory(method, connectivityChecker, tokenValidator);
        Summarizer summarizer = factory.create(apiKey);

        if ("API-based".equals(method) && !(summarizer instanceof ApiBasedSummarizer)) {
            JOptionPane.showMessageDialog(this,
                    "API-based tidak dapat digunakan. Sistem otomatis menggunakan Rule-based.",
                    "Info Fallback",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        String summary = summarizer.summarize(inputText);
        currentSummary = summaryFormatter.format(summary);
        outputArea.setText(currentSummary);
        historyManager.addRecord(currentSummary);
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
        JOptionPane.showMessageDialog(this, new JScrollPane(historyArea), "Riwayat Ringkasan", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleSaveTxt() {
        if (currentSummary.isBlank()) {
            showError("Belum ada ringkasan untuk disimpan.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("ringkasan.txt"));
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                summaryFormatter.exportToTxt(currentSummary, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Ringkasan berhasil disimpan ke TXT.");
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
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                summaryFormatter.exportToPdf(currentSummary, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Ringkasan berhasil disimpan ke PDF.");
            } catch (IOException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void handleSetApiKey() {
        String input = JOptionPane.showInputDialog(this, "Masukkan API Key:", apiKey);
        if (input != null) {
            apiKey = input.trim();
            if (apiKey.isEmpty()) {
                JOptionPane.showMessageDialog(this, "API key dikosongkan.");
            } else {
                JOptionPane.showMessageDialog(this, "API key berhasil disimpan sementara.");
            }
        }
    }

    private void handleClear() {
        inputArea.setText("");
        outputArea.setText("");
        currentSummary = "";
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
