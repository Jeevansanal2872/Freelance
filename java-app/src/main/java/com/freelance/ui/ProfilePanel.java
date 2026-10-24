package com.freelance.ui;

import javax.swing.*;
import java.awt.*;

/**
 * ProfilePanel — Modern profile management workspace.
 * Displays animated profile completion bar, personal & professional details, and links.
 */
public class ProfilePanel extends JPanel {

    private final JTextField firstNameField = new JTextField(16);
    private final JTextField lastNameField = new JTextField(16);
    private final JTextField hourlyRateField = new JTextField(10);
    private final JTextArea bioArea = new JTextArea(4, 20);
    private final JTextField skillsField = new JTextField(24);
    private final JTextField portfolioField = new JTextField(20);
    private final JTextField educationField = new JTextField(20);
    private final JTextField languagesField = new JTextField(20);
    private final JTextField hoursField = new JTextField(10);
    private final JTextField githubField = new JTextField(20);
    private final JTextField linkedinField = new JTextField(20);

    private final JLabel completionLabel = Theme.label("0%", Theme.FONT_BOLD, Theme.ACCENT);
    private int completionPercent = 0;
    private final JLabel feedbackLabel = Theme.label("", Theme.FONT_SMALL, Theme.GREEN_TEXT);
    private Theme.AnimatedProgressBar progressBar;

    private JPanel avatarPanel;
    private JLabel nameHeader;
    private JLabel roleHeader;

    public ProfilePanel() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        buildUI();
        loadProfileAsync();
    }

    private void buildUI() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setOpaque(false);
        page.setBorder(BorderFactory.createEmptyBorder(32, 48, 48, 48));

        // ── Top Profile Header Card ───────────────────────────────────────────
        Theme.GlassPanel headerCard = Theme.card();
        headerCard.setLayout(new BorderLayout(24, 16));
        headerCard.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        headerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        headerCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        leftGroup.setOpaque(false);

        String initials = Session.userName != null && !Session.userName.isBlank()
            ? (Session.userName.length() >= 2 ? Session.userName.substring(0, 2).toUpperCase() : Session.userName.toUpperCase())
            : "ME";
        avatarPanel = Theme.avatar(initials, 72, Theme.ACCENT, Theme.WHITE);
        leftGroup.add(avatarPanel);

        JPanel nameCol = new JPanel();
        nameCol.setLayout(new BoxLayout(nameCol, BoxLayout.Y_AXIS));
        nameCol.setOpaque(false);

        nameHeader = Theme.label(Session.userName != null && !Session.userName.isEmpty() ? Session.userName : "My Account", Theme.FONT_HERO, Theme.NEAR_BLACK);
        nameCol.add(nameHeader);
        nameCol.add(Box.createVerticalStrut(4));

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        badgeRow.setOpaque(false);
        roleHeader = Theme.badge(Session.role != null ? Session.role : "FREELANCER");
        badgeRow.add(roleHeader);
        badgeRow.add(Theme.label("• " + Session.userEmail, Theme.FONT_SMALL, Theme.MID_GRAY));
        nameCol.add(badgeRow);

        leftGroup.add(nameCol);
        headerCard.add(leftGroup, BorderLayout.WEST);

        // Right side: Profile Completion Animated Progress Bar
        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);
        rightCol.setPreferredSize(new Dimension(220, 0));

        JPanel compTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        compTop.setOpaque(false);
        compTop.add(Theme.label("Profile Strength:", Theme.FONT_SMALL, Theme.MID_GRAY));
        compTop.add(completionLabel);
        rightCol.add(compTop);
        rightCol.add(Box.createVerticalStrut(8));

        progressBar = new Theme.AnimatedProgressBar(10, Theme.ACCENT, Theme.SEPARATOR);
        progressBar.setPreferredSize(new Dimension(200, 10));
        progressBar.setMaximumSize(new Dimension(200, 10));
        rightCol.add(progressBar);

        headerCard.add(rightCol, BorderLayout.EAST);
        page.add(headerCard);
        page.add(Box.createVerticalStrut(24));

        // ── Form Cards Grid ───────────────────────────────────────────────────
        JPanel cardsGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        cardsGrid.setOpaque(false);
        cardsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 460));
        cardsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Column 1: Personal & Bio
        Theme.GlassPanel p1 = Theme.card();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        p1.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel sec1 = Theme.label("Personal Identity", Theme.FONT_H3, Theme.NEAR_BLACK);
        p1.add(sec1);
        p1.add(Box.createVerticalStrut(14));

        JPanel nameGrid = new JPanel(new GridLayout(1, 2, 12, 0));
        nameGrid.setOpaque(false);
        Theme.styleInput(firstNameField);
        Theme.styleInput(lastNameField);
        nameGrid.add(createField("First Name", firstNameField));
        nameGrid.add(createField("Last Name", lastNameField));
        p1.add(nameGrid);
        p1.add(Box.createVerticalStrut(12));

        Theme.styleInput(hourlyRateField);
        p1.add(createField("Hourly Rate ($ USD)", hourlyRateField));
        p1.add(Box.createVerticalStrut(12));

        bioArea.setFont(Theme.FONT_BODY);
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        bioArea.setBorder(BorderFactory.createCompoundBorder(
            new Theme.RoundBorder(Theme.LIGHT_GRAY, Theme.RADIUS_SM, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        p1.add(createField("Professional Bio", new JScrollPane(bioArea)));

        cardsGrid.add(p1);

        // Column 2: Professional & Links
        Theme.GlassPanel p2 = Theme.card();
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        p2.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel sec2 = Theme.label("Skills & Links", Theme.FONT_H3, Theme.NEAR_BLACK);
        p2.add(sec2);
        p2.add(Box.createVerticalStrut(14));

        Theme.styleInput(skillsField);
        Theme.styleInput(portfolioField);
        Theme.styleInput(educationField);
        Theme.styleInput(languagesField);
        Theme.styleInput(githubField);

        p2.add(createField("Primary Skills (comma-separated)", skillsField));
        p2.add(Box.createVerticalStrut(10));
        p2.add(createField("Portfolio Website URL", portfolioField));
        p2.add(Box.createVerticalStrut(10));

        JPanel rowEdu = new JPanel(new GridLayout(1, 2, 12, 0));
        rowEdu.setOpaque(false);
        rowEdu.add(createField("Education / Degrees", educationField));
        rowEdu.add(createField("Languages Spoken", languagesField));
        p2.add(rowEdu);
        p2.add(Box.createVerticalStrut(10));

        p2.add(createField("GitHub Profile URL", githubField));

        cardsGrid.add(p2);
        page.add(cardsGrid);
        page.add(Box.createVerticalStrut(24));

        // ── Action Bar ────────────────────────────────────────────────────────
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionRow.setOpaque(false);
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        feedbackLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        actionRow.add(feedbackLabel);

        Theme.FlatButton saveBtn = Theme.btnPrimary("Save Changes →");
        saveBtn.addActionListener(e -> saveProfile(saveBtn));
        actionRow.add(saveBtn);

        page.add(actionRow);
        add(Theme.scroll(page), BorderLayout.CENTER);
    }

    private JPanel createField(String labelText, JComponent input) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel lbl = Theme.label(labelText, Theme.FONT_SMALL, Theme.MID_GRAY);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 2, 4, 0));
        p.add(lbl);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, input instanceof JScrollPane ? 90 : 40));
        p.add(input);
        return p;
    }

    private void loadProfileAsync() {
        if (!Session.isLoggedIn()) return;

        SwingWorker<String, Void> sw = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return ApiClient.get("/api/users/" + Session.userId);
            }

            @Override
            protected void done() {
                try {
                    String json = get();
                    firstNameField.setText(ApiClient.jsonStr(json, "firstName"));
                    lastNameField.setText(ApiClient.jsonStr(json, "lastName"));
                    bioArea.setText(ApiClient.jsonStr(json, "bio"));
                    skillsField.setText(ApiClient.jsonStr(json, "skills"));
                    portfolioField.setText(ApiClient.jsonStr(json, "portfolioUrl"));
                    educationField.setText(ApiClient.jsonStr(json, "education"));
                    languagesField.setText(ApiClient.jsonStr(json, "languages"));
                    githubField.setText(ApiClient.jsonStr(json, "githubUrl"));

                    double rate = ApiClient.jsonDouble(json, "hourlyRate");
                    if (rate > 0) hourlyRateField.setText(String.valueOf(rate));

                    String pc = ApiClient.jsonStr(json, "profileCompletion");
                    if (pc != null) {
                        completionPercent = Integer.parseInt(pc);
                        completionLabel.setText(completionPercent + "%");
                        progressBar.setProgressAnimated(completionPercent);
                    }
                } catch (Exception ignored) {}
            }
        };
        sw.execute();
    }

    private void saveProfile(JButton btn) {
        if (!Session.isLoggedIn()) return;

        btn.setEnabled(false);
        btn.setText("Saving...");
        feedbackLabel.setText("");

        Double hourlyRate = null;
        try {
            String r = hourlyRateField.getText().trim();
            if (!r.isEmpty()) hourlyRate = Double.parseDouble(r);
        } catch (Exception ignored) {}

        Double finalHourlyRate = hourlyRate;

        SwingWorker<String, Void> sw = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                String json = "{\"firstName\":" + ApiClient.esc(firstNameField.getText().trim()) +
                              ",\"lastName\":" + ApiClient.esc(lastNameField.getText().trim()) +
                              ",\"bio\":" + ApiClient.esc(bioArea.getText().trim()) +
                              ",\"skills\":" + ApiClient.esc(skillsField.getText().trim()) +
                              ",\"portfolioUrl\":" + ApiClient.esc(portfolioField.getText().trim()) +
                              ",\"education\":" + ApiClient.esc(educationField.getText().trim()) +
                              ",\"languages\":" + ApiClient.esc(languagesField.getText().trim()) +
                              ",\"githubUrl\":" + ApiClient.esc(githubField.getText().trim()) +
                              (finalHourlyRate != null ? ",\"hourlyRate\":" + finalHourlyRate : "") + "}";
                return ApiClient.put("/api/users/" + Session.userId, json);
            }

            @Override
            protected void done() {
                btn.setEnabled(true);
                btn.setText("Save Changes →");
                try {
                    String resp = get();
                    feedbackLabel.setForeground(Theme.GREEN_TEXT);
                    feedbackLabel.setText("✓ Profile updated successfully.");

                    String fn = firstNameField.getText().trim();
                    String ln = lastNameField.getText().trim();
                    if (!fn.isEmpty()) {
                        String fullName = fn + (ln.isEmpty() ? "" : " " + ln);
                        Session.userName = fullName;
                        nameHeader.setText(fullName);
                    }

                    String pc = ApiClient.jsonStr(resp, "profileCompletion");
                    if (pc != null) {
                        completionPercent = Integer.parseInt(pc);
                        completionLabel.setText(completionPercent + "%");
                        progressBar.setProgressAnimated(completionPercent);
                    }
                    revalidate();
                    repaint();
                } catch (Exception ex) {
                    feedbackLabel.setForeground(Theme.RED);
                    feedbackLabel.setText("Failed to save: " + ex.getMessage());
                }
            }
        };
        sw.execute();
    }
}
