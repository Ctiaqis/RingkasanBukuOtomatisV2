package com.ringkasanbuku.app;

import com.ringkasanbuku.data.SummaryHistoryManager;
import com.ringkasanbuku.gui.MainFrame;
import com.ringkasanbuku.support.SummaryFormatter;
import com.ringkasanbuku.support.TextInputHandler;
import com.ringkasanbuku.util.ConnectivityChecker;
import com.ringkasanbuku.util.TokenValidator;
import javax.swing.SwingUtilities;

public class MainApp {
    private MainFrame mainFrame;

    public void initialize() {
        TextInputHandler textInputHandler = new TextInputHandler();
        SummaryFormatter summaryFormatter = new SummaryFormatter();
        SummaryHistoryManager historyManager = new SummaryHistoryManager();
        ConnectivityChecker connectivityChecker = new ConnectivityChecker();
        TokenValidator tokenValidator = new TokenValidator();

        mainFrame = new MainFrame(
                textInputHandler,
                summaryFormatter,
                historyManager,
                connectivityChecker,
                tokenValidator);
    }

    public void run() {
        if (mainFrame == null) {
            initialize();
        }
        mainFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainApp app = new MainApp();
            app.initialize();
            app.run();
        });
    }
}