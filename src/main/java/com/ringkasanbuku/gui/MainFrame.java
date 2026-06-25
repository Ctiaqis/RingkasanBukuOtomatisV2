package com.ringkasanbuku.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.ringkasanbuku.core.Summarizer;
import com.ringkasanbuku.data.HistoryRecord;
import com.ringkasanbuku.data.SummaryHistoryManager;
import com.ringkasanbuku.support.SummarizerFactory;
import com.ringkasanbuku.support.SummaryFormatter;
import com.ringkasanbuku.support.TextInputHandler;
import com.ringkasanbuku.util.ConfigLoader;

public class MainFrame extends JFrame {
    private static final String TITLE_PLACEHOLDER = "Masukkan judul buku atau artikel (opsional)";
    private static final String INPUT_PLACEHOLDER = "Tempel teks buku atau artikel di sini atau gunakan tombol Upload TXT/PDF...";
    private static final String OUTPUT_PLACEHOLDER = "Hasil ringkasan akan muncul di sini setelah tombol RINGKAS ditekan.";

    private final JTextField titleField = new JTextField();
    private final JTextArea inputArea = new JTextArea();
    private final JTextArea outputArea = new JTextArea();
    private final JComboBox<String> methodDropdown = new JComboBox<>(
            new String[] { "Rule-Based - Offline", "API-Based - Online" });
    private final JComboBox<String> lengthDropdown = new JComboBox<>(new String[] { "Biasa", "Medium", "Tinggi" });
    private final JLabel statusLabelLeft = new JLabel("Status: Siap digunakan");
    private final JLabel statusLabelRight = new JLabel("0 karakter input \u2022 0 karakter output");

    private final JButton summarizeButton = new JButton("RINGKAS");
    private final JButton saveTxtButton = new JButton("Simpan TXT");
    private final JButton savePdfButton = new JButton("Simpan PDF");

    private final TextInputHandler textInputHandler;
    private final SummaryFormatter summaryFormatter;
    private final SummaryHistoryManager historyManager;

    private String currentSummary = "";

    private final Color textColor = new Color(50, 50, 50);
    private final Color primaryBlue = new Color(0, 120, 215);

    public MainFrame(TextInputHandler textInputHandler, SummaryFormatter summaryFormatter,
            SummaryHistoryManager historyManager) {
        this.textInputHandler = textInputHandler;
        this.summaryFormatter = summaryFormatter;
        this.historyManager = historyManager;

        setTitle("Aplikasi Ringkasan Buku Otomatis");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Slightly smaller height so text areas don't stretch forever
        setPreferredSize(new Dimension(1050, 680));
        setLayout(new BorderLayout());

        initComponents();
        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        Color bgColor = new Color(245, 245, 245);
        Color cardColor = Color.WHITE;

        Font uiFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font uiFontBold = new Font("Segoe UI", Font.BOLD, 14);
        Font textFont = new Font("Consolas", Font.PLAIN, 14);

        if (!GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()[0].equals("Segoe UI")) {
            uiFont = new Font("SansSerif", Font.PLAIN, 14);
            uiFontBold = new Font("SansSerif", Font.BOLD, 14);
            textFont = new Font("Monospaced", Font.PLAIN, 14);
        }

        getContentPane().setBackground(bgColor);

        // --- TOP PANEL ---
        JPanel topPanel = new JPanel(new BorderLayout(16, 16));
        topPanel.setBackground(bgColor);
        topPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

        JPanel titleFieldPanel = new JPanel(new BorderLayout(12, 0));
        titleFieldPanel.setBackground(bgColor);
        JLabel titleLabel = new JLabel("Judul Buku/Artikel:");
        titleLabel.setFont(uiFontBold);
        titleLabel.setForeground(textColor);

        titleField.setFont(uiFont);
        titleField.setPreferredSize(new Dimension(400, 32));
        titleField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        titleField.setText(TITLE_PLACEHOLDER);
        titleField.setForeground(Color.GRAY);
        titleField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (titleField.getText().equals(TITLE_PLACEHOLDER)) {
                    titleField.setText("");
                    titleField.setForeground(textColor);
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (titleField.getText().isEmpty()) {
                    titleField.setText(TITLE_PLACEHOLDER);
                    titleField.setForeground(Color.GRAY);
                }
            }
        });

        titleFieldPanel.add(titleLabel, BorderLayout.WEST);
        titleFieldPanel.add(titleField, BorderLayout.CENTER);

        JPanel topButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        topButtonsPanel.setBackground(bgColor);
        JButton historyButton = createStyledButton("Riwayat", Color.WHITE, textColor, uiFont);
        JButton helpButton = createStyledButton("Bantuan", Color.WHITE, textColor, uiFont);
        topButtonsPanel.add(historyButton);
        topButtonsPanel.add(helpButton);

        topPanel.add(titleFieldPanel, BorderLayout.CENTER);
        topPanel.add(topButtonsPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- MIDDLE PANEL ---
        inputArea.setFont(textFont);
        outputArea.setFont(textFont);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setEditable(false);

        inputArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        outputArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        inputArea.setText(INPUT_PLACEHOLDER);
        inputArea.setForeground(Color.GRAY);
        inputArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (inputArea.getText().equals(INPUT_PLACEHOLDER)) {
                    inputArea.setText("");
                    inputArea.setForeground(textColor);
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (inputArea.getText().trim().isEmpty()) {
                    inputArea.setText(INPUT_PLACEHOLDER);
                    inputArea.setForeground(Color.GRAY);
                }
            }
        });

        DocumentListener docListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateState();
            }

            public void removeUpdate(DocumentEvent e) {
                updateState();
            }

            public void changedUpdate(DocumentEvent e) {
                updateState();
            }
        };
        inputArea.getDocument().addDocumentListener(docListener);
        outputArea.getDocument().addDocumentListener(docListener);

        outputArea.setText(OUTPUT_PLACEHOLDER);
        outputArea.setForeground(Color.GRAY);

        JPanel middlePanel = new JPanel(new GridLayout(1, 2, 16, 0));
        middlePanel.setBackground(bgColor);
        middlePanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        TitledBorder inputBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true),
                " Input Teks ");
        inputBorder.setTitleFont(uiFontBold);
        inputBorder.setTitleColor(textColor);

        TitledBorder outputBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true),
                " Hasil Ringkasan ");
        outputBorder.setTitleFont(uiFontBold);
        outputBorder.setTitleColor(textColor);

        // Kiri: Input Teks (Card)
        JPanel inputPanel = new JPanel(new BorderLayout(0, 10));
        inputPanel.setBackground(cardColor);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                inputBorder,
                BorderFactory.createEmptyBorder(8, 16, 16, 16)));

        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(BorderFactory.createEmptyBorder());
        inputScroll.setBackground(cardColor);
        inputScroll.setPreferredSize(new Dimension(0, 380)); // Batasi tinggi
        inputPanel.add(inputScroll, BorderLayout.CENTER);

        // Upload TXT, Upload PDF, Bersihkan di bawah input teks (satu baris, sama
        // besar)
        JPanel inputButtonsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        inputButtonsPanel.setBackground(cardColor);
        JButton uploadTxtButton = createStyledButton("Upload TXT", Color.WHITE, textColor, uiFont);
        JButton uploadPdfButton = createStyledButton("Upload PDF", Color.WHITE, textColor, uiFont);
        JButton clearButton = createStyledButton("Bersihkan", Color.WHITE, textColor, uiFont);

        inputButtonsPanel.add(uploadTxtButton);
        inputButtonsPanel.add(uploadPdfButton);
        inputButtonsPanel.add(clearButton);
        inputPanel.add(inputButtonsPanel, BorderLayout.SOUTH);

        // Kanan: Hasil Ringkasan (Card)
        JPanel outputPanel = new JPanel(new BorderLayout(0, 10));
        outputPanel.setBackground(cardColor);
        outputPanel.setBorder(BorderFactory.createCompoundBorder(
                outputBorder,
                BorderFactory.createEmptyBorder(8, 16, 16, 16)));

        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createEmptyBorder());
        outputScroll.setBackground(cardColor);
        outputScroll.setPreferredSize(new Dimension(0, 380)); // Batasi tinggi
        outputPanel.add(outputScroll, BorderLayout.CENTER);

        middlePanel.add(inputPanel);
        middlePanel.add(outputPanel);
        add(middlePanel, BorderLayout.CENTER);

        // --- BAGIAN BAWAH ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(bgColor);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 12, 16));

        methodDropdown.setSelectedItem("Rule-Based - Offline");
        methodDropdown.setFont(uiFont);
        methodDropdown.setBackground(Color.WHITE);

        lengthDropdown.setSelectedItem("Biasa");
        lengthDropdown.setFont(uiFont);
        lengthDropdown.setBackground(Color.WHITE);

        summarizeButton.setFont(uiFontBold);
        summarizeButton.setFocusPainted(false);
        summarizeButton.setMargin(new Insets(4, 16, 4, 16));
        summarizeButton.setEnabled(false);

        saveTxtButton.setFont(uiFont);
        saveTxtButton.setForeground(textColor);
        saveTxtButton.setFocusPainted(false);
        saveTxtButton.setMargin(new Insets(4, 12, 4, 12));
        saveTxtButton.setEnabled(false);

        savePdfButton.setFont(uiFont);
        savePdfButton.setForeground(textColor);
        savePdfButton.setFocusPainted(false);
        savePdfButton.setMargin(new Insets(4, 12, 4, 12));
        savePdfButton.setEnabled(false);

        JLabel methodLabel = new JLabel("Metode Ringkasan:");
        methodLabel.setFont(uiFont);
        JLabel lengthLabel = new JLabel("Panjang Ringkasan:");
        lengthLabel.setFont(uiFont);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setBackground(bgColor);
        leftPanel.add(methodLabel);
        leftPanel.add(methodDropdown);
        leftPanel.add(Box.createHorizontalStrut(5));
        leftPanel.add(lengthLabel);
        leftPanel.add(lengthDropdown);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(bgColor);
        rightPanel.add(summarizeButton);
        rightPanel.add(saveTxtButton);
        rightPanel.add(savePdfButton);

        bottomPanel.add(leftPanel, BorderLayout.WEST);
        bottomPanel.add(rightPanel, BorderLayout.EAST);

        // --- STATUS BAR ---
        statusLabelLeft.setFont(uiFont);
        statusLabelLeft.setForeground(new Color(100, 100, 100));

        statusLabelRight.setFont(uiFont);
        statusLabelRight.setForeground(new Color(100, 100, 100));

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(235, 235, 235));
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 210, 210)),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        statusPanel.add(statusLabelLeft, BorderLayout.WEST);
        statusPanel.add(statusLabelRight, BorderLayout.EAST);

        JPanel footerContainer = new JPanel(new BorderLayout());
        footerContainer.setBackground(bgColor);
        footerContainer.add(bottomPanel, BorderLayout.CENTER);
        footerContainer.add(statusPanel, BorderLayout.SOUTH);

        add(footerContainer, BorderLayout.SOUTH);

        // --- EVENT LISTENERS ---
        uploadTxtButton.addActionListener(e -> handleUploadTxt());
        uploadPdfButton.addActionListener(e -> handleUploadPdf());
        summarizeButton.addActionListener(e -> handleSummarize());
        historyButton.addActionListener(e -> handleShowHistory());
        helpButton.addActionListener(e -> handleShowHelp());
        saveTxtButton.addActionListener(e -> handleSaveTxt());
        savePdfButton.addActionListener(e -> handleSavePdf());
        clearButton.addActionListener(e -> handleClear());

        updateState();
    }

    private JButton createStyledButton(String text, Color bg, Color fg, Font font) {
        JButton btn = new JButton(text);
        btn.setFont(font);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(4, 14, 4, 14));

        return btn;
    }

    private void updateState() {
        int inChars = 0;
        int outChars = 0;
        String rawInput = inputArea.getText();
        String rawOutput = outputArea.getText();

        boolean isPlaceholderInput = rawInput.equals(INPUT_PLACEHOLDER);
        boolean isPlaceholderOutput = rawOutput.equals(OUTPUT_PLACEHOLDER);
        boolean isInputEmpty = rawInput.trim().isEmpty();

        if (!isPlaceholderInput) {
            inChars = rawInput.length();
        }
        if (!isPlaceholderOutput) {
            outChars = rawOutput.length();
        }

        statusLabelRight.setText(inChars + " karakter input \u2022 " + outChars + " karakter output");

        boolean canSummarize = !isPlaceholderInput && !isInputEmpty;
        summarizeButton.setEnabled(canSummarize);

        // Custom coloring for summarize button
        // Kita cukup mengatur enable/disable dan warna foreground gelap
        // agar tombol tidak hilang di LookAndFeel bawaan Windows
        if (canSummarize) {
            summarizeButton.setForeground(Color.BLACK);
        } else {
            summarizeButton.setForeground(new Color(130, 130, 130));
        }
    }

    private void handleUploadTxt() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Text File", "txt"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                inputArea.setText(textInputHandler.loadFromFile(chooser.getSelectedFile()));
                inputArea.setForeground(textColor);
                String fileName = chooser.getSelectedFile().getName();
                if (fileName.toLowerCase().endsWith(".txt"))
                    fileName = fileName.substring(0, fileName.length() - 4);
                titleField.setText(fileName);
                titleField.setForeground(textColor);
                setStatus("File TXT dimuat: " + chooser.getSelectedFile().getName());
                updateState();
            } catch (IOException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void handleUploadPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PDF File", "pdf"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                inputArea.setText(textInputHandler.loadFromFile(chooser.getSelectedFile()));
                inputArea.setForeground(textColor);
                String fileName = chooser.getSelectedFile().getName();
                if (fileName.toLowerCase().endsWith(".pdf"))
                    fileName = fileName.substring(0, fileName.length() - 4);
                titleField.setText(fileName);
                titleField.setForeground(textColor);
                setStatus("File PDF dimuat: " + chooser.getSelectedFile().getName());
                updateState();
            } catch (IOException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void handleSummarize() {
        String inputText = inputArea.getText().trim();
        if (inputText.isEmpty() || inputText.equals(INPUT_PLACEHOLDER)) {
            showError("Silakan masukkan teks terlebih dahulu.");
            return;
        }

        String uiMethod = (String) methodDropdown.getSelectedItem();
        String uiLength = (String) lengthDropdown.getSelectedItem();

        String[] sentences = inputText.split("(?<=[.!?])\\s+");
        int totalSentences = sentences.length;
        if (totalSentences == 0)
            totalSentences = 1;

        int sentenceCount = 1;
        if ("Biasa".equals(uiLength)) {
            sentenceCount = (int) Math.max(1, Math.round(totalSentences * 0.20));
        } else if ("Medium".equals(uiLength)) {
            sentenceCount = (int) Math.max(1, Math.round(totalSentences * 0.35));
        } else if ("Tinggi".equals(uiLength)) {
            sentenceCount = (int) Math.max(1, Math.round(totalSentences * 0.50));
        }
        sentenceCount = Math.min(sentenceCount, totalSentences);

        setStatus("Meringkas...");

        System.out.println("Method dipilih = " + uiMethod);

        String apiKeyFromEnv = ConfigLoader.getOpenRouterApiKey();

        if (apiKeyFromEnv == null) {
            apiKeyFromEnv = "";
        }

        SummarizerFactory factory = new SummarizerFactory();
        Summarizer summarizer;
        String methodUsed = "";
        String summary = "";

        if ("Rule-Based - Offline".equals(uiMethod)) {
            summarizer = factory.create(uiMethod, apiKeyFromEnv);
            try {
                summary = summarizer.summarize(inputText, sentenceCount, uiLength);
                methodUsed = "Rule-Based - Offline";
            } catch (Exception e) {
                showError("Gagal meringkas: " + e.getMessage());
                return;
            }
        } else {
            summarizer = factory.create(uiMethod, apiKeyFromEnv);
            try {
                summary = summarizer.summarize(inputText, sentenceCount, uiLength);
                methodUsed = "API-Based - Online";
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "API-Based gagal digunakan. Sistem akan menggunakan Rule-Based - Offline.", "Info Fallback",
                        JOptionPane.INFORMATION_MESSAGE);
                summarizer = factory.create("Rule-Based - Offline", "");
                try {
                    summary = summarizer.summarize(inputText, sentenceCount, uiLength);
                    methodUsed = "Rule-Based - Offline (Fallback)";
                } catch (Exception ex) {
                    showError("Gagal meringkas: " + ex.getMessage());
                    return;
                }
            }
        }

        currentSummary = summaryFormatter.format(summary);

        outputArea.setText(currentSummary);
        outputArea.setForeground(textColor);

        String title = titleField.getText().trim();
        if (title.equals(TITLE_PLACEHOLDER))
            title = "";
        historyManager.addRecord(title, methodUsed, uiLength, inputText, currentSummary);

        saveTxtButton.setEnabled(true);
        savePdfButton.setEnabled(true);

        setStatus("Selesai meringkas teks.");
        updateState();

        System.out.println("TOKEN = " + ConfigLoader.getOpenRouterApiKey());
    }

    private void handleShowHistory() {
        List<HistoryRecord> history = historyManager.getHistory();
        if (history.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Belum ada riwayat ringkasan.");
            return;
        }

        JDialog dialog = new JDialog(this, "Riwayat Ringkasan", true);
        dialog.setSize(650, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        DefaultListModel<HistoryRecord> listModel = new DefaultListModel<>();
        // Tampilkan urutan dari yang terbaru
        for (int i = history.size() - 1; i >= 0; i--) {
            listModel.addElement(history.get(i));
        }

        JList<HistoryRecord> list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof HistoryRecord) {
                    HistoryRecord hr = (HistoryRecord) value;
                    String titleDisp = (hr.getTitle() != null && !hr.getTitle().isEmpty()) ? hr.getTitle()
                            : "Tanpa Judul";
                    setText(titleDisp + " | " + hr.getMethod() + " | " + hr.getSummaryLength() + " | "
                            + hr.getTimestamp());
                }
                return this;
            }
        });

        JTextArea inputPreviewArea = new JTextArea();
        inputPreviewArea.setEditable(false);
        inputPreviewArea.setLineWrap(true);
        inputPreviewArea.setWrapStyleWord(true);
        inputPreviewArea.setFont(new Font("Consolas", Font.PLAIN, 14));

        JTextArea summaryPreviewArea = new JTextArea();
        summaryPreviewArea.setEditable(false);
        summaryPreviewArea.setLineWrap(true);
        summaryPreviewArea.setWrapStyleWord(true);
        summaryPreviewArea.setFont(new Font("Consolas", Font.PLAIN, 14));

        JScrollPane inputScroll = new JScrollPane(inputPreviewArea);
        inputScroll.setBorder(BorderFactory.createTitledBorder("Input Teks"));

        JScrollPane summaryScroll = new JScrollPane(summaryPreviewArea);
        summaryScroll.setBorder(BorderFactory.createTitledBorder("Hasil Ringkasan"));

        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputScroll, summaryScroll);
        rightSplitPane.setDividerLocation(150);
        // Make it resizable
        rightSplitPane.setResizeWeight(0.5);

        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                HistoryRecord selected = list.getSelectedValue();
                if (selected != null) {
                    String input = selected.getInput() != null && !selected.getInput().trim().isEmpty()
                            ? selected.getInput()
                            : "(Tidak ada teks input)";
                    inputPreviewArea.setText(input);
                    inputPreviewArea.setCaretPosition(0);

                    summaryPreviewArea.setText(selected.getSummary());
                    summaryPreviewArea.setCaretPosition(0);
                }
            }
        });

        if (!listModel.isEmpty()) {
            list.setSelectedIndex(0);
        }

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(list), rightSplitPane);
        splitPane.setDividerLocation(200);
        dialog.add(splitPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton clearHistoryButton = new JButton("Bersihkan Riwayat");

        clearHistoryButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Apakah Anda yakin ingin menghapus semua riwayat ringkasan?",
                    "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                historyManager.clearHistory();
                listModel.clear();
                inputPreviewArea.setText("");
                summaryPreviewArea.setText("");
                JOptionPane.showMessageDialog(dialog, "Seluruh riwayat berhasil dibersihkan.");
            }
        });

        buttonPanel.add(clearHistoryButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void handleShowHelp() {
        String helpText = "Panduan Penggunaan:\n" +
                "1. Masukkan judul buku/artikel (opsional).\n" +
                "2. Masukkan teks yang ingin diringkas di panel kiri atau gunakan Upload TXT / PDF.\n" +
                "3. Pilih Pilihan Ringkasan dan Panjang.\n" +
                "4. Klik tombol RINGKAS.\n" +
                "5. Hasil akan muncul di panel kanan dan dapat disimpan ke TXT atau PDF.";
        JOptionPane.showMessageDialog(this, helpText, "Bantuan", JOptionPane.INFORMATION_MESSAGE);
    }

    private String generateFormattedExportContent() {
        String title = titleField.getText().trim();
        if (title.isEmpty() || title.equals(TITLE_PLACEHOLDER)) {
            title = "Tidak Ada Judul";
        }
        
        String method = (String) methodDropdown.getSelectedItem();
        String length = (String) lengthDropdown.getSelectedItem();
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm");
        String dateStr = dtf.format(now);
        
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================\n");
        sb.append("                RINGKASAN BUKU / ARTIKEL                \n");
        sb.append("========================================================\n\n");
        sb.append("Judul    : ").append(title).append("\n");
        sb.append("Metode   : ").append(method).append("\n");
        sb.append("Panjang  : ").append(length).append("\n");
        sb.append("Tanggal  : ").append(dateStr).append("\n\n");
        sb.append("--------------------------------------------------------\n");
        sb.append("ISI RINGKASAN:\n\n");
        sb.append(currentSummary).append("\n\n");
        sb.append("========================================================\n");
        sb.append("      Dihasilkan oleh Aplikasi Ringkasan Otomatis       \n");
        sb.append("========================================================\n");
        
        return sb.toString();
    }

    private void handleSaveTxt() {
        if (currentSummary.isBlank()) {
            showError("Belum ada ringkasan untuk disimpan.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(generateExportFileName("txt")));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String contentToSave = generateFormattedExportContent();
                summaryFormatter.exportToTxt(contentToSave, chooser.getSelectedFile());
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
        chooser.setSelectedFile(new File(generateExportFileName("pdf")));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String contentToSave = generateFormattedExportContent();
                summaryFormatter.exportToPdf(contentToSave, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Ringkasan berhasil disimpan ke PDF.");
                setStatus("Disimpan ke PDF: " + chooser.getSelectedFile().getName());
            } catch (IOException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private String generateExportFileName(String extension) {
        String title = titleField.getText().trim();
        if (title.isEmpty() || title.equals(TITLE_PLACEHOLDER)) {
            title = "Ringkasan_Buku";
        }

        String method = (String) methodDropdown.getSelectedItem();
        if (method != null) {
            method = method.contains("Rule-Based") ? "RuleBased" : "APIBased";
        } else {
            method = "RuleBased";
        }

        String length = (String) lengthDropdown.getSelectedItem();
        if (length == null) {
            length = "Medium";
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
        String dateTime = dtf.format(now);

        String rawName = title + "_" + method + "_" + length + "_" + dateTime;
        String safeName = rawName.replaceAll("[^a-zA-Z0-9\\-_]", "_").replaceAll("_+", "_");

        return safeName + "." + extension;
    }

    private void handleClear() {
        titleField.setText(TITLE_PLACEHOLDER);
        titleField.setForeground(Color.GRAY);

        inputArea.setText(INPUT_PLACEHOLDER);
        inputArea.setForeground(Color.GRAY);

        outputArea.setText(OUTPUT_PLACEHOLDER);
        outputArea.setForeground(Color.GRAY);

        currentSummary = "";

        saveTxtButton.setEnabled(false);
        savePdfButton.setEnabled(false);

        setStatus("Siap digunakan");
        updateState();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        setStatus("Error terjadi.");
    }

    private void setStatus(String message) {
        statusLabelLeft.setText("Status: " + message);
    }
}