package com.freelance.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BrowseJobsPanel — Modern marketplace to explore and filter job listings.
 * Features frosted glass cards with interactive hover lifts, category filter pills,
 * real-time live search, and smooth interactions.
 */
public class BrowseJobsPanel extends JPanel {

    private final JTextField searchField = new JTextField(24);
    private final JPanel jobsContainer = new JPanel();
    private final List<String> rawJobsList = new ArrayList<>();
    private final JLabel resultsCountLabel = Theme.label("", Theme.FONT_SMALL, Theme.MID_GRAY);
    private String selectedCategory = "All";

    private final String[] categories = {
        "All", "Engineering", "Design", "AI & Data", "Marketing", "Writing"
    };

    private final Map<String, Theme.FlatButton> pillButtons = new HashMap<>();

    public BrowseJobsPanel() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        buildUI();
        loadJobsAsync();
    }

    private void buildUI() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setOpaque(false);
        page.setBorder(BorderFactory.createEmptyBorder(32, 48, 48, 48));

        // ── Header Section ─────────────────────────────────────────────────────
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel eyebrow = Theme.label("CURATED MARKETPLACE", Theme.FONT_TINY, Theme.ACCENT);
        JLabel title = Theme.label("Find work that inspires.", Theme.FONT_HERO, Theme.NEAR_BLACK);
        JLabel subtitle = Theme.label(
            "Discover high-impact opportunities from clients who value craftsmanship, clarity, and design.",
            Theme.FONT_BODY, Theme.MID_GRAY
        );

        headerPanel.add(eyebrow);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(subtitle);
        headerPanel.add(Box.createVerticalStrut(24));

        page.add(headerPanel);

        // ── Search & Filter Glass Bar ──────────────────────────────────────────
        Theme.GlassPanel filterBar = Theme.card();
        filterBar.setLayout(new BorderLayout(16, 12));
        filterBar.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        filterBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));
        filterBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Search Input with real-time DocumentListener
        Theme.styleInput(searchField);
        searchField.setPreferredSize(new Dimension(320, 42));
        searchField.putClientProperty("JTextField.placeholderText", "Search opportunities or skills...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        searchField.addActionListener(e -> applyFilter());

        Theme.FlatButton searchBtn = Theme.btnPrimary("Search");
        searchBtn.addActionListener(e -> applyFilter());

        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchGroup.setOpaque(false);
        searchGroup.add(searchField);
        searchGroup.add(searchBtn);
        filterBar.add(searchGroup, BorderLayout.WEST);

        // Category Pills
        JPanel pillsGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pillsGroup.setOpaque(false);

        for (String cat : categories) {
            boolean isSelected = cat.equalsIgnoreCase(selectedCategory);
            Theme.FlatButton pill = isSelected ? Theme.btnDark(cat) : Theme.btnOutline(cat);
            pill.setFont(Theme.FONT_SMALL);
            pill.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
            pill.addActionListener(e -> selectCategory(cat));
            pillButtons.put(cat, pill);
            pillsGroup.add(pill);
        }
        filterBar.add(pillsGroup, BorderLayout.EAST);

        page.add(filterBar);
        page.add(Box.createVerticalStrut(14));

        // Results count label
        resultsCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultsCountLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 10, 0));
        page.add(resultsCountLabel);

        // ── Jobs Container ─────────────────────────────────────────────────────
        jobsContainer.setLayout(new BoxLayout(jobsContainer, BoxLayout.Y_AXIS));
        jobsContainer.setOpaque(false);
        jobsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(jobsContainer);

        add(Theme.scroll(page), BorderLayout.CENTER);
    }

    private void selectCategory(String cat) {
        selectedCategory = cat;
        for (Map.Entry<String, Theme.FlatButton> entry : pillButtons.entrySet()) {
            boolean active = entry.getKey().equalsIgnoreCase(cat);
            entry.getValue().setActive(active);
        }
        applyFilter();
    }

    private void loadJobsAsync() {
        jobsContainer.removeAll();
        JLabel loading = Theme.label("Loading open marketplace opportunities...", Theme.FONT_BODY, Theme.MID_GRAY);
        loading.setAlignmentX(Component.LEFT_ALIGNMENT);
        jobsContainer.add(loading);
        jobsContainer.revalidate();
        jobsContainer.repaint();

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return ApiClient.get("/api/jobs");
            }

            @Override
            protected void done() {
                try {
                    String resp = get();
                    rawJobsList.clear();
                    String[] items = ApiClient.jsonArray(resp);
                    for (String item : items) {
                        rawJobsList.add(item);
                    }
                    applyFilter();
                } catch (Exception ex) {
                    jobsContainer.removeAll();
                    JLabel err = Theme.label("Unable to load jobs. Ensure the backend server is running.", Theme.FONT_BODY, Theme.RED);
                    Theme.FlatButton retry = Theme.btnOutline("Retry");
                    retry.addActionListener(e -> loadJobsAsync());
                    jobsContainer.add(err);
                    jobsContainer.add(Box.createVerticalStrut(12));
                    jobsContainer.add(retry);
                    jobsContainer.revalidate();
                    jobsContainer.repaint();
                }
            }
        };
        worker.execute();
    }

    private boolean matchesCategory(String jobCat, String filterCat) {
        if (filterCat == null || "All".equalsIgnoreCase(filterCat) || filterCat.isBlank()) {
            return true;
        }
        if (jobCat == null) return false;

        String j = jobCat.trim().toLowerCase();
        String f = filterCat.trim().toLowerCase();

        if (j.equals(f) || j.contains(f) || f.contains(j)) return true;

        if (f.contains("eng") && (j.contains("eng") || j.contains("dev") || j.contains("soft") || j.contains("architect"))) {
            return true;
        }
        if (f.contains("design") && (j.contains("design") || j.contains("ui") || j.contains("ux") || j.contains("figma") || j.contains("creative"))) {
            return true;
        }
        if (f.contains("ai") && (j.contains("ai") || j.contains("data") || j.contains("ml") || j.contains("machine") || j.contains("model"))) {
            return true;
        }
        if (f.contains("market") && (j.contains("market") || j.contains("brand") || j.contains("growth") || j.contains("strategy") || j.contains("seo"))) {
            return true;
        }
        if (f.contains("writ") && (j.contains("writ") || j.contains("copy") || j.contains("content"))) {
            return true;
        }

        return false;
    }

    private void applyFilter() {
        jobsContainer.removeAll();
        String query = searchField.getText().trim().toLowerCase();
        String[] queryTerms = query.isEmpty() ? new String[0] : query.split("\\s+");

        int matched = 0;
        for (String json : rawJobsList) {
            String title = ApiClient.jsonStr(json, "title");
            String cat = ApiClient.jsonStr(json, "category");
            String skills = ApiClient.jsonStr(json, "requiredSkills");
            String desc = ApiClient.jsonStr(json, "description");
            double budget = ApiClient.jsonDouble(json, "budget");

            if (title == null) title = "";
            if (cat == null) cat = "";
            if (skills == null) skills = "";
            if (desc == null) desc = "";

            // 1. Category filter
            if (!matchesCategory(cat, selectedCategory)) {
                continue;
            }

            // 2. Keyword query filter (checks every word typed)
            if (queryTerms.length > 0) {
                String corpus = (title + " " + cat + " " + skills + " " + desc + " " + (budget > 0 ? (int)budget : "")).toLowerCase();
                boolean allTermsMatch = true;
                for (String term : queryTerms) {
                    if (!corpus.contains(term)) {
                        allTermsMatch = false;
                        break;
                    }
                }
                if (!allTermsMatch) {
                    continue;
                }
            }

            jobsContainer.add(createJobCard(json));
            jobsContainer.add(Box.createVerticalStrut(16));
            matched++;
        }

        resultsCountLabel.setText("Showing " + matched + (matched == 1 ? " opportunity" : " opportunities") +
            (!"All".equalsIgnoreCase(selectedCategory) ? " in " + selectedCategory : ""));

        if (matched == 0) {
            Theme.GlassPanel emptyCard = Theme.card();
            emptyCard.setBorder(BorderFactory.createEmptyBorder(36, 36, 36, 36));
            emptyCard.setLayout(new BoxLayout(emptyCard, BoxLayout.Y_AXIS));
            emptyCard.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel noMatch = Theme.label("No opportunities matched your criteria.", Theme.FONT_H3, Theme.NEAR_BLACK);
            JLabel hint = Theme.label("Try adjusting your search terms or selecting 'All' categories.", Theme.FONT_BODY, Theme.MID_GRAY);
            Theme.FlatButton reset = Theme.btnOutline("Reset Filters");
            reset.addActionListener(e -> {
                searchField.setText("");
                selectCategory("All");
            });

            emptyCard.add(noMatch);
            emptyCard.add(Box.createVerticalStrut(8));
            emptyCard.add(hint);
            emptyCard.add(Box.createVerticalStrut(16));
            emptyCard.add(reset);
            jobsContainer.add(emptyCard);
        }

        jobsContainer.revalidate();
        jobsContainer.repaint();
    }

    private JPanel createJobCard(String json) {
        long jobId = ApiClient.jsonLong(json, "id");
        String title = ApiClient.jsonStr(json, "title");
        String category = ApiClient.jsonStr(json, "category");
        String skills = ApiClient.jsonStr(json, "requiredSkills");
        String desc = ApiClient.jsonStr(json, "description");
        double budget = ApiClient.jsonDouble(json, "budget");
        String status = ApiClient.jsonStr(json, "status");

        Theme.GlassPanel card = Theme.card();
        card.enableHoverLift();
        card.setLayout(new BorderLayout(20, 16));
        card.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left Content
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        // Tags & Meta Row
        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        metaRow.setOpaque(false);
        metaRow.add(Theme.badge(status != null ? status : "OPEN"));
        if (category != null && !category.isBlank()) {
            JLabel catLabel = Theme.label(category.toUpperCase(), Theme.FONT_TINY, Theme.MID_GRAY);
            catLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
            metaRow.add(catLabel);
        }
        left.add(metaRow);
        left.add(Box.createVerticalStrut(10));

        // Title
        JLabel titleLbl = Theme.label(title != null ? title : "Untitled Job", Theme.FONT_H2, Theme.NEAR_BLACK);
        titleLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        titleLbl.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                MainFrame.get().show(MainFrame.JOB_DETAIL, jobId);
            }
            @Override public void mouseEntered(MouseEvent e) {
                titleLbl.setForeground(Theme.ACCENT);
            }
            @Override public void mouseExited(MouseEvent e) {
                titleLbl.setForeground(Theme.NEAR_BLACK);
            }
        });
        left.add(titleLbl);
        left.add(Box.createVerticalStrut(8));

        // Description preview (truncated to 150 chars)
        String shortDesc = desc != null ? (desc.length() > 140 ? desc.substring(0, 137) + "..." : desc) : "";
        JLabel descLbl = Theme.label(shortDesc, Theme.FONT_BODY, Theme.DARK_GRAY);
        left.add(descLbl);
        left.add(Box.createVerticalStrut(14));

        // Skills tags
        if (skills != null && !skills.isBlank()) {
            JPanel skillsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            skillsRow.setOpaque(false);
            for (String s : skills.split(",")) {
                String clean = s.trim();
                if (!clean.isEmpty()) {
                    JLabel skillTag = Theme.tag(clean);
                    skillsRow.add(skillTag);
                }
            }
            left.add(skillsRow);
        }

        card.add(left, BorderLayout.CENTER);

        // Right Section: Budget & Action CTA
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(190, 0));

        JLabel budgetLbl = Theme.label(
            budget > 0 ? String.format("$%,.0f", budget) : "Competitive",
            Theme.FONT_TITLE, Theme.ACCENT
        );
        budgetLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel budgetSub = Theme.label("Estimated Budget", Theme.FONT_TINY, Theme.MID_GRAY);
        budgetSub.setAlignmentX(Component.RIGHT_ALIGNMENT);

        Theme.FlatButton viewBtn = Theme.btnPrimary("View Details →");
        viewBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        viewBtn.addActionListener(e -> MainFrame.get().show(MainFrame.JOB_DETAIL, jobId));

        right.add(budgetLbl);
        right.add(Box.createVerticalStrut(2));
        right.add(budgetSub);
        right.add(Box.createVerticalGlue());
        right.add(viewBtn);

        card.add(right, BorderLayout.EAST);

        Dimension pref = card.getPreferredSize();
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Math.max(160, pref.height)));

        return card;
    }
}
