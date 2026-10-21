package com.freelance.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * NavBar — Frosted modern navigation bar.
 * Semi-transparent white with subtle bottom separator.
 */
public class NavBar extends JPanel {

    private static final int HEIGHT = 56;

    public NavBar() {
        setPreferredSize(new Dimension(0, HEIGHT));
        setOpaque(false);
        setLayout(new BorderLayout());
        refresh();
    }

    public void refresh() {
        removeAll();

        // ── Brand ─────────────────────────────────────────────────────────────
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        brandPanel.setOpaque(false);

        // Brand dot (accent circle)
        JPanel dot = new Theme.RoundPanel(20, Theme.ACCENT) {
            @Override public Dimension getPreferredSize() { return new Dimension(10, 10); }
            @Override public Dimension getMaximumSize() { return getPreferredSize(); }
        };
        brandPanel.add(Box.createHorizontalStrut(8));

        JLabel brand = new JLabel("Freelance .");
        brand.setFont(new Font(Theme.FONT_H3.getFamily(), Font.BOLD, 18));
        brand.setForeground(Theme.NEAR_BLACK);
        brand.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        brand.setIcon(createAccentDot());
        brand.setIconTextGap(8);
        brand.addMouseListener(click(() -> {
            if (Session.isLoggedIn()) MainFrame.get().show(MainFrame.DASHBOARD);
            else                     MainFrame.get().show(MainFrame.LOGIN);
        }));
        brandPanel.add(brand);
        add(brandPanel, BorderLayout.WEST);

        // ── Links ─────────────────────────────────────────────────────────────
        JPanel links = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        links.setOpaque(false);
        links.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));

        if (!Session.isLoggedIn()) {
            links.add(navLink("Browse Jobs", () -> MainFrame.get().show(MainFrame.BROWSE_JOBS)));
            links.add(navLink("Login",       () -> MainFrame.get().show(MainFrame.LOGIN)));
            links.add(Box.createHorizontalStrut(4));
            links.add(navBtnPrimary("Sign Up", () -> MainFrame.get().show(MainFrame.REGISTER)));
        } else {
            links.add(navLink("Browse Jobs", () -> MainFrame.get().show(MainFrame.BROWSE_JOBS)));
            if (Session.isClient()) {
                links.add(navLink("Post Job", () -> MainFrame.get().show(MainFrame.POST_JOB)));
            } else {
                links.add(navLink("My Proposals", () -> MainFrame.get().show(MainFrame.MY_PROPOSALS)));
            }
            links.add(navLink("Contracts", () -> MainFrame.get().show(MainFrame.MY_CONTRACTS)));
            links.add(navLink("Dashboard", () -> MainFrame.get().show(MainFrame.DASHBOARD)));
            links.add(navLink("Profile",   () -> MainFrame.get().show(MainFrame.PROFILE)));
            links.add(Box.createHorizontalStrut(8));
            links.add(Theme.badge(Session.role != null ? Session.role : "USER"));
            links.add(Box.createHorizontalStrut(6));
            links.add(navBtnDark("Log Out", () -> {
                Session.clear();
                MainFrame.get().show(MainFrame.LOGIN);
            }));
        }
        add(links, BorderLayout.EAST);

        revalidate();
        repaint();
    }

    // ── Paint ─────────────────────────────────────────────────────────────────
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Frosted white background (translucent glass style)
        g2.setColor(new Color(255, 255, 255, 235));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Subtle bottom line
        g2.setColor(Theme.SEPARATOR);
        g2.fillRect(0, getHeight() - 1, getWidth(), 1);

        g2.dispose();
        super.paintComponent(g);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JLabel navLink(String text, Runnable action) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BODY);
        l.setForeground(Theme.MID_GRAY);
        l.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        l.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { l.setForeground(Theme.NEAR_BLACK); }
            @Override public void mouseExited(MouseEvent e)  { l.setForeground(Theme.MID_GRAY); }
            @Override public void mouseClicked(MouseEvent e)  { action.run(); }
        });
        return l;
    }

    private Theme.FlatButton navBtnPrimary(String text, Runnable action) {
        Theme.FlatButton b = Theme.btnPrimary(text);
        b.setFont(Theme.FONT_SMALL);
        b.setBorder(BorderFactory.createEmptyBorder(7, 18, 7, 18));
        b.addActionListener(e -> action.run());
        return b;
    }

    private Theme.FlatButton navBtnDark(String text, Runnable action) {
        Theme.FlatButton b = Theme.btnOutline(text);
        b.setFont(Theme.FONT_SMALL);
        b.addActionListener(e -> action.run());
        return b;
    }

    private MouseAdapter click(Runnable r) {
        return new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { r.run(); }
        };
    }

    /** Small accent-colour dot icon for the brand. */
    private Icon createAccentDot() {
        int sz = 10;
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.ACCENT);
                g2.fillOval(x, y, sz, sz);
                g2.dispose();
            }
            @Override public int getIconWidth()  { return sz; }
            @Override public int getIconHeight() { return sz; }
        };
    }
}
