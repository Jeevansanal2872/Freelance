package com.freelance.ui;

import javax.swing.*;
import java.awt.*;

/**
 * DashboardPanel — Modern central workspace dashboard.
 * Features gradient obsidian hero banner, frosted glass metric cards, live pipeline, and quick actions.
 */
public class DashboardPanel extends JPanel {

    public DashboardPanel() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(28, 48, 36, 48));

        // ── Dark Obsidian Hero Banner ──────────────────────────────────────────
        Theme.GradientPanel hero = new Theme.GradientPanel(
            new Color(0x0C0D12), new Color(0x181B26), true
        );
        hero.setLayout(new BorderLayout(24, 0));
        hero.setBorder(BorderFactory.createEmptyBorder(32, 36, 32, 36));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        hero.setPreferredSize(new Dimension(0, 140));

        JPanel heroLeft = new JPanel();
        heroLeft.setLayout(new BoxLayout(heroLeft, BoxLayout.Y_AXIS));
        heroLeft.setOpaque(false);

        String greeting = Theme.greeting() + ", " + (Session.isLoggedIn() ? Session.firstName() : "Creator");
        JLabel heroTitle = Theme.label(greeting, Theme.FONT_TITLE, Theme.WHITE);
        heroLeft.add(heroTitle);
        heroLeft.add(Box.createVerticalStrut(6));

        String subText = Session.isClient()
            ? "Orchestrate your active projects, review proposals, and hire top talent."
            : "Track your client proposals, deliver outstanding work, and grow your career.";
        JLabel heroSub = Theme.label(subText, Theme.FONT_BODY, new Color(255, 255, 255, 185));
        heroLeft.add(heroSub);

        hero.add(heroLeft, BorderLayout.CENTER);

        // Hero CTA button
        JPanel heroRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 14));
        heroRight.setOpaque(false);
        if (Session.isClient()) {
            Theme.FlatButton cta = Theme.btnPrimary("Post an Opportunity →");
            cta.addActionListener(e -> MainFrame.get().show(MainFrame.POST_JOB));
            heroRight.add(cta);
        } else {
            Theme.FlatButton cta = Theme.btnPrimary("Explore Opportunities →");
            cta.addActionListener(e -> MainFrame.get().show(MainFrame.BROWSE_JOBS));
            heroRight.add(cta);
        }
        hero.add(heroRight, BorderLayout.EAST);

        content.add(hero);
        content.add(Box.createVerticalStrut(24));

        // ── Frosted Glass Metric Cards Row ─────────────────────────────────────
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        loadStatsAsync(statsRow);
        content.add(statsRow);
        content.add(Box.createVerticalStrut(28));

        // ── Main Content Grid (Listings & Sidebar) ────────────────────────────
        JPanel gridPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        gridPanel.setOpaque(false);
        gridPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 440));

        JPanel leftCol = createListingsPanel();
        gridPanel.add(leftCol);

        JPanel rightCol = createSidebarPanel();
        gridPanel.add(rightCol);

        content.add(gridPanel);
        content.add(Box.createVerticalGlue());

        add(Theme.scroll(content), BorderLayout.CENTER);
    }

    private void loadStatsAsync(JPanel statsRow) {
        statsRow.add(statCard("—", Session.isClient() ? "Jobs Posted" : "Proposals Sent", Theme.ACCENT));
        statsRow.add(statCard("—", "Active Contracts", Theme.GREEN));
        statsRow.add(statCard("—", "Completed Projects", Theme.NEAR_BLACK));

        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            @Override
            protected int[] doInBackground() throws Exception {
                int count = 0, active = 0, done = 0;

                if (Session.isClient()) {
                    String[] jobs = ApiClient.jsonArray(ApiClient.get("/api/jobs/client/" + Session.userId));
                    count = jobs.length;
                    String[] contracts = ApiClient.jsonArray(ApiClient.get("/api/contracts/client/" + Session.userId));
                    for (String c : contracts) {
                        String st = ApiClient.jsonStr(c, "status");
                        if ("ACTIVE".equalsIgnoreCase(st)) active++;
                        if ("COMPLETED".equalsIgnoreCase(st)) done++;
                    }
                } else {
                    String[] proposals = ApiClient.jsonArray(ApiClient.get("/api/proposals/my/" + Session.userId));
                    count = proposals.length;
                    String[] contracts = ApiClient.jsonArray(ApiClient.get("/api/contracts/freelancer/" + Session.userId));
                    for (String c : contracts) {
                        String st = ApiClient.jsonStr(c, "status");
                        if ("ACTIVE".equalsIgnoreCase(st)) active++;
                        if ("COMPLETED".equalsIgnoreCase(st)) done++;
                    }
                }
                return new int[]{count, active, done};
            }

            @Override
            protected void done() {
                try {
                    int[] d = get();
                    statsRow.removeAll();
                    statsRow.add(statCard(String.valueOf(d[0]), Session.isClient() ? "Jobs Posted" : "Proposals Sent", Theme.ACCENT));
                    statsRow.add(statCard(String.valueOf(d[1]), "Active Contracts", Theme.GREEN));
                    statsRow.add(statCard(String.valueOf(d[2]), "Completed Projects", Theme.NEAR_BLACK));
                    statsRow.revalidate();
                    statsRow.repaint();
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private Theme.GlassPanel statCard(String value, String label, Color accentColor) {
        Theme.GlassPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel dotRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dotRow.setOpaque(false);
        JPanel dot = new Theme.RoundPanel(8, accentColor) {
            @Override public Dimension getPreferredSize() { return new Dimension(8, 8); }
            @Override public Dimension getMaximumSize() { return getPreferredSize(); }
        };
        dotRow.add(dot);
        card.add(dotRow);
        card.add(Box.createVerticalStrut(10));

        JLabel valLabel = Theme.label(value, Theme.FONT_HERO, Theme.NEAR_BLACK);
        card.add(valLabel);
        card.add(Box.createVerticalStrut(4));

        JLabel nameLabel = Theme.label(label, Theme.FONT_SMALL, Theme.MID_GRAY);
        card.add(nameLabel);

        return card;
    }

    private JPanel createListingsPanel() {
        Theme.GlassPanel panel = Theme.card();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel title = Theme.label(
            Session.isClient() ? "Your Active Job Postings" : "Recent Proposals Submitted",
            Theme.FONT_H3, Theme.NEAR_BLACK
        );
        panel.add(title);
        panel.add(Box.createVerticalStrut(16));

        SwingWorker<String[], Void> worker = new SwingWorker<>() {
            @Override
            protected String[] doInBackground() throws Exception {
                if (Session.isClient()) {
                    return ApiClient.jsonArray(ApiClient.get("/api/jobs/client/" + Session.userId));
                } else {
                    return ApiClient.jsonArray(ApiClient.get("/api/proposals/my/" + Session.userId));
                }
            }

            @Override
            protected void done() {
                try {
                    String[] items = get();
                    if (items.length == 0) {
                        JLabel empty = Theme.label(
                            Session.isClient() ? "No jobs posted yet." : "No proposals submitted yet.",
                            Theme.FONT_BODY, Theme.MID_GRAY
                        );
                        panel.add(empty);
                        panel.add(Box.createVerticalStrut(14));

                        Theme.FlatButton cta = Session.isClient()
                            ? Theme.btnPrimary("Post a Job Now")
                            : Theme.btnPrimary("Browse Marketplace");
                        cta.addActionListener(e -> MainFrame.get().show(
                            Session.isClient() ? MainFrame.POST_JOB : MainFrame.BROWSE_JOBS
                        ));
                        panel.add(cta);
                    } else {
                        int limit = Math.min(items.length, 4);
                        for (int i = 0; i < limit; i++) {
                            panel.add(createListItem(items[i]));
                            if (i < limit - 1) {
                                panel.add(Box.createVerticalStrut(4));
                                panel.add(Theme.separator());
                                panel.add(Box.createVerticalStrut(4));
                            }
                        }
                    }
                    panel.revalidate();
                    panel.repaint();
                } catch (Exception ignored) {
                    panel.add(Theme.label("Unable to load records.", Theme.FONT_BODY, Theme.MID_GRAY));
                    panel.revalidate();
                }
            }
        };
        worker.execute();

        return panel;
    }

    private JPanel createListItem(String json) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        if (Session.isClient()) {
            String titleText = ApiClient.jsonStr(json, "title");
            String category = ApiClient.jsonStr(json, "category");
            String status = ApiClient.jsonStr(json, "status");
            double budget = ApiClient.jsonDouble(json, "budget");
            long jobId = ApiClient.jsonLong(json, "id");

            JLabel name = Theme.label(titleText != null ? titleText : "Untitled Job", Theme.FONT_BOLD, Theme.NEAR_BLACK);
            name.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            name.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    MainFrame.get().show(MainFrame.JOB_DETAIL, jobId);
                }
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    name.setForeground(Theme.ACCENT);
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    name.setForeground(Theme.NEAR_BLACK);
                }
            });
            info.add(name);

            String meta = (category != null ? category : "General") +
                " • $" + (budget > 0 ? String.format("%,.0f", budget) : "Flexible");
            info.add(Theme.label(meta, Theme.FONT_SMALL, Theme.MID_GRAY));

            row.add(info, BorderLayout.CENTER);
            if (status != null) {
                row.add(Theme.badge(status), BorderLayout.EAST);
            }
        } else {
            long jobId = ApiClient.jsonLong(json, "jobId");
            double bid = ApiClient.jsonDouble(json, "bidAmount");
            String status = ApiClient.jsonStr(json, "status");
            long days = ApiClient.jsonLong(json, "estimatedDays");

            JLabel name = Theme.label("Job #" + jobId, Theme.FONT_BOLD, Theme.NEAR_BLACK);
            name.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            name.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    MainFrame.get().show(MainFrame.JOB_DETAIL, jobId);
                }
            });
            info.add(name);

            String meta = (bid > 0 ? "Bid: $" + String.format("%,.0f", bid) : "No bid") +
                (days > 0 ? " • " + days + " days delivery" : "");
            info.add(Theme.label(meta, Theme.FONT_SMALL, Theme.MID_GRAY));

            row.add(info, BorderLayout.CENTER);
            if (status != null) {
                row.add(Theme.badge(status), BorderLayout.EAST);
            }
        }

        return row;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);

        // Profile completion card
        Theme.GlassPanel profileCard = Theme.card();
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        profileCard.add(Theme.label("Profile Strength", Theme.FONT_H3, Theme.NEAR_BLACK));
        profileCard.add(Box.createVerticalStrut(12));

        int pct = Session.profileCompletion > 0 ? Session.profileCompletion : 45;

        JPanel barBg = new Theme.RoundPanel(6, Theme.BG) {
            @Override public Dimension getPreferredSize() { return new Dimension(0, 8); }
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 8); }
        };
        barBg.setLayout(null);
        JPanel barFill = new Theme.RoundPanel(6, Theme.ACCENT);
        barFill.setBounds(0, 0, (int)(220 * pct / 100.0), 8);
        barBg.add(barFill);
        profileCard.add(barBg);
        profileCard.add(Box.createVerticalStrut(8));

        profileCard.add(Theme.label(pct + "% complete", Theme.FONT_SMALL, Theme.MID_GRAY));
        profileCard.add(Box.createVerticalStrut(14));

        Theme.FlatButton profileBtn = Theme.btnOutline("Edit Profile & Portfolio");
        profileBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        profileBtn.addActionListener(e -> MainFrame.get().show(MainFrame.PROFILE));
        profileCard.add(profileBtn);

        sidebar.add(profileCard);
        sidebar.add(Box.createVerticalStrut(16));

        // Quick Actions card
        Theme.GlassPanel actionsCard = Theme.card();
        actionsCard.setLayout(new BoxLayout(actionsCard, BoxLayout.Y_AXIS));
        actionsCard.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        actionsCard.add(Theme.label("Quick Actions", Theme.FONT_H3, Theme.NEAR_BLACK));
        actionsCard.add(Box.createVerticalStrut(14));

        if (Session.isClient()) {
            addQuickAction(actionsCard, "Post an Opportunity", MainFrame.POST_JOB, true);
            addQuickAction(actionsCard, "View Active Contracts", MainFrame.MY_CONTRACTS, false);
            addQuickAction(actionsCard, "Browse Marketplace", MainFrame.BROWSE_JOBS, false);
        } else {
            addQuickAction(actionsCard, "Browse Open Jobs", MainFrame.BROWSE_JOBS, true);
            addQuickAction(actionsCard, "Track My Proposals", MainFrame.MY_PROPOSALS, false);
            addQuickAction(actionsCard, "Active Contracts", MainFrame.MY_CONTRACTS, false);
        }

        sidebar.add(actionsCard);
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private void addQuickAction(JPanel parent, String text, String screen, boolean primary) {
        Theme.FlatButton btn = primary ? Theme.btnDark(text) : Theme.btnOutline(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.addActionListener(e -> MainFrame.get().show(screen));
        parent.add(btn);
        parent.add(Box.createVerticalStrut(8));
    }
}
