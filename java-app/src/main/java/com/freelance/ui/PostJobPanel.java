package com.freelance.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * PostJobPanel — Modern job creation workspace for clients.
 * Includes interactive fields, live marketplace preview card, and instant publishing.
 */
public class PostJobPanel extends JPanel {

    private final JTextField titleField = new JTextField(24);
    private final JComboBox<String> catBox = new JComboBox<>(new String[]{
        "Engineering", "Design", "AI & Data", "Marketing", "Writing", "Consulting"
    });
    private final JTextField budgetField = new JTextField(12);
    private final JTextField deadlineField = new JTextField(16);
    private final JTextField skillsField = new JTextField(24);
    private final JTextArea descArea = new JTextArea(6, 24);

    // Live preview labels
    private final JLabel previewTitle = Theme.label("Job Title Preview", Theme.FONT_H2, Theme.NEAR_BLACK);
    private final JLabel previewCategory = Theme.label("ENGINEERING", Theme.FONT_TINY, Theme.MID_GRAY);
    private final JLabel previewBudget = Theme.label("$0", Theme.FONT_TITLE, Theme.ACCENT);
    private final JLabel previewDesc = Theme.label("Your job description will appear here as you type...", Theme.FONT_BODY, Theme.DARK_GRAY);
    private final JPanel previewSkills = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));

    private final JLabel statusLabel = Theme.label("", Theme.FONT_SMALL, Theme.RED);

    public PostJobPanel() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setOpaque(false);
        page.setBorder(BorderFactory.createEmptyBorder(32, 48, 48, 48));

        // ── Header ─────────────────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel eyebrow = Theme.label("CLIENT WORKSPACE", Theme.FONT_TINY, Theme.ACCENT);
        JLabel title = Theme.label("Post a new opportunity.", Theme.FONT_HERO, Theme.NEAR_BLACK);
        JLabel sub = Theme.label(
            "Reach thousands of skilled specialists ready to bring your vision to life.",
            Theme.FONT_BODY, Theme.MID_GRAY
        );

        header.add(eyebrow);
        header.add(Box.createVerticalStrut(4));
        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);
        header.add(Box.createVerticalStrut(28));
        page.add(header);

        // ── Two Columns (Form on Left, Live Preview on Right) ───────────────────
        JPanel columns = new JPanel(new GridBagLayout());
        columns.setOpaque(false);
        columns.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 24);
        gbc.weightx = 0.58;
        gbc.weighty = 1.0;
        gbc.gridx = 0;

        // Left: Form Card
        Theme.GlassPanel formCard = Theme.card();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(BorderFactory.createEmptyBorder(28, 32, 32, 32));

        Theme.styleInput(titleField);
        Theme.styleInput(catBox);
        Theme.styleInput(budgetField);
        Theme.styleInput(deadlineField);
        Theme.styleInput(skillsField);

        descArea.setFont(Theme.FONT_BODY);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            new Theme.RoundBorder(Theme.LIGHT_GRAY, Theme.RADIUS_SM, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        // Wire live preview listeners
        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updatePreview(); }
            @Override public void removeUpdate(DocumentEvent e) { updatePreview(); }
            @Override public void changedUpdate(DocumentEvent e) { updatePreview(); }
        };
        titleField.getDocument().addDocumentListener(dl);
        budgetField.getDocument().addDocumentListener(dl);
        skillsField.getDocument().addDocumentListener(dl);
        descArea.getDocument().addDocumentListener(dl);
        catBox.addActionListener(e -> updatePreview());

        formCard.add(createField("Job Title", titleField, "e.g. Senior Java Spring Boot Architect"));
        formCard.add(Box.createVerticalStrut(14));

        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row.add(createField("Category", catBox, null));
        row.add(createField("Budget ($ USD)", budgetField, "e.g. 3500"));
        formCard.add(row);
        formCard.add(Box.createVerticalStrut(14));

        JPanel row2 = new JPanel(new GridLayout(1, 2, 16, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row2.add(createField("Deadline / Timeline", deadlineField, "e.g. 4 Weeks"));
        row2.add(createField("Required Skills (comma separated)", skillsField, "e.g. Java, Docker, AWS"));
        formCard.add(row2);
        formCard.add(Box.createVerticalStrut(14));

        formCard.add(createField("Project Description", new JScrollPane(descArea), "Outline deliverables, scope, and expectations..."));
        formCard.add(Box.createVerticalStrut(18));

        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(statusLabel);
        formCard.add(Box.createVerticalStrut(10));

        Theme.FlatButton publishBtn = Theme.btnPrimary("Publish Opportunity →");
        publishBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        publishBtn.addActionListener(e -> submitJob(publishBtn));
        formCard.add(publishBtn);

        columns.add(formCard, gbc);

        // Right: Live Preview Card
        gbc.weightx = 0.42;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 1;

        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);

        JLabel previewHead = Theme.label("LIVE MARKETPLACE PREVIEW", Theme.FONT_TINY, Theme.MID_GRAY);
        rightCol.add(previewHead);
        rightCol.add(Box.createVerticalStrut(10));

        Theme.GlassPanel previewCard = Theme.card();
        previewCard.setLayout(new BoxLayout(previewCard, BoxLayout.Y_AXIS));
        previewCard.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel pMeta = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pMeta.setOpaque(false);
        pMeta.add(Theme.badge("OPEN"));
        pMeta.add(previewCategory);
        previewCard.add(pMeta);
        previewCard.add(Box.createVerticalStrut(12));

        previewCard.add(previewTitle);
        previewCard.add(Box.createVerticalStrut(8));
        previewCard.add(previewBudget);
        previewCard.add(Box.createVerticalStrut(12));
        previewCard.add(previewDesc);
        previewCard.add(Box.createVerticalStrut(16));

        previewSkills.setOpaque(false);
        previewCard.add(previewSkills);

        rightCol.add(previewCard);
        columns.add(rightCol, gbc);

        page.add(columns);
        add(Theme.scroll(page), BorderLayout.CENTER);
    }

    private JPanel createField(String labelText, JComponent input, String placeholder) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel lbl = Theme.label(labelText, Theme.FONT_SMALL, Theme.MID_GRAY);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));
        p.add(lbl);

        if (input instanceof JTextField tf && placeholder != null) {
            tf.putClientProperty("JTextField.placeholderText", placeholder);
        }
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, input instanceof JScrollPane ? 120 : 42));
        p.add(input);
        return p;
    }

    private void updatePreview() {
        String t = titleField.getText().trim();
        previewTitle.setText(t.isEmpty() ? "Job Title Preview" : t);

        String cat = (String) catBox.getSelectedItem();
        previewCategory.setText(cat != null ? cat.toUpperCase() : "ENGINEERING");

        String b = budgetField.getText().trim();
        try {
            double d = Double.parseDouble(b);
            previewBudget.setText(String.format("$%,.0f", d));
        } catch (Exception ex) {
            previewBudget.setText(b.isEmpty() ? "$0" : "$" + b);
        }

        String d = descArea.getText().trim();
        previewDesc.setText(d.isEmpty()
            ? "Your job description will appear here as you type..."
            : (d.length() > 140 ? d.substring(0, 137) + "..." : d)
        );

        previewSkills.removeAll();
        String s = skillsField.getText().trim();
        if (!s.isEmpty()) {
            for (String item : s.split(",")) {
                if (!item.trim().isEmpty()) {
                    previewSkills.add(Theme.tag(item.trim()));
                }
            }
        } else {
            previewSkills.add(Theme.tag("Sample Skill"));
        }
        previewSkills.revalidate();
        previewSkills.repaint();
    }

    private void submitJob(JButton btn) {
        if (!Session.isLoggedIn()) {
            statusLabel.setForeground(Theme.RED);
            statusLabel.setText("You must be logged in to post a job.");
            return;
        }

        String title = titleField.getText().trim();
        String cat = (String) catBox.getSelectedItem();
        String budgetStr = budgetField.getText().trim();
        String deadline = deadlineField.getText().trim();
        String skills = skillsField.getText().trim();
        String desc = descArea.getText().trim();

        if (title.isEmpty() || desc.isEmpty() || budgetStr.isEmpty()) {
            statusLabel.setForeground(Theme.RED);
            statusLabel.setText("Title, Budget, and Description are required.");
            return;
        }

        double budget;
        try {
            budget = Double.parseDouble(budgetStr);
        } catch (Exception ex) {
            statusLabel.setForeground(Theme.RED);
            statusLabel.setText("Budget must be a valid number.");
            return;
        }

        btn.setEnabled(false);
        btn.setText("Publishing...");
        statusLabel.setText("");

        SwingWorker<String, Void> sw = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                String json = "{\"clientId\":" + Session.userId +
                              ",\"title\":" + ApiClient.esc(title) +
                              ",\"description\":" + ApiClient.esc(desc) +
                              ",\"budget\":" + budget +
                              ",\"category\":" + ApiClient.esc(cat) +
                              ",\"requiredSkills\":" + ApiClient.esc(skills) +
                              ",\"deadline\":" + ApiClient.esc(deadline) + "}";
                return ApiClient.post("/api/jobs", json);
            }

            @Override
            protected void done() {
                btn.setEnabled(true);
                btn.setText("Publish Opportunity →");
                try {
                    String resp = get();
                    long newJobId = ApiClient.jsonLong(resp, "id");
                    JOptionPane.showMessageDialog(PostJobPanel.this, "Job posted successfully to the marketplace!");
                    MainFrame.get().show(MainFrame.JOB_DETAIL, newJobId);
                } catch (Exception ex) {
                    statusLabel.setForeground(Theme.RED);
                    statusLabel.setText("Failed to post job: " + ex.getMessage());
                }
            }
        };
        sw.execute();
    }
}
