package com.freelance.ui;

import javax.swing.*;
import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MyContractsPanel — Modern contract tracker and manager.
 * Supports active engagements, milestone completion, reviews, and contract resolution.
 */
public class MyContractsPanel extends JPanel {

    private final JPanel listContainer = new JPanel();
    private final List<String> rawContracts = new ArrayList<>();
    private String selectedTab = "All";
    private final Map<String, Theme.FlatButton> tabButtons = new HashMap<>();

    public MyContractsPanel() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        buildUI();
        loadContractsAsync();
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

        JLabel eyebrow = Theme.label("ACTIVE AGREEMENTS", Theme.FONT_TINY, Theme.ACCENT);
        JLabel title = Theme.label("Contracts & Engagements.", Theme.FONT_HERO, Theme.NEAR_BLACK);
        JLabel sub = Theme.label(
            "Manage active collaborations, deliverables, completion sign-offs, and payment releases.",
            Theme.FONT_BODY, Theme.MID_GRAY
        );

        header.add(eyebrow);
        header.add(Box.createVerticalStrut(4));
        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);
        header.add(Box.createVerticalStrut(24));
        page.add(header);

        // Filter Bar (Tabs)
        Theme.GlassPanel tabsBar = Theme.card();
        tabsBar.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 8));
        tabsBar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        tabsBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        tabsBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] tabs = {"All", "ACTIVE", "COMPLETED", "CANCELLED"};
        for (String tab : tabs) {
            boolean active = tab.equalsIgnoreCase(selectedTab);
            Theme.FlatButton tabBtn = active ? Theme.btnDark(tab) : Theme.btnOutline(tab);
            tabBtn.setFont(Theme.FONT_SMALL);
            tabBtn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
            tabBtn.addActionListener(e -> selectTab(tab));
            tabButtons.put(tab, tabBtn);
            tabsBar.add(tabBtn);
        }
        page.add(tabsBar);
        page.add(Box.createVerticalStrut(24));

        // List Container
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);
        listContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(listContainer);

        add(Theme.scroll(page), BorderLayout.CENTER);
    }

    private void selectTab(String tab) {
        selectedTab = tab;
        for (Map.Entry<String, Theme.FlatButton> entry : tabButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equalsIgnoreCase(tab));
        }
        renderFilteredContracts();
    }

    private void loadContractsAsync() {
        if (!Session.isLoggedIn()) {
            listContainer.add(Theme.label("Please sign in to view your contracts.", Theme.FONT_BODY, Theme.MID_GRAY));
            return;
        }

        listContainer.removeAll();
        JLabel loading = Theme.label("Fetching contract records...", Theme.FONT_BODY, Theme.MID_GRAY);
        listContainer.add(loading);
        listContainer.revalidate();
        listContainer.repaint();

        String endpoint = Session.isClient()
            ? "/api/contracts/client/" + Session.userId
            : "/api/contracts/freelancer/" + Session.userId;

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return ApiClient.get(endpoint);
            }

            @Override
            protected void done() {
                try {
                    rawContracts.clear();
                    String[] items = ApiClient.jsonArray(get());
                    for (String item : items) {
                        rawContracts.add(item);
                    }
                    renderFilteredContracts();
                } catch (Exception ex) {
                    listContainer.removeAll();
                    listContainer.add(Theme.label("Error loading contracts: " + ex.getMessage(), Theme.FONT_BODY, Theme.RED));
                    listContainer.revalidate();
                    listContainer.repaint();
                }
            }
        };
        worker.execute();
    }

    private void renderFilteredContracts() {
        listContainer.removeAll();

        int count = 0;
        for (String cJson : rawContracts) {
            String status = ApiClient.jsonStr(cJson, "status");
            if (!"All".equalsIgnoreCase(selectedTab) && (status == null || !status.equalsIgnoreCase(selectedTab))) {
                continue;
            }
            listContainer.add(createContractCard(cJson));
            listContainer.add(Box.createVerticalStrut(16));
            count++;
        }

        if (count == 0) {
            Theme.GlassPanel empty = Theme.card();
            empty.setBorder(BorderFactory.createEmptyBorder(36, 36, 36, 36));
            empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));

            JLabel noContracts = Theme.label("No contracts in this view.", Theme.FONT_H3, Theme.NEAR_BLACK);
            JLabel hint = Theme.label(
                Session.isClient()
                    ? "Hire freelancers from submitted proposals to create active contracts."
                    : "When clients accept your proposals, active contracts will appear here.",
                Theme.FONT_BODY, Theme.MID_GRAY
            );

            empty.add(noContracts);
            empty.add(Box.createVerticalStrut(8));
            empty.add(hint);
            listContainer.add(empty);
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createContractCard(String cJson) {
        long cId = ApiClient.jsonLong(cJson, "id");
        long jobId = ApiClient.jsonLong(cJson, "jobId");
        long clientId = ApiClient.jsonLong(cJson, "clientId");
        long freelancerId = ApiClient.jsonLong(cJson, "freelancerId");
        double amount = ApiClient.jsonDouble(cJson, "amount");
        String status = ApiClient.jsonStr(cJson, "status");
        String startedAt = ApiClient.jsonStr(cJson, "startedAt");
        String endedAt = ApiClient.jsonStr(cJson, "endedAt");
        String note = ApiClient.jsonStr(cJson, "completionNote");

        Theme.GlassPanel card = Theme.card();
        card.enableHoverLift();
        card.setLayout(new BorderLayout(20, 16));
        card.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left Information
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setOpaque(false);
        topRow.add(Theme.badge(status != null ? status : "ACTIVE"));
        topRow.add(Theme.label("Contract #" + cId, Theme.FONT_H3, Theme.NEAR_BLACK));
        topRow.add(Theme.label("• Associated Job #" + jobId, Theme.FONT_SMALL, Theme.MID_GRAY));
        left.add(topRow);
        left.add(Box.createVerticalStrut(8));

        String dateInfo = "Started: " + (startedAt != null ? (startedAt.contains("T") ? startedAt.substring(0, startedAt.indexOf("T")) : startedAt) : "N/A");
        if (endedAt != null) {
            dateInfo += "  |  Completed: " + (endedAt.contains("T") ? endedAt.substring(0, endedAt.indexOf("T")) : endedAt);
        }
        JLabel datesLbl = Theme.label(dateInfo, Theme.FONT_SMALL, Theme.MID_GRAY);
        left.add(datesLbl);
        left.add(Box.createVerticalStrut(4));

        String counterpart = Session.isClient() ? "Freelancer ID: #" + freelancerId : "Client ID: #" + clientId;
        JLabel partLbl = Theme.label(counterpart, Theme.FONT_SMALL, Theme.DARK_GRAY);
        left.add(partLbl);

        if (note != null && !note.isBlank()) {
            left.add(Box.createVerticalStrut(6));
            JLabel noteLbl = Theme.label("Note: " + note, Theme.FONT_SMALL, Theme.GREEN_TEXT);
            left.add(noteLbl);
        }

        card.add(left, BorderLayout.CENTER);

        // Right Financials & Action
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(210, 0));

        JLabel amountLbl = Theme.label(String.format("$%,.0f", amount), Theme.FONT_TITLE, Theme.ACCENT);
        amountLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel amountSub = Theme.label("Agreed Amount", Theme.FONT_TINY, Theme.MID_GRAY);
        amountSub.setAlignmentX(Component.RIGHT_ALIGNMENT);

        right.add(amountLbl);
        right.add(amountSub);
        right.add(Box.createVerticalGlue());

        // Actions
        if ("ACTIVE".equalsIgnoreCase(status)) {
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            actions.setOpaque(false);
            actions.setAlignmentX(Component.RIGHT_ALIGNMENT);

            if (Session.isClient()) {
                Theme.FlatButton completeBtn = Theme.btnPrimary("Complete & Pay");
                completeBtn.setFont(Theme.FONT_SMALL);
                completeBtn.addActionListener(e -> {
                    String noteInput = JOptionPane.showInputDialog(
                        this,
                        "Enter completion note or feedback for the freelancer:",
                        "Complete Contract",
                        JOptionPane.QUESTION_MESSAGE
                    );
                    if (noteInput != null) {
                        try {
                            ApiClient.post("/api/contracts/" + cId + "/complete?clientId=" + Session.userId + "&note=" + URLEncoder.encode(noteInput, StandardCharsets.UTF_8), "{}");
                            JOptionPane.showMessageDialog(this, "Contract completed and payment released successfully!");
                            loadContractsAsync();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Failed to complete contract: " + ex.getMessage());
                        }
                    }
                });
                actions.add(completeBtn);
            }

            Theme.FlatButton cancelBtn = Theme.btnDanger("Cancel");
            cancelBtn.setFont(Theme.FONT_SMALL);
            cancelBtn.addActionListener(e -> {
                String reason = JOptionPane.showInputDialog(this, "Reason for cancellation:", "Cancel Contract", JOptionPane.WARNING_MESSAGE);
                if (reason != null) {
                    try {
                        ApiClient.post("/api/contracts/" + cId + "/cancel?userId=" + Session.userId + "&reason=" + URLEncoder.encode(reason, StandardCharsets.UTF_8), "{}");
                        JOptionPane.showMessageDialog(this, "Contract cancelled.");
                        loadContractsAsync();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Failed to cancel contract: " + ex.getMessage());
                    }
                }
            });
            actions.add(cancelBtn);

            right.add(actions);
        }

        card.add(right, BorderLayout.EAST);

        return card;
    }
}
