package com.freelance.ui;

import javax.swing.*;
import java.awt.*;

/**
 * MainFrame — the application window with CardLayout for screen navigation.
 * Modern light background with frosted navbar.
 */
public class MainFrame extends JFrame {

    public static final String LOGIN        = "LOGIN";
    public static final String REGISTER     = "REGISTER";
    public static final String DASHBOARD    = "DASHBOARD";
    public static final String BROWSE_JOBS  = "BROWSE_JOBS";
    public static final String JOB_DETAIL   = "JOB_DETAIL";
    public static final String POST_JOB     = "POST_JOB";
    public static final String MY_PROPOSALS = "MY_PROPOSALS";
    public static final String MY_CONTRACTS = "MY_CONTRACTS";
    public static final String PROFILE      = "PROFILE";

    private static MainFrame instance;

    private final CardLayout  cardLayout = new CardLayout();
    private final JPanel      cardPanel  = new JPanel(cardLayout);

    private NavBar navBar;

    public static MainFrame get() { return instance; }

    public MainFrame() {
        instance = this;
        setTitle("Freelance .");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1360, 860);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);

        // System look and feel as base, then override
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        // Global Swing defaults — canvas styling
        UIManager.put("Panel.background",     Theme.BG);
        UIManager.put("ScrollPane.border",    BorderFactory.createEmptyBorder());
        UIManager.put("TextField.background", Theme.SURFACE);
        UIManager.put("TextArea.background",  Theme.SURFACE);
        UIManager.put("ComboBox.background",  Theme.SURFACE);
        UIManager.put("OptionPane.background", Theme.BG);
        UIManager.put("Panel.background",     Theme.BG);

        // Enable anti-aliased text globally
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        navBar = new NavBar();
        add(navBar, BorderLayout.NORTH);

        cardPanel.setBackground(Theme.BG);
        add(cardPanel, BorderLayout.CENTER);

        // Pre-build auth screens
        cardPanel.add(new LoginPanel(),    LOGIN);
        cardPanel.add(new RegisterPanel(), REGISTER);

        // Placeholders for protected screens
        cardPanel.add(new JPanel(), DASHBOARD);
        cardPanel.add(new JPanel(), BROWSE_JOBS);
        cardPanel.add(new JPanel(), JOB_DETAIL);
        cardPanel.add(new JPanel(), POST_JOB);
        cardPanel.add(new JPanel(), MY_PROPOSALS);
        cardPanel.add(new JPanel(), MY_CONTRACTS);
        cardPanel.add(new JPanel(), PROFILE);

        show(LOGIN);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    public void show(String card) { show(card, -1); }

    public void show(String card, long param) {
        switch (card) {
            case DASHBOARD    -> replace(card, new DashboardPanel());
            case BROWSE_JOBS  -> replace(card, new BrowseJobsPanel());
            case JOB_DETAIL   -> replace(card, new JobDetailPanel(param));
            case POST_JOB     -> replace(card, new PostJobPanel());
            case MY_PROPOSALS -> replace(card, new MyProposalsPanel());
            case MY_CONTRACTS -> replace(card, new MyContractsPanel());
            case PROFILE      -> replace(card, new ProfilePanel());
            default           -> {}
        }
        cardLayout.show(cardPanel, card);
        navBar.refresh();
    }

    private void replace(String key, JPanel panel) {
        cardPanel.add(panel, key);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
