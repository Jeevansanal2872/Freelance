package com.freelance.ui;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * RegisterPanel — Modern sign-up form with role selector.
 * Glass card, accent-blue CTA, clean minimal fields.
 */
public class RegisterPanel extends JPanel {

    private final JTextField     nameField     = new JTextField(28);
    private final JTextField     emailField    = new JTextField(28);
    private final JPasswordField passwordField = new JPasswordField(28);
    private final JComboBox<String> roleBox    = new JComboBox<>(new String[]{
        "Hire Freelancers (CLIENT)", "Find Work & Pitch (FREELANCER)"
    });
    private final Theme.FlatButton  submitBtn  = Theme.btnPrimary("Create Account");
    private final JLabel            errorLabel = Theme.label("", Theme.FONT_SMALL, Theme.RED);

    public RegisterPanel() {
        setBackground(Theme.BG);
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        Theme.GlassPanel card = Theme.card();
        card.setPreferredSize(new Dimension(460, 600));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 44, 40, 44));

        JLabel title = Theme.label("Create your account.", Theme.FONT_TITLE, Theme.NEAR_BLACK);
        title.setAlignmentX(CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(6));

        JLabel sub = Theme.label("Join the next-generation marketplace for creative craft", Theme.FONT_SMALL, Theme.MID_GRAY);
        sub.setAlignmentX(CENTER_ALIGNMENT);
        card.add(sub);
        card.add(Box.createVerticalStrut(26));

        card.add(fieldGroup("Full Name", nameField));
        card.add(Box.createVerticalStrut(14));
        card.add(fieldGroup("Email address", emailField));
        card.add(Box.createVerticalStrut(14));
        card.add(fieldGroup("Password", passwordField));
        card.add(Box.createVerticalStrut(14));

        // Role selector
        JPanel roleGroup = new JPanel();
        roleGroup.setLayout(new BoxLayout(roleGroup, BoxLayout.Y_AXIS));
        roleGroup.setOpaque(false);
        JLabel roleLbl = Theme.label("I want to...", Theme.FONT_SMALL, Theme.MID_GRAY);
        roleLbl.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));
        Theme.styleInput(roleBox);
        roleBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        roleGroup.add(roleLbl);
        roleGroup.add(roleBox);
        card.add(roleGroup);
        card.add(Box.createVerticalStrut(10));

        errorLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(14));

        submitBtn.setAlignmentX(CENTER_ALIGNMENT);
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        submitBtn.addActionListener(e -> doRegister());
        card.add(submitBtn);
        card.add(Box.createVerticalStrut(20));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        footer.setOpaque(false);
        footer.add(Theme.label("Already have an account?", Theme.FONT_SMALL, Theme.MID_GRAY));
        JLabel signIn = Theme.label("Sign In", Theme.FONT_SMALL, Theme.ACCENT);
        signIn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signIn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                MainFrame.get().show(MainFrame.LOGIN);
            }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                signIn.setForeground(Theme.ACCENT_HOVER);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                signIn.setForeground(Theme.ACCENT);
            }
        });
        footer.add(signIn);
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

    private void doRegister() {
        String name  = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass  = new String(passwordField.getPassword()).trim();
        String roleRaw = roleBox.getSelectedIndex() == 0 ? "CLIENT" : "FREELANCER";

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("All fields are required.");
            return;
        }
        if (pass.length() < 6) {
            errorLabel.setText("Password must be at least 6 characters.");
            return;
        }

        submitBtn.setEnabled(false);
        submitBtn.setText("Creating account...");
        errorLabel.setText("");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() throws Exception {
                String json = "{\"name\":" + ApiClient.esc(name) +
                              ",\"email\":" + ApiClient.esc(email) +
                              ",\"password\":" + ApiClient.esc(pass) +
                              ",\"role\":\"" + roleRaw + "\"}";
                return ApiClient.post("/api/users/register", json);
            }
            @Override protected void done() {
                submitBtn.setEnabled(true);
                submitBtn.setText("Create Account");
                try {
                    String resp = get();
                    Session.set(resp);
                    MainFrame.get().show(MainFrame.DASHBOARD);
                } catch (Exception ex) {
                    errorLabel.setText("Registration failed. Email may already be in use.");
                }
            }
        };
        worker.execute();
    }
}
