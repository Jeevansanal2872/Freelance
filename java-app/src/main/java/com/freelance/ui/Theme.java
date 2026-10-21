package com.freelance.ui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Theme — Premium design system for Freelance .
 *
 * Palette: #00B2FF (light blue accent), white, obsidian black, neutral grays.
 * Glass effects, subtle depth shadows, smooth micro-animations, rounded corners.
 */
public final class Theme {

    private Theme() {}

    // ── Palette ──────────────────────────────────────────────────────────────
    public static final Color ACCENT       = new Color(0x00B2FF);           // primary accent #00B2FF
    public static final Color ACCENT_HOVER = new Color(0x009EE0);           // smooth hover
    public static final Color ACCENT_LIGHT = new Color(0x00B2FF, true);     // for subtle tints
    public static final Color ACCENT_10    = new Color(0x00, 0xB2, 0xFF, 0x1A); // 10% opacity
    public static final Color ACCENT_20    = new Color(0x00, 0xB2, 0xFF, 0x33); // 20% opacity

    public static final Color BLACK        = new Color(0x000000);
    public static final Color NEAR_BLACK   = new Color(0x1D1D1F);           // Obsidian near-black
    public static final Color DARK_GRAY    = new Color(0x333336);
    public static final Color MID_GRAY     = new Color(0x86868B);           // Slate mid-gray
    public static final Color LIGHT_GRAY   = new Color(0xD2D2D7);
    public static final Color SEPARATOR    = new Color(0xE8E8ED);
    public static final Color BG           = new Color(0xF5F5F7);           // Canvas background #F5F5F7
    public static final Color SURFACE      = Color.WHITE;
    public static final Color WHITE        = Color.WHITE;

    // Glass effect colours
    public static final Color GLASS_BG     = new Color(255, 255, 255, 215); // Translucent white
    public static final Color GLASS_BORDER = new Color(255, 255, 255, 140); // 55% white border
    public static final Color SHADOW_COLOR = new Color(0, 0, 0, 18);
    public static final Color HOVER_SHADOW = new Color(0, 178, 255, 30);

    // Status colours
    public static final Color GREEN        = new Color(0x34C759);
    public static final Color GREEN_DARK   = new Color(0x248A3D);
    public static final Color RED          = new Color(0xFF3B30);
    public static final Color RED_LIGHT    = new Color(0xFFF0F0);
    public static final Color RED_TEXT     = new Color(0xCC1100);
    public static final Color ORANGE       = new Color(0xFF9500);
    public static final Color ORANGE_LIGHT = new Color(0xFFF8EC);
    public static final Color ORANGE_TEXT  = new Color(0x99600A);
    public static final Color GREEN_LIGHT  = new Color(0xEAFBEF);
    public static final Color GREEN_TEXT   = new Color(0x1A7A34);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final String FONT_FAMILY = selectFont();

    public static final Font FONT_HERO     = new Font(FONT_FAMILY, Font.BOLD,   32);
    public static final Font FONT_TITLE    = new Font(FONT_FAMILY, Font.BOLD,   24);
    public static final Font FONT_H2       = new Font(FONT_FAMILY, Font.BOLD,   19);
    public static final Font FONT_H3       = new Font(FONT_FAMILY, Font.BOLD,   15);
    public static final Font FONT_BODY     = new Font(FONT_FAMILY, Font.PLAIN,  14);
    public static final Font FONT_SMALL    = new Font(FONT_FAMILY, Font.PLAIN,  12);
    public static final Font FONT_TINY     = new Font(FONT_FAMILY, Font.PLAIN,  11);
    public static final Font FONT_BOLD     = new Font(FONT_FAMILY, Font.BOLD,   13);
    public static final Font FONT_MONO     = new Font("Consolas", Font.PLAIN,   13);

    // ── Radii & Metrics ───────────────────────────────────────────────────────
    public static final int RADIUS       = 18;
    public static final int RADIUS_SM    = 10;
    public static final int RADIUS_LG    = 24;
    public static final int RADIUS_FULL  = 999;

    private static String selectFont() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] names = ge.getAvailableFontFamilyNames();
        for (String n : names) if (n.equals("SF Pro Display")) return n;
        for (String n : names) if (n.equals("SF Pro Text")) return n;
        for (String n : names) if (n.equals("Inter")) return n;
        for (String n : names) if (n.equals("Segoe UI")) return n;
        return Font.SANS_SERIF;
    }

    // ── Factories ─────────────────────────────────────────────────────────────
    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setOpaque(false);
        return l;
    }

    public static GlassPanel card() {
        return new GlassPanel(RADIUS);
    }

    public static RoundPanel solidCard() {
        RoundPanel p = new RoundPanel(RADIUS, SURFACE);
        p.setBorder(new RoundBorder(SEPARATOR, RADIUS, 1));
        return p;
    }

    public static FlatButton btnPrimary(String text) {
        return new FlatButton(text, ACCENT, WHITE, ACCENT_HOVER, RADIUS_SM);
    }

    public static FlatButton btnDark(String text) {
        return new FlatButton(text, NEAR_BLACK, WHITE, DARK_GRAY, RADIUS_SM);
    }

    public static FlatButton btnOutline(String text) {
        FlatButton b = new FlatButton(text, SURFACE, NEAR_BLACK, BG, RADIUS_SM);
        b.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(SEPARATOR, RADIUS_SM, 1),
            BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        return b;
    }

    public static FlatButton btnGhost(String text) {
        return new FlatButton(text, new Color(0, 0, 0, 0), ACCENT, ACCENT_10, RADIUS_SM);
    }

    public static FlatButton btnDanger(String text) {
        return new FlatButton(text, RED_LIGHT, RED_TEXT, new Color(0xFFDCD8), RADIUS_SM);
    }

    public static JLabel badge(String status) {
        Color bg, fg;
        switch (status.toUpperCase()) {
            case "OPEN", "ACTIVE", "ACCEPTED", "COMPLETED" -> { bg = GREEN_LIGHT; fg = GREEN_TEXT; }
            case "PENDING"                                  -> { bg = ORANGE_LIGHT; fg = ORANGE_TEXT; }
            case "CLOSED", "REJECTED", "CANCELLED",
                 "WITHDRAWN"                                -> { bg = RED_LIGHT; fg = RED_TEXT; }
            default                                         -> { bg = new Color(0xF0F0F5); fg = MID_GRAY; }
        }
        JLabel lbl = new JLabel(status) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(FONT_TINY);
        lbl.setForeground(fg);
        lbl.setBackground(bg);
        lbl.setOpaque(false);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        return lbl;
    }

    public static JLabel tag(String text) {
        JLabel lbl = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 178, 255, 18));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(new Color(0, 178, 255, 75));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(FONT_TINY);
        lbl.setForeground(ACCENT);
        lbl.setOpaque(false);
        lbl.setBorder(BorderFactory.createEmptyBorder(3, 9, 3, 9));
        return lbl;
    }

    public static JLabel successTag(String text) {
        JLabel lbl = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GREEN_LIGHT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(new Color(0x34, 0xC7, 0x59, 90));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(FONT_TINY);
        lbl.setForeground(GREEN_TEXT);
        lbl.setOpaque(false);
        lbl.setBorder(BorderFactory.createEmptyBorder(3, 9, 3, 9));
        return lbl;
    }

    public static JLabel warningTag(String text) {
        JLabel lbl = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ORANGE_LIGHT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(new Color(0xFF, 0x95, 0x00, 80));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(FONT_TINY);
        lbl.setForeground(ORANGE_TEXT);
        lbl.setOpaque(false);
        lbl.setBorder(BorderFactory.createEmptyBorder(3, 9, 3, 9));
        return lbl;
    }

    public static void styleInput(JTextComponent tf) {
        tf.setFont(FONT_BODY);
        tf.setForeground(NEAR_BLACK);
        tf.setBackground(SURFACE);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(LIGHT_GRAY, RADIUS_SM, 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        tf.setCaretColor(ACCENT);
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(ACCENT, RADIUS_SM, 2),
                    BorderFactory.createEmptyBorder(9, 13, 9, 13)
                ));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(LIGHT_GRAY, RADIUS_SM, 1),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)
                ));
            }
        });
    }

    public static void styleInput(JComboBox<?> cb) {
        cb.setFont(FONT_BODY);
        cb.setBackground(SURFACE);
        cb.setForeground(NEAR_BLACK);
        cb.setBorder(new RoundBorder(LIGHT_GRAY, RADIUS_SM, 1));
    }

    public static JSeparator separator() {
        JSeparator s = new JSeparator();
        s.setForeground(SEPARATOR);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    public static Component hGap(int px) { return Box.createHorizontalStrut(px); }
    public static Component vGap(int px) { return Box.createVerticalStrut(px); }

    public static JScrollPane scroll(Component view) {
        JScrollPane sp = new JScrollPane(view,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    public static JPanel avatar(String initials, int size, Color bg, Color fg) {
        JPanel p = new RoundPanel(size, bg) {
            @Override public Dimension getPreferredSize() { return new Dimension(size, size); }
            @Override public Dimension getMinimumSize() { return getPreferredSize(); }
            @Override public Dimension getMaximumSize() { return getPreferredSize(); }
        };
        JLabel l = label(initials, new Font(FONT_FAMILY, Font.BOLD, Math.max(12, size / 3)), fg);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setVerticalAlignment(SwingConstants.CENTER);
        p.setLayout(new BorderLayout());
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    public static String greeting() {
        int h = java.time.LocalTime.now().getHour();
        if (h < 12) return "Good morning";
        if (h < 18) return "Good afternoon";
        return "Good evening";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  CUSTOM COMPONENTS WITH SMOOTH ANIMATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Frosted-glass panel with subtle depth shadow, translucent fill,
     * and optional interactive hover lift.
     */
    public static class GlassPanel extends JPanel {
        private final int arc;
        private boolean hoverLiftEnabled = false;
        private float hoverTransition = 0f;
        private Timer hoverTimer;

        public GlassPanel(int arc) {
            this.arc = arc;
            setOpaque(false);
            setLayout(new BorderLayout());
        }

        public void enableHoverLift() {
            if (hoverLiftEnabled) return;
            this.hoverLiftEnabled = true;
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { animateHover(true); }
                @Override public void mouseExited(MouseEvent e)  { animateHover(false); }
            });
        }

        private void animateHover(boolean forward) {
            if (hoverTimer != null && hoverTimer.isRunning()) hoverTimer.stop();
            hoverTimer = new Timer(15, e -> {
                if (forward) {
                    hoverTransition = Math.min(1.0f, hoverTransition + 0.15f);
                    if (hoverTransition >= 1.0f) ((Timer) e.getSource()).stop();
                } else {
                    hoverTransition = Math.max(0.0f, hoverTransition - 0.15f);
                    if (hoverTransition <= 0.0f) ((Timer) e.getSource()).stop();
                }
                repaint();
            });
            hoverTimer.start();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int lift = (int) (hoverTransition * 2.5f);

            // Shadow
            if (hoverLiftEnabled && hoverTransition > 0.01f) {
                g2.setColor(new Color(0, 178, 255, (int)(hoverTransition * 28)));
                g2.fill(new RoundRectangle2D.Float(0, 4 - lift, getWidth(), getHeight(), arc + 4, arc + 4));
            } else {
                g2.setColor(SHADOW_COLOR);
                g2.fill(new RoundRectangle2D.Float(0, 3, getWidth(), getHeight(), arc + 2, arc + 2));
            }

            // Glass background
            g2.setColor(GLASS_BG);
            g2.fill(new RoundRectangle2D.Float(0, -lift, getWidth(), getHeight() - 2, arc, arc));

            // Glass border
            Color borderColor = hoverLiftEnabled && hoverTransition > 0.01f
                ? blend(GLASS_BORDER, ACCENT, hoverTransition * 0.4f)
                : GLASS_BORDER;
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f - lift, getWidth() - 1, getHeight() - 3, arc, arc));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Solid rounded container panel.
     */
    public static class RoundPanel extends JPanel {
        private final int arc;
        private Color bg;
        public RoundPanel(int arc, Color bg) {
            this.arc = arc; this.bg = bg;
            setOpaque(false);
            setLayout(new BorderLayout());
        }
        public void setBg(Color bg) { this.bg = bg; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Button with smooth micro-animated color blending on mouse hover & press.
     */
    public static class FlatButton extends JButton {
        private Color normal, hover;
        private Color currentBg;
        private final int arc;
        private Timer animTimer;
        private float blendRatio = 0f;

        public FlatButton(String text, Color bg, Color fg, Color hoverBg, int arc) {
            super(text);
            this.normal = bg; this.hover = hoverBg; this.currentBg = bg;
            this.arc = arc;
            setFont(FONT_BOLD);
            setForeground(fg);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) startFade(1.0f);
                }
                @Override public void mouseExited(MouseEvent e) {
                    if (isEnabled()) startFade(0.0f);
                }
                @Override public void mousePressed(MouseEvent e) {
                    if (isEnabled()) {
                        currentBg = darker(hover, 0.88f);
                        repaint();
                    }
                }
                @Override public void mouseReleased(MouseEvent e) {
                    if (isEnabled()) {
                        currentBg = hover;
                        repaint();
                    }
                }
            });
        }

        private void startFade(float target) {
            if (animTimer != null && animTimer.isRunning()) animTimer.stop();
            animTimer = new Timer(15, ev -> {
                if (blendRatio < target) {
                    blendRatio = Math.min(target, blendRatio + 0.2f);
                    if (blendRatio >= target) ((Timer) ev.getSource()).stop();
                } else {
                    blendRatio = Math.max(target, blendRatio - 0.2f);
                    if (blendRatio <= target) ((Timer) ev.getSource()).stop();
                }
                currentBg = blend(normal, hover, blendRatio);
                repaint();
            });
            animTimer.start();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isEnabled() ? currentBg : new Color(0xE0E0E5));
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }

        public void setActive(boolean active) {
            if (active) {
                this.normal = NEAR_BLACK;
                this.hover = DARK_GRAY;
                this.currentBg = NEAR_BLACK;
                setForeground(WHITE);
                setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
            } else {
                this.normal = SURFACE;
                this.hover = BG;
                this.currentBg = SURFACE;
                setForeground(NEAR_BLACK);
                setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(SEPARATOR, arc, 1),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
                ));
            }
            repaint();
        }

        private static Color darker(Color c, float factor) {
            return new Color(
                Math.max((int)(c.getRed() * factor), 0),
                Math.max((int)(c.getGreen() * factor), 0),
                Math.max((int)(c.getBlue() * factor), 0),
                c.getAlpha()
            );
        }
    }

    /**
     * AnimatedProgressBar — Glides smoothly from 0% to the target score.
     */
    public static class AnimatedProgressBar extends JPanel {
        private float currentProgress = 0f;
        private float targetProgress = 0f;
        private final Color trackColor;
        private final Color fillColor;
        private final int heightPx;
        private Timer animationTimer;

        public AnimatedProgressBar(int heightPx, Color fillColor, Color trackColor) {
            this.heightPx = heightPx;
            this.fillColor = fillColor;
            this.trackColor = trackColor;
            setOpaque(false);
            setPreferredSize(new Dimension(200, heightPx));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, heightPx));
        }

        public void setProgressAnimated(int targetPercent) {
            this.targetProgress = Math.max(0, Math.min(100, targetPercent));
            if (animationTimer != null && animationTimer.isRunning()) animationTimer.stop();

            animationTimer = new Timer(15, e -> {
                float diff = targetProgress - currentProgress;
                if (Math.abs(diff) < 0.6f) {
                    currentProgress = targetProgress;
                    ((Timer) e.getSource()).stop();
                } else {
                    currentProgress += diff * 0.16f;
                }
                repaint();
            });
            animationTimer.start();
        }

        public void setProgressImmediate(int percent) {
            this.currentProgress = Math.max(0, Math.min(100, percent));
            this.targetProgress = currentProgress;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            // Track
            g2.setColor(trackColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));

            // Fill
            int fillW = (int) (w * (currentProgress / 100.0f));
            if (fillW > 0) {
                g2.setColor(fillColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, fillW, h, h, h));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Rounded border with configurable colour, arc, and thickness. */
    public static class RoundBorder extends AbstractBorder {
        private final Color color;
        private final int arc, thickness;
        public RoundBorder(Color color, int arc, int thickness) {
            this.color = color; this.arc = arc; this.thickness = thickness;
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            if (thickness == 0) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(
                x + thickness / 2f, y + thickness / 2f,
                w - thickness, h - thickness, arc, arc));
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }
        @Override public boolean isBorderOpaque() { return false; }
    }

    /** Gradient panel for hero sections. */
    public static class GradientPanel extends JPanel {
        private final Color start, end;
        private final boolean horizontal;
        public GradientPanel(Color start, Color end, boolean horizontal) {
            this.start = start; this.end = end; this.horizontal = horizontal;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = horizontal
                ? new GradientPaint(0, 0, start, getWidth(), 0, end)
                : new GradientPaint(0, 0, start, 0, getHeight(), end);
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), RADIUS_LG, RADIUS_LG));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Color blending helper ─────────────────────────────────────────────────
    public static Color blend(Color c1, Color c2, float ratio) {
        float r = Math.max(0f, Math.min(1f, ratio));
        float ir = 1.0f - r;
        int red = (int) (c1.getRed() * ir + c2.getRed() * r);
        int green = (int) (c1.getGreen() * ir + c2.getGreen() * r);
        int blue = (int) (c1.getBlue() * ir + c2.getBlue() * r);
        int alpha = (int) (c1.getAlpha() * ir + c2.getAlpha() * r);
        return new Color(red, green, blue, alpha);
    }
}
