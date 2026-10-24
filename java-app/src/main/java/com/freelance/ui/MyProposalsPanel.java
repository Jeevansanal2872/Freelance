package com.freelance.ui;

import javax.swing.*;
import java.awt.*;

/**
 * MyProposalsPanel — Freelancer proposal pipeline tracker.
 * Displays submitted proposals, live statuses, bid amounts, and withdrawal options.
 */
public class MyProposalsPanel extends JPanel {

    private final JPanel listContainer = new JPanel();
    private final JLabel statsLabel = Theme.label("", Theme.FONT_SMALL, Theme.MID_GRAY);

    public MyProposalsPanel() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        buildUI();
        loadProposalsAsync();
    }

    private void buildUI() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setOpaque(false);
        page.setBorder(BorderFactory.createEmptyBorder(32, 48, 48, 48));

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel eyebrow = Theme.label("FREELANCER WORKSPACE", Theme.FONT_TINY, Theme.ACCENT);
        JLabel title = Theme.label("Your proposal pipeline.", Theme.FONT_HERO, Theme.NEAR_BLACK);
        JLabel sub = Theme.label(
            "Track active pitches, client reviews, and incoming contract invitations.",
            Theme.FONT_BODY, Theme.MID_GRAY
        );

        header.add(eyebrow);
        header.add(Box.createVerticalStrut(4));
        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);
        header.add(Box.createVerticalStrut(6));
        header.add(statsLabel);
        header.add(Box.createVerticalStrut(24));
        page.add(header);

        // List Container
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);
        listContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(listContainer);

        add(Theme.scroll(page), BorderLayout.CENTER);
    }

    private void loadProposalsAsync() {
        if (!Session.isLoggedIn()) {
            listContainer.add(Theme.label("Please sign in to view your proposals.", Theme.FONT_BODY, Theme.MID_GRAY));
            return;
        }

        listContainer.removeAll();
        JLabel loading = Theme.label("Loading your active proposals...", Theme.FONT_BODY, Theme.MID_GRAY);
        listContainer.add(loading);
        listContainer.revalidate();
        listContainer.repaint();

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return ApiClient.get("/api/proposals/my/" + Session.userId);
            }

            @Override
            protected void done() {
                listContainer.removeAll();
                try {
                    String[] items = ApiClient.jsonArray(get());
                    statsLabel.setText(items.length + " total proposals submitted");

                    if (items.length == 0) {
                        Theme.GlassPanel emptyCard = Theme.card();
                        emptyCard.setBorder(BorderFactory.createEmptyBorder(36, 36, 36, 36));
                        emptyCard.setLayout(new BoxLayout(emptyCard, BoxLayout.Y_AXIS));

                        JLabel noProp = Theme.label("You have not submitted any proposals yet.", Theme.FONT_H3, Theme.NEAR_BLACK);
                        JLabel hint = Theme.label("Explore open opportunities and submit your first pitch today.", Theme.FONT_BODY, Theme.MID_GRAY);
                        Theme.FlatButton browseBtn = Theme.btnPrimary("Browse Marketplace →");
                        browseBtn.addActionListener(e -> MainFrame.get().show(MainFrame.BROWSE_JOBS));

                        emptyCard.add(noProp);
                        emptyCard.add(Box.createVerticalStrut(8));
                        emptyCard.add(hint);
                        emptyCard.add(Box.createVerticalStrut(18));
                        emptyCard.add(browseBtn);
                        listContainer.add(emptyCard);
                    } else {
                        for (String pJson : items) {
                            listContainer.add(createProposalCard(pJson));
                            listContainer.add(Box.createVerticalStrut(16));
                        }
                    }
                } catch (Exception ex) {
                    listContainer.add(Theme.label("Failed to load proposals: " + ex.getMessage(), Theme.FONT_BODY, Theme.RED));
                }
                listContainer.revalidate();
                listContainer.repaint();
            }
        };
        worker.execute();
    }

    private JPanel createProposalCard(String json) {
        long pId = ApiClient.jsonLong(json, "id");
        long jobId = ApiClient.jsonLong(json, "jobId");
        double bid = ApiClient.jsonDouble(json, "bidAmount");
        long days = ApiClient.jsonLong(json, "estimatedDays");
        String letter = ApiClient.jsonStr(json, "coverLetter");
        String status = ApiClient.jsonStr(json, "status");
        String createdAt = ApiClient.jsonStr(json, "createdAt");

        Theme.GlassPanel card = Theme.card();
        card.setLayout(new BorderLayout(20, 16));
        card.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left Info
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.setOpaque(false);
        top.add(Theme.badge(status != null ? status : "PENDING"));
        top.add(Theme.label("Job #" + jobId, Theme.FONT_BOLD, Theme.NEAR_BLACK));
        if (createdAt != null) {
            String dateOnly = createdAt.contains("T") ? createdAt.substring(0, createdAt.indexOf("T")) : createdAt;
            top.add(Theme.label("• Submitted " + dateOnly, Theme.FONT_TINY, Theme.MID_GRAY));
        }
        left.add(top);
        left.add(Box.createVerticalStrut(8));

        JTextArea letterArea = new JTextArea(letter != null ? letter : "");
        letterArea.setFont(Theme.FONT_BODY);
        letterArea.setForeground(Theme.DARK_GRAY);
        letterArea.setLineWrap(true);
        letterArea.setWrapStyleWord(true);
        letterArea.setEditable(false);
        letterArea.setOpaque(false);
        left.add(letterArea);

        card.add(left, BorderLayout.CENTER);

        // Right Info & Actions
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(200, 0));

        JLabel bidLbl = Theme.label(String.format("$%,.0f", bid), Theme.FONT_TITLE, Theme.ACCENT);
        bidLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel estLbl = Theme.label("Est: " + days + " days", Theme.FONT_TINY, Theme.MID_GRAY);
        estLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        right.add(bidLbl);
        right.add(estLbl);
        right.add(Box.createVerticalGlue());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.setOpaque(false);
        btns.setAlignmentX(Component.RIGHT_ALIGNMENT);

        Theme.FlatButton viewJob = Theme.btnOutline("View Job");
        viewJob.setFont(Theme.FONT_SMALL);
        viewJob.addActionListener(e -> MainFrame.get().show(MainFrame.JOB_DETAIL, jobId));
        btns.add(viewJob);

        if ("PENDING".equalsIgnoreCase(status)) {
            Theme.FlatButton withdrawBtn = Theme.btnDanger("Withdraw");
            withdrawBtn.setFont(Theme.FONT_SMALL);
            withdrawBtn.addActionListener(e -> {
                int conf = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to withdraw this proposal?",
                    "Withdraw Proposal",
                    JOptionPane.YES_NO_OPTION
                );
                if (conf == JOptionPane.YES_OPTION) {
                    try {
                        ApiClient.post("/api/proposals/" + pId + "/withdraw?freelancerId=" + Session.userId, "{}");
                        loadProposalsAsync();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Failed to withdraw proposal.");
                    }
                }
            });
            btns.add(withdrawBtn);
        }

        right.add(btns);
        card.add(right, BorderLayout.EAST);

        return card;
    }
}
