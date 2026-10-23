package com.freelance.ui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

/**
 * JobDetailPanel — Premium deep dive into a job opportunity.
 * Supports proposal submission, proposal review for clients, and Google Gemini AI Resume Compatibility analysis.
 */
public class JobDetailPanel extends JPanel {

    private final long jobId;
    private final JPanel contentArea = new JPanel();
    private final JTextArea letterArea = new JTextArea(5, 20);

    public JobDetailPanel(long jobId) {
        this.jobId = jobId;
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        buildShell();
        loadJobDetailsAsync();
    }

    private void buildShell() {
        contentArea.setLayout(new BoxLayout(contentArea, BoxLayout.Y_AXIS));
        contentArea.setOpaque(false);
        contentArea.setBorder(BorderFactory.createEmptyBorder(28, 48, 48, 48));

        // Back link
        JPanel topNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topNav.setOpaque(false);
        topNav.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel backBtn = Theme.label("← Back to Browse Jobs", Theme.FONT_SMALL, Theme.ACCENT);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                MainFrame.get().show(MainFrame.BROWSE_JOBS);
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                backBtn.setForeground(Theme.ACCENT_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                backBtn.setForeground(Theme.ACCENT);
            }
        });
        topNav.add(backBtn);
        contentArea.add(topNav);
        contentArea.add(Box.createVerticalStrut(20));

        add(Theme.scroll(contentArea), BorderLayout.CENTER);
    }

    private void loadJobDetailsAsync() {
        JLabel loading = Theme.label("Fetching job details...", Theme.FONT_BODY, Theme.MID_GRAY);
        loading.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentArea.add(loading);
        contentArea.revalidate();
        contentArea.repaint();

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return ApiClient.get("/api/jobs/" + jobId);
            }

            @Override
            protected void done() {
                try {
                    String json = get();
                    renderJob(json);
                } catch (Exception e) {
                    contentArea.removeAll();
                    buildShell();
                    JLabel err = Theme.label("Job not found or server error.", Theme.FONT_BODY, Theme.RED);
                    contentArea.add(err);
                    contentArea.revalidate();
                    contentArea.repaint();
                }
            }
        };
        worker.execute();
    }

    private void renderJob(String jobJson) {
        contentArea.removeAll();

        // Re-add back button
        JPanel topNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topNav.setOpaque(false);
        topNav.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel backBtn = Theme.label("← Back to Browse Jobs", Theme.FONT_SMALL, Theme.ACCENT);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                MainFrame.get().show(MainFrame.BROWSE_JOBS);
            }
        });
        topNav.add(backBtn);
        contentArea.add(topNav);
        contentArea.add(Box.createVerticalStrut(18));

        long clientId = ApiClient.jsonLong(jobJson, "clientId");
        String title = ApiClient.jsonStr(jobJson, "title");
        String description = ApiClient.jsonStr(jobJson, "description");
        double budget = ApiClient.jsonDouble(jobJson, "budget");
        String category = ApiClient.jsonStr(jobJson, "category");
        String skills = ApiClient.jsonStr(jobJson, "requiredSkills");
        String status = ApiClient.jsonStr(jobJson, "status");
        String createdAt = ApiClient.jsonStr(jobJson, "createdAt");

        // ── Top Header Hero Card ──────────────────────────────────────────────
        Theme.GlassPanel hero = Theme.card();
        hero.setLayout(new BorderLayout(24, 16));
        hero.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel heroLeft = new JPanel();
        heroLeft.setLayout(new BoxLayout(heroLeft, BoxLayout.Y_AXIS));
        heroLeft.setOpaque(false);

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        badgeRow.setOpaque(false);
        badgeRow.add(Theme.badge(status != null ? status : "OPEN"));
        if (category != null && !category.isBlank()) {
            badgeRow.add(Theme.label(category.toUpperCase(), Theme.FONT_TINY, Theme.MID_GRAY));
        }
        if (createdAt != null) {
            String dateOnly = createdAt.contains("T") ? createdAt.substring(0, createdAt.indexOf("T")) : createdAt;
            badgeRow.add(Theme.label("• Posted " + dateOnly, Theme.FONT_TINY, Theme.MID_GRAY));
        }
        heroLeft.add(badgeRow);
        heroLeft.add(Box.createVerticalStrut(10));

        JLabel titleLbl = Theme.label(title != null ? title : "Job Opportunity", Theme.FONT_HERO, Theme.NEAR_BLACK);
        heroLeft.add(titleLbl);
        hero.add(heroLeft, BorderLayout.CENTER);

        // Budget Pill
        JPanel heroRight = new JPanel();
        heroRight.setLayout(new BoxLayout(heroRight, BoxLayout.Y_AXIS));
        heroRight.setOpaque(false);
        heroRight.setPreferredSize(new Dimension(180, 0));

        JLabel budVal = Theme.label(budget > 0 ? String.format("$%,.0f", budget) : "Competitive", Theme.FONT_HERO, Theme.ACCENT);
        budVal.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel budSub = Theme.label("Project Budget", Theme.FONT_TINY, Theme.MID_GRAY);
        budSub.setAlignmentX(Component.RIGHT_ALIGNMENT);
        heroRight.add(budVal);
        heroRight.add(budSub);

        hero.add(heroRight, BorderLayout.EAST);
        contentArea.add(hero);
        contentArea.add(Box.createVerticalStrut(24));

        // ── Main Two-Column Layout ────────────────────────────────────────────
        JPanel columns = new JPanel(new GridBagLayout());
        columns.setOpaque(false);
        columns.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 20);
        gbc.weightx = 0.58;
        gbc.weighty = 1.0;
        gbc.gridx = 0;

        // Left Column: Details & Client Actions
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        // Description Card
        Theme.GlassPanel descCard = Theme.card();
        descCard.setLayout(new BoxLayout(descCard, BoxLayout.Y_AXIS));
        descCard.setBorder(BorderFactory.createEmptyBorder(28, 30, 28, 30));

        JLabel descHeader = Theme.label("Project Overview", Theme.FONT_H3, Theme.NEAR_BLACK);
        descCard.add(descHeader);
        descCard.add(Box.createVerticalStrut(14));

        JTextArea descArea = new JTextArea(description != null ? description : "No detailed description provided.");
        descArea.setFont(Theme.FONT_BODY);
        descArea.setForeground(Theme.DARK_GRAY);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setBorder(BorderFactory.createEmptyBorder());
        descCard.add(descArea);
        descCard.add(Box.createVerticalStrut(24));

        // Required Skills Section
        JLabel skillsHeader = Theme.label("Required Expertise", Theme.FONT_H3, Theme.NEAR_BLACK);
        descCard.add(skillsHeader);
        descCard.add(Box.createVerticalStrut(12));

        JPanel skillsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        skillsRow.setOpaque(false);
        if (skills != null && !skills.isBlank()) {
            for (String s : skills.split(",")) {
                skillsRow.add(Theme.tag(s.trim()));
            }
        } else {
            skillsRow.add(Theme.label("No specific skills specified", Theme.FONT_SMALL, Theme.MID_GRAY));
        }
        descCard.add(skillsRow);

        leftCol.add(descCard);
        leftCol.add(Box.createVerticalStrut(20));

        // If client owns this job, show received proposals!
        boolean isOwner = Session.isLoggedIn() && Session.userId == clientId;
        if (isOwner) {
            leftCol.add(buildClientProposalsSection());
        }

        columns.add(leftCol, gbc);

        // Right Column: Proposal or Resume Checker
        gbc.weightx = 0.42;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 1;

        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);

        if (Session.isLoggedIn() && Session.isFreelancer()) {
            rightCol.add(buildProposalFormCard());
            rightCol.add(Box.createVerticalStrut(20));
            rightCol.add(buildResumeCompatibilityCard());
        } else if (!Session.isLoggedIn()) {
            Theme.GlassPanel promptCard = Theme.card();
            promptCard.setLayout(new BoxLayout(promptCard, BoxLayout.Y_AXIS));
            promptCard.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

            JLabel pTitle = Theme.label("Interested in this role?", Theme.FONT_H3, Theme.NEAR_BLACK);
            JLabel pSub = Theme.label("Sign in or create an account to submit your proposal and pitch your craft.", Theme.FONT_BODY, Theme.MID_GRAY);
            Theme.FlatButton loginCta = Theme.btnPrimary("Sign In to Apply");
            loginCta.addActionListener(e -> MainFrame.get().show(MainFrame.LOGIN));

            promptCard.add(pTitle);
            promptCard.add(Box.createVerticalStrut(10));
            promptCard.add(pSub);
            promptCard.add(Box.createVerticalStrut(18));
            promptCard.add(loginCta);
            rightCol.add(promptCard);
        } else {
            // Client is viewing
            Theme.GlassPanel clientCard = Theme.card();
            clientCard.setLayout(new BoxLayout(clientCard, BoxLayout.Y_AXIS));
            clientCard.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

            JLabel cTitle = Theme.label("Project Management", Theme.FONT_H3, Theme.NEAR_BLACK);
            JLabel cSub = Theme.label("You are currently viewing this job as a Client.", Theme.FONT_BODY, Theme.MID_GRAY);
            clientCard.add(cTitle);
            clientCard.add(Box.createVerticalStrut(8));
            clientCard.add(cSub);
            rightCol.add(clientCard);
        }

        columns.add(rightCol, gbc);
        contentArea.add(columns);

        contentArea.revalidate();
        contentArea.repaint();
    }

    // ── Proposal Form for Freelancers ──────────────────────────────────────────
    private JPanel buildProposalFormCard() {
        Theme.GlassPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel title = Theme.label("Submit a Proposal", Theme.FONT_H3, Theme.NEAR_BLACK);
        JLabel subtitle = Theme.label("Set your terms and explain why you're ideal for this work.", Theme.FONT_SMALL, Theme.MID_GRAY);
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(18));

        JTextField bidField = new JTextField();
        Theme.styleInput(bidField);
        bidField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JTextField daysField = new JTextField();
        Theme.styleInput(daysField);
        daysField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        letterArea.setFont(Theme.FONT_BODY);
        letterArea.setLineWrap(true);
        letterArea.setWrapStyleWord(true);
        letterArea.setBorder(BorderFactory.createCompoundBorder(
            new Theme.RoundBorder(Theme.LIGHT_GRAY, Theme.RADIUS_SM, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        card.add(Theme.label("Your Bid Amount ($)", Theme.FONT_SMALL, Theme.DARK_GRAY));
        card.add(Box.createVerticalStrut(4));
        card.add(bidField);
        card.add(Box.createVerticalStrut(12));

        card.add(Theme.label("Estimated Delivery (Days)", Theme.FONT_SMALL, Theme.DARK_GRAY));
        card.add(Box.createVerticalStrut(4));
        card.add(daysField);
        card.add(Box.createVerticalStrut(12));

        card.add(Theme.label("Cover Letter & Pitch", Theme.FONT_SMALL, Theme.DARK_GRAY));
        card.add(Box.createVerticalStrut(4));
        card.add(new JScrollPane(letterArea));
        card.add(Box.createVerticalStrut(16));

        JLabel msgLabel = Theme.label("", Theme.FONT_SMALL, Theme.RED);
        msgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(msgLabel);
        card.add(Box.createVerticalStrut(8));

        Theme.FlatButton submitBtn = Theme.btnPrimary("Send Proposal →");
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        submitBtn.addActionListener(e -> {
            String bidStr = bidField.getText().trim();
            String daysStr = daysField.getText().trim();
            String letter = letterArea.getText().trim();

            if (bidStr.isEmpty() || daysStr.isEmpty() || letter.isEmpty()) {
                msgLabel.setForeground(Theme.RED);
                msgLabel.setText("Please complete all fields.");
                return;
            }

            double bid;
            int days;
            try {
                bid = Double.parseDouble(bidStr);
                days = Integer.parseInt(daysStr);
            } catch (Exception ex) {
                msgLabel.setForeground(Theme.RED);
                msgLabel.setText("Bid and days must be valid numbers.");
                return;
            }

            submitBtn.setEnabled(false);
            submitBtn.setText("Submitting...");

            SwingWorker<String, Void> sw = new SwingWorker<>() {
                @Override
                protected String doInBackground() throws Exception {
                    String json = "{\"jobId\":" + jobId +
                                  ",\"freelancerId\":" + Session.userId +
                                  ",\"bidAmount\":" + bid +
                                  ",\"estimatedDays\":" + days +
                                  ",\"coverLetter\":" + ApiClient.esc(letter) + "}";
                    return ApiClient.post("/api/proposals", json);
                }

                @Override
                protected void done() {
                    submitBtn.setEnabled(true);
                    submitBtn.setText("Send Proposal →");
                    try {
                        get();
                        msgLabel.setForeground(Theme.GREEN_TEXT);
                        msgLabel.setText("Proposal submitted successfully!");
                        bidField.setText("");
                        daysField.setText("");
                    } catch (Exception ex) {
                        msgLabel.setForeground(Theme.RED);
                        msgLabel.setText("Error submitting proposal: " + ex.getMessage());
                    }
                }
            };
            sw.execute();
        });

        card.add(submitBtn);
        return card;
    }

    // ── AI Resume Compatibility Card with Google Gemini API ─────────────────────
    private JPanel buildResumeCompatibilityCard() {
        Theme.GlassPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Header with Gemini badge & Key configuration
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JPanel titleGroup = new JPanel();
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.setOpaque(false);

        JLabel title = Theme.label("AI Resume Compatibility", Theme.FONT_H3, Theme.NEAR_BLACK);
        JLabel subtitle = Theme.label("Job-specific evaluation of skills & qualifications for this role.", Theme.FONT_SMALL, Theme.MID_GRAY);
        titleGroup.add(title);
        titleGroup.add(Box.createVerticalStrut(2));
        titleGroup.add(subtitle);
        headerRow.add(titleGroup, BorderLayout.CENTER);

        // Gemini Key config button (no unprintable unicode symbols)
        String keyStatus = (Session.geminiApiKey != null && !Session.geminiApiKey.isEmpty()) ? "Gemini Key: Active" : "Set Gemini Key";
        Theme.FlatButton keyBtn = Theme.btnGhost(keyStatus);
        keyBtn.setFont(Theme.FONT_TINY);
        keyBtn.addActionListener(e -> promptForGeminiKey(keyBtn));
        headerRow.add(keyBtn, BorderLayout.EAST);

        card.add(headerRow);
        card.add(Box.createVerticalStrut(16));

        // Action Buttons Row: 1. From My Profile, 2. Upload File, 3. Paste Text
        JPanel actionsGrid = new JPanel(new GridLayout(1, 3, 8, 0));
        actionsGrid.setOpaque(false);
        actionsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        Theme.FlatButton profileBtn = Theme.btnOutline("My Profile");
        profileBtn.setFont(Theme.FONT_SMALL);

        Theme.FlatButton uploadBtn = Theme.btnOutline("Upload File");
        uploadBtn.setFont(Theme.FONT_SMALL);

        Theme.FlatButton pasteBtn = Theme.btnOutline("Paste Text");
        pasteBtn.setFont(Theme.FONT_SMALL);

        actionsGrid.add(profileBtn);
        actionsGrid.add(uploadBtn);
        actionsGrid.add(pasteBtn);
        card.add(actionsGrid);
        card.add(Box.createVerticalStrut(14));

        // Result Container
        JPanel resultContainer = new JPanel();
        resultContainer.setLayout(new BoxLayout(resultContainer, BoxLayout.Y_AXIS));
        resultContainer.setOpaque(false);

        JLabel initialHint = Theme.label("Select an option above to evaluate your qualifications against this job.", Theme.FONT_SMALL, Theme.MID_GRAY);
        resultContainer.add(initialHint);
        card.add(resultContainer);

        // 1. Analyze from My Profile action
        profileBtn.addActionListener(e -> {
            if (!Session.isLoggedIn() || Session.userId <= 0) {
                JOptionPane.showMessageDialog(this, "Please sign in as a freelancer to test your saved profile, or use 'Upload File' / 'Paste Text'.");
                return;
            }

            resultContainer.removeAll();
            resultContainer.add(Theme.label("Evaluating profile against job requirements...", Theme.FONT_SMALL, Theme.ACCENT));
            resultContainer.revalidate();
            resultContainer.repaint();

            SwingWorker<String, Void> sw = new SwingWorker<>() {
                @Override
                protected String doInBackground() throws Exception {
                    String json = "{\"jobId\":" + jobId + ",\"userId\":" + Session.userId +
                                  (Session.geminiApiKey.isEmpty() ? "" : ",\"geminiKey\":" + ApiClient.esc(Session.geminiApiKey)) + "}";
                    return ApiClient.post("/api/resume/compatibility/profile", json);
                }

                @Override
                protected void done() {
                    try {
                        renderAnalysisResults(resultContainer, get());
                    } catch (Exception ex) {
                        resultContainer.removeAll();
                        resultContainer.add(Theme.label("Analysis error: " + ex.getMessage(), Theme.FONT_SMALL, Theme.RED));
                        resultContainer.revalidate();
                        resultContainer.repaint();
                    }
                }
            };
            sw.execute();
        });

        // 2. Upload resume file action
        uploadBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Documents (*.txt, *.pdf, *.docx)", "txt", "pdf", "docx"));
            int res = chooser.showOpenDialog(JobDetailPanel.this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    resultContainer.removeAll();
                    resultContainer.add(Theme.label("Extracting document and analyzing job match...", Theme.FONT_SMALL, Theme.ACCENT));
                    resultContainer.revalidate();
                    resultContainer.repaint();

                    SwingWorker<String, Void> sw = new SwingWorker<>() {
                        @Override
                        protected String doInBackground() throws Exception {
                            return ApiClient.postMultipartResume("/api/resume/compatibility", jobId, file.getName(), bytes, Session.geminiApiKey);
                        }

                        @Override
                        protected void done() {
                            try {
                                renderAnalysisResults(resultContainer, get());
                            } catch (Exception ex) {
                                resultContainer.removeAll();
                                resultContainer.add(Theme.label("Analysis failed: " + ex.getMessage(), Theme.FONT_SMALL, Theme.RED));
                                resultContainer.revalidate();
                                resultContainer.repaint();
                            }
                        }
                    };
                    sw.execute();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(JobDetailPanel.this, "Could not read file: " + ex.getMessage());
                }
            }
        });

        // 3. Paste Resume Text action
        pasteBtn.addActionListener(e -> promptForResumeText(resultContainer));

        return card;
    }

    private void promptForGeminiKey(JButton triggerBtn) {
        JDialog dlg = new JDialog(MainFrame.get(), "Configure Google Gemini API", true);
        dlg.setSize(480, 260);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Theme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel title = Theme.label("Google Gemini API Integration", Theme.FONT_H3, Theme.NEAR_BLACK);
        JLabel sub = Theme.label("Enter your Gemini API key to activate deep neural analysis:", Theme.FONT_BODY, Theme.MID_GRAY);
        p.add(title);
        p.add(Box.createVerticalStrut(4));
        p.add(sub);
        p.add(Box.createVerticalStrut(14));

        JTextField keyField = new JTextField(Session.geminiApiKey);
        Theme.styleInput(keyField);
        keyField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(keyField);
        p.add(Box.createVerticalStrut(8));

        JLabel hint = Theme.label("Get a free API key at https://aistudio.google.com/", Theme.FONT_TINY, Theme.MID_GRAY);
        p.add(hint);
        p.add(Box.createVerticalStrut(18));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        Theme.FlatButton clearBtn = Theme.btnOutline("Clear Key");
        clearBtn.addActionListener(e -> {
            Session.geminiApiKey = "";
            triggerBtn.setText("Set Gemini Key");
            dlg.dispose();
            JOptionPane.showMessageDialog(this, "Gemini key cleared. Using job-specific Smart Heuristic Engine.");
        });

        Theme.FlatButton saveBtn = Theme.btnPrimary("Save & Activate");
        saveBtn.addActionListener(e -> {
            String key = keyField.getText().trim();
            if (!key.isEmpty()) {
                Session.geminiApiKey = key;
                triggerBtn.setText("Gemini Key: Active");
                // Persist to backend service
                SwingWorker<Void, Void> sw = new SwingWorker<>() {
                    @Override protected Void doInBackground() throws Exception {
                        ApiClient.post("/api/resume/gemini-key", "{\"apiKey\":" + ApiClient.esc(key) + "}");
                        return null;
                    }
                };
                sw.execute();
                dlg.dispose();
                JOptionPane.showMessageDialog(this, "Google Gemini API key activated successfully!");
            }
        });

        btnRow.add(clearBtn);
        btnRow.add(saveBtn);
        p.add(btnRow);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    private void promptForResumeText(JPanel resultContainer) {
        JDialog dlg = new JDialog(MainFrame.get(), "Paste Resume or Profile Text", true);
        dlg.setSize(520, 420);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Theme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel title = Theme.label("Paste Resume Content", Theme.FONT_H3, Theme.NEAR_BLACK);
        JLabel sub = Theme.label("Paste your resume, skills, or experience below to evaluate against this job:", Theme.FONT_SMALL, Theme.MID_GRAY);
        p.add(title);
        p.add(Box.createVerticalStrut(4));
        p.add(sub);
        p.add(Box.createVerticalStrut(12));

        JTextArea ta = new JTextArea();
        ta.setFont(Theme.FONT_BODY);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(460, 220));
        p.add(sp);
        p.add(Box.createVerticalStrut(14));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        Theme.FlatButton cancel = Theme.btnOutline("Cancel");
        cancel.addActionListener(e -> dlg.dispose());

        Theme.FlatButton analyze = Theme.btnPrimary("Run AI Analysis →");
        analyze.addActionListener(e -> {
            String text = ta.getText().trim();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Please paste resume text.");
                return;
            }
            dlg.dispose();

            resultContainer.removeAll();
            resultContainer.add(Theme.label("Evaluating resume alignment...", Theme.FONT_SMALL, Theme.ACCENT));
            resultContainer.revalidate();
            resultContainer.repaint();

            SwingWorker<String, Void> sw = new SwingWorker<>() {
                @Override
                protected String doInBackground() throws Exception {
                    String json = "{\"jobId\":" + jobId + ",\"resumeText\":" + ApiClient.esc(text) +
                                  (Session.geminiApiKey.isEmpty() ? "" : ",\"geminiKey\":" + ApiClient.esc(Session.geminiApiKey)) + "}";
                    return ApiClient.post("/api/resume/compatibility/text", json);
                }

                @Override
                protected void done() {
                    try {
                        renderAnalysisResults(resultContainer, get());
                    } catch (Exception ex) {
                        resultContainer.removeAll();
                        resultContainer.add(Theme.label("Error: " + ex.getMessage(), Theme.FONT_SMALL, Theme.RED));
                        resultContainer.revalidate();
                        resultContainer.repaint();
                    }
                }
            };
            sw.execute();
        });

        btnRow.add(cancel);
        btnRow.add(analyze);
        p.add(btnRow);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    private void renderAnalysisResults(JPanel container, String json) {
        container.removeAll();

        double score = ApiClient.jsonDouble(json, "score");
        String label = ApiClient.jsonStr(json, "label");
        String engine = ApiClient.jsonStr(json, "engine");
        boolean isGemini = ApiClient.jsonBool(json, "isGeminiPowered");
        String summary = ApiClient.jsonStr(json, "editedSummary");
        List<String> matched = ApiClient.jsonStrList(json, "matchedSkills");
        List<String> missing = ApiClient.jsonStrList(json, "missingSkills");

        boolean isNotSuitable = score <= 15 || (label != null && label.toLowerCase().contains("not good"));

        // Top Score Header Row
        JPanel topScore = new JPanel(new BorderLayout(12, 0));
        topScore.setOpaque(false);

        Color scoreColor = isNotSuitable ? Theme.RED_TEXT : (score >= 70 ? Theme.GREEN_TEXT : Theme.ACCENT);
        JLabel scoreVal = Theme.label(String.format("%.0f%%", score), Theme.FONT_HERO, scoreColor);
        topScore.add(scoreVal, BorderLayout.WEST);

        JPanel scoreLabels = new JPanel();
        scoreLabels.setLayout(new BoxLayout(scoreLabels, BoxLayout.Y_AXIS));
        scoreLabels.setOpaque(false);

        JLabel matchLabel = Theme.label(label != null ? label : "Compatibility Analysis", Theme.FONT_H3, isNotSuitable ? Theme.RED_TEXT : Theme.NEAR_BLACK);
        String cleanEngine = isGemini ? "Google Gemini 1.5 Flash" : (engine != null ? engine : "Smart Heuristic Engine");
        JLabel engineLabel = Theme.label(cleanEngine, Theme.FONT_TINY, isGemini ? Theme.ACCENT : Theme.MID_GRAY);
        scoreLabels.add(matchLabel);
        scoreLabels.add(Box.createVerticalStrut(2));
        scoreLabels.add(engineLabel);
        topScore.add(scoreLabels, BorderLayout.CENTER);

        container.add(topScore);
        container.add(Box.createVerticalStrut(10));

        // Animated Progress Bar (Red if not suitable, Accent/Green if suitable)
        Color barColor = isNotSuitable ? Theme.RED : (score >= 70 ? Theme.GREEN : Theme.ACCENT);
        Theme.AnimatedProgressBar bar = new Theme.AnimatedProgressBar(8, barColor, Theme.SEPARATOR);
        container.add(bar);
        bar.setProgressAnimated((int) score);
        container.add(Box.createVerticalStrut(14));

        // If not suitable, display prominent warning alert
        if (isNotSuitable) {
            Theme.RoundPanel alert = new Theme.RoundPanel(8, Theme.RED_LIGHT);
            alert.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            JLabel alertLbl = Theme.label("Not recommended for this role: Zero matching skills detected.", Theme.FONT_SMALL, Theme.RED_TEXT);
            alert.add(alertLbl, BorderLayout.CENTER);
            container.add(alert);
            container.add(Box.createVerticalStrut(12));
        }

        // Matched Skills
        if (!matched.isEmpty()) {
            JLabel mTitle = Theme.label("MATCHED STRENGTHS (" + matched.size() + ")", Theme.FONT_TINY, Theme.GREEN_TEXT);
            container.add(mTitle);
            container.add(Box.createVerticalStrut(4));

            JPanel mRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
            mRow.setOpaque(false);
            for (String s : matched) {
                mRow.add(Theme.successTag(s));
            }
            container.add(mRow);
            container.add(Box.createVerticalStrut(10));
        }

        // Missing Skills / Gaps
        if (!missing.isEmpty()) {
            JLabel gTitle = Theme.label("KEYWORD / SKILL GAPS (" + missing.size() + ")", Theme.FONT_TINY, Theme.ORANGE_TEXT);
            container.add(gTitle);
            container.add(Box.createVerticalStrut(4));

            JPanel gRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
            gRow.setOpaque(false);
            for (String s : missing) {
                gRow.add(Theme.warningTag(s));
            }
            container.add(gRow);
            container.add(Box.createVerticalStrut(10));
        }

        // Assessment Summary / Pitch
        if (summary != null && !summary.isBlank()) {
            JLabel pTitle = Theme.label(isNotSuitable ? "AI ASSESSMENT & ADVICE" : "TAILORED PROPOSAL PITCH", Theme.FONT_TINY, Theme.MID_GRAY);
            container.add(pTitle);
            container.add(Box.createVerticalStrut(4));

            JTextArea sumArea = new JTextArea(summary);
            sumArea.setFont(Theme.FONT_SMALL);
            sumArea.setForeground(isNotSuitable ? Theme.DARK_GRAY : Theme.NEAR_BLACK);
            sumArea.setLineWrap(true);
            sumArea.setWrapStyleWord(true);
            sumArea.setEditable(false);
            sumArea.setOpaque(false);
            container.add(sumArea);
            container.add(Box.createVerticalStrut(12));

            // Only allow inserting pitch if candidate is suitable (score >= 35)
            if (!isNotSuitable && score >= 35) {
                Theme.FlatButton copyPitchBtn = Theme.btnOutline("Insert Pitch into Proposal");
                copyPitchBtn.setFont(Theme.FONT_SMALL);
                copyPitchBtn.addActionListener(e -> {
                    String existing = letterArea.getText().trim();
                    if (existing.isEmpty()) {
                        letterArea.setText(summary);
                    } else {
                        letterArea.setText(summary + "\n\n" + existing);
                    }
                    copyPitchBtn.setText("Pitch Copied to Proposal!");
                });
                container.add(copyPitchBtn);
            }
        }

        container.revalidate();
        container.repaint();
    }

    // ── Proposals List for Client Owners ───────────────────────────────────────
    private JPanel buildClientProposalsSection() {
        Theme.GlassPanel pnl = Theme.card();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBorder(BorderFactory.createEmptyBorder(28, 30, 28, 30));

        JLabel title = Theme.label("Applicant Proposals", Theme.FONT_H3, Theme.NEAR_BLACK);
        JLabel sub = Theme.label("Review bids and select the right talent to award this contract.", Theme.FONT_SMALL, Theme.MID_GRAY);
        pnl.add(title);
        pnl.add(Box.createVerticalStrut(4));
        pnl.add(sub);
        pnl.add(Box.createVerticalStrut(18));

        JPanel proposalsList = new JPanel();
        proposalsList.setLayout(new BoxLayout(proposalsList, BoxLayout.Y_AXIS));
        proposalsList.setOpaque(false);
        pnl.add(proposalsList);

        SwingWorker<String, Void> sw = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return ApiClient.get("/api/proposals/job/" + jobId);
            }

            @Override
            protected void done() {
                try {
                    String[] items = ApiClient.jsonArray(get());
                    if (items.length == 0) {
                        proposalsList.add(Theme.label("No proposals submitted yet for this job.", Theme.FONT_BODY, Theme.MID_GRAY));
                    } else {
                        for (String pJson : items) {
                            proposalsList.add(createProposalRow(pJson));
                            proposalsList.add(Box.createVerticalStrut(12));
                        }
                    }
                    proposalsList.revalidate();
                    proposalsList.repaint();
                } catch (Exception e) {
                    proposalsList.add(Theme.label("Could not fetch proposals.", Theme.FONT_BODY, Theme.RED));
                }
            }
        };
        sw.execute();

        return pnl;
    }

    private JPanel createProposalRow(String pJson) {
        long pId = ApiClient.jsonLong(pJson, "id");
        long fId = ApiClient.jsonLong(pJson, "freelancerId");
        double bid = ApiClient.jsonDouble(pJson, "bidAmount");
        int days = (int) ApiClient.jsonLong(pJson, "estimatedDays");
        String letter = ApiClient.jsonStr(pJson, "coverLetter");
        String status = ApiClient.jsonStr(pJson, "status");

        Theme.RoundPanel row = Theme.solidCard();
        row.setLayout(new BorderLayout(16, 12));
        row.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.setOpaque(false);
        top.add(Theme.badge(status != null ? status : "PENDING"));
        top.add(Theme.label("Freelancer #" + fId, Theme.FONT_BOLD, Theme.NEAR_BLACK));
        top.add(Theme.label("• $" + String.format("%.0f", bid) + " in " + days + " days", Theme.FONT_BODY, Theme.ACCENT));
        left.add(top);
        left.add(Box.createVerticalStrut(8));

        JTextArea letterTxt = new JTextArea(letter != null ? letter : "");
        letterTxt.setFont(Theme.FONT_SMALL);
        letterTxt.setForeground(Theme.DARK_GRAY);
        letterTxt.setLineWrap(true);
        letterTxt.setWrapStyleWord(true);
        letterTxt.setEditable(false);
        letterTxt.setOpaque(false);
        left.add(letterTxt);

        row.add(left, BorderLayout.CENTER);

        // Actions if PENDING
        if ("PENDING".equalsIgnoreCase(status)) {
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            actions.setOpaque(false);

            Theme.FlatButton hireBtn = Theme.btnPrimary("Hire Freelancer");
            hireBtn.addActionListener(e -> {
                int conf = JOptionPane.showConfirmDialog(
                    this,
                    "Accept this proposal and create an active contract for $" + String.format("%.0f", bid) + "?",
                    "Confirm Hiring",
                    JOptionPane.YES_NO_OPTION
                );
                if (conf == JOptionPane.YES_OPTION) {
                    try {
                        ApiClient.post("/api/proposals/" + pId + "/accept", "{}");
                        JOptionPane.showMessageDialog(this, "Freelancer hired! Contract is now active.");
                        MainFrame.get().show(MainFrame.MY_CONTRACTS);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Hiring failed: " + ex.getMessage());
                    }
                }
            });

            Theme.FlatButton rejectBtn = Theme.btnDanger("Reject");
            rejectBtn.addActionListener(e -> {
                try {
                    ApiClient.post("/api/proposals/" + pId + "/reject", "{}");
                    loadJobDetailsAsync();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to reject proposal.");
                }
            });

            actions.add(rejectBtn);
            actions.add(hireBtn);
            row.add(actions, BorderLayout.EAST);
        }

        return row;
    }
}
