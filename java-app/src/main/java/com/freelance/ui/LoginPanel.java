package com.freelance.ui;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * LoginPanel — Centered login card with acrylic glass effect.
 * Clean, minimal design with accent-blue CTA and demo shortcuts.
 */
public class LoginPanel extends JPanel {

    private final JTextField     emailField    = new JTextField(28);
    private final JPasswordField passwordField = new JPasswordField(28);
    private final Theme.FlatButton loginBtn    = Theme.btnPrimary("Sign In");
    private final JLabel          errorLabel   = Theme.label("", Theme.FONT_SMALL, Theme.RED);

    public LoginPanel() {
        setBackground(Theme.BG);
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        Theme.GlassPanel card = Theme.card();
        card.setPreferredSize(new Dimension(440, 580));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 44, 40, 44));

        // Brand avatar circle
        JPanel avatarRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avatarRow.setOpaque(false);
        JPanel av = Theme.avatar("FH", 56, Theme.ACCENT, Theme.WHITE);
        avatarRow.add(av);
        card.add(avatarRow);
        card.add(Box.createVerticalStrut(16));

        // Title & Subtitle
        JLabel title = Theme.label("Welcome back.", Theme.FONT_TITLE, Theme.NEAR_BLACK);
        title.setAlignmentX(CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(6));

        JLabel sub = Theme.label("Sign in to your Freelance . workspace", Theme.FONT_SMALL, Theme.MID_GRAY);
        sub.setAlignmentX(CENTER_ALIGNMENT);
        card.add(sub);
        card.add(Box.createVerticalStrut(24));

        // Quick Demo Fill buttons
        JPanel demoBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        demoBar.setOpaque(false);
        Theme.FlatButton demoClient = Theme.btnOutline("Demo Client");
        demoClient.setFont(Theme.FONT_TINY);
        demoClient.addActionListener(e -> {
            emailField.setText("client@nexus.demo");
            passwordField.setText("password123");
            errorLabel.setText("");
        });

        Theme.FlatButton demoFreelancer = Theme.btnOutline("Demo Freelancer");
        demoFreelancer.setFont(Theme.FONT_TINY);
        demoFreelancer.addActionListener(e -> {
            emailField.setText("Jeevan@engineer.demo");
            passwordField.setText("password123");
            errorLabel.setText("");
        });

        demoBar.add(demoClient);
        demoBar.add(demoFreelancer);
        card.add(demoBar);
        card.add(Box.createVerticalStrut(18));

        // Fields
        card.add(fieldGroup("Email address", emailField));
        card.add(Box.createVerticalStrut(12));
        card.add(fieldGroup("Password", passwordField));
        card.add(Box.createVerticalStrut(8));

        errorLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(14));

        // Sign In button
        loginBtn.setAlignmentX(CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.addActionListener(e -> doLogin());
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(14));

        // Browse without login
        Theme.FlatButton guestBtn = Theme.btnGhost("Browse Marketplace as Guest →");
        guestBtn.setAlignmentX(CENTER_ALIGNMENT);
        guestBtn.addActionListener(e -> MainFrame.get().show(MainFrame.BROWSE_JOBS));
        card.add(guestBtn);
        card.add(Box.createVerticalStrut(16));

        // Footer link
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        footer.setOpaque(false);
        footer.add(Theme.label("Don't have an account?", Theme.FONT_SMALL, Theme.MID_GRAY));
        JLabel signUp = Theme.label("Sign Up", Theme.FONT_SMALL, Theme.ACCENT);
        signUp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signUp.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                MainFrame.get().show(MainFrame.REGISTER);
            }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                signUp.setForeground(Theme.ACCENT_HOVER);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                signUp.setForeground(Theme.ACCENT);
            }
        });
        footer.add(signUp);
        card.add(footer);

        add(card);
    }

    private JPanel fieldGroup(String labelText, JTextComponent field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel lbl = Theme.label(labelText, Theme.FONT_SMALL, Theme.MID_GRAY);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));
        Theme.styleInput(field);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        p.add(lbl);
        p.add(field);
        return p;
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String pass  = new String(passwordField.getPassword()).trim();
        if (email.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Email and password are required.");
            return;
        }
        loginBtn.setEnabled(false);
        loginBtn.setText("Signing in...");
        errorLabel.setText("");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() throws Exception {
                String json = "{\"email\":" + ApiClient.esc(email) +
                              ",\"password\":" + ApiClient.esc(pass) + "}";
                return ApiClient.post("/api/users/login", json);
            }
            @Override protected void done() {
                loginBtn.setEnabled(true);
                loginBtn.setText("Sign In");
                try {
                    String resp = get();
                    Session.set(resp);
                    MainFrame.get().show(MainFrame.DASHBOARD);
                } catch (Exception ex) {
                    errorLabel.setText("Invalid email or password.");
                }
            }
        };
        worker.execute();
    }
}
