package com.ringkasanbuku.app;

import javax.swing.SwingUtilities;

import com.ringkasanbuku.data.SummaryHistoryManager;
import com.ringkasanbuku.gui.MainFrame;
import com.ringkasanbuku.support.SummaryFormatter;
import com.ringkasanbuku.support.TextInputHandler;


public class MainApp {
    private MainFrame mainFrame;

    public void initialize() {
        TextInputHandler textInputHandler = new TextInputHandler();
        SummaryFormatter summaryFormatter = new SummaryFormatter();
        SummaryHistoryManager historyManager = new SummaryHistoryManager();
        mainFrame = new MainFrame(
                textInputHandler,
                summaryFormatter,
                historyManager);
    }

    public void run() {
        if (mainFrame == null) {
            initialize();
        }
        mainFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // ignore
            }
            MainApp app = new MainApp();
            app.initialize();
            app.run();
        });
    }
}