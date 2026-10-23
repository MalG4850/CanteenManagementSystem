package canteen.ui.components;

import canteen.ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class ActionButton extends JButton {

    public enum Variant { PRIMARY, SECONDARY, GHOST, OUTLINE, DANGER }

    private final Variant variant;
    private Color baseBg;
    private Color baseFg;
    private Color hoverBg;
    private Color hoverFg;
    private Color pressBg;

    public ActionButton(String text, Variant variant) {
        super(text);
        this.variant = variant;
        init();
    }

    public ActionButton(String text, Icon icon, Variant variant) {
        super(text, icon);
        this.variant = variant;
        init();
    }

    public ActionButton(Icon icon, Variant variant) {
        super(icon);
        this.variant = variant;
        init();
    }

    private void init() {
        setFont(new Font("Segoe UI", Font.PLAIN, 13));
        setFocusPainted(false);
        setBorder(new EmptyBorder(10, 20, 10, 20));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setContentAreaFilled(false);
        setOpaque(false);
        applyVariantColors();

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (isEnabled()) repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                if (isEnabled()) repaint();
            }
            @Override public void mousePressed(MouseEvent e) {
                if (isEnabled()) repaint();
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (isEnabled()) repaint();
            }
        });

        addChangeListener(e -> repaint());
    }

    private void applyVariantColors() {
        switch (variant) {
            case PRIMARY -> {
                baseBg = ThemeManager.getAccent();
                baseFg = Color.WHITE;
                hoverBg = ThemeManager.getAccentHover();
                hoverFg = Color.WHITE;
                pressBg = ThemeManager.getAccentHover().darker();
            }
            case SECONDARY -> {
                baseBg = ThemeManager.getBgTertiary();
                baseFg = ThemeManager.getTextPrimary();
                hoverBg = ThemeManager.getBgHover();
                hoverFg = ThemeManager.getTextPrimary();
                pressBg = ThemeManager.getBgHover().darker();
            }
            case GHOST -> {
                baseBg = new Color(0, 0, 0, 0);
                baseFg = ThemeManager.getAccent();
                hoverBg = ThemeManager.getAccentBg();
                hoverFg = ThemeManager.getAccent();
                pressBg = ThemeManager.getAccentBg().darker();
            }
            case OUTLINE -> {
                baseBg = new Color(0, 0, 0, 0);
                baseFg = ThemeManager.getTextPrimary();
                hoverBg = ThemeManager.getBgHover();
                hoverFg = ThemeManager.getTextPrimary();
                pressBg = ThemeManager.getBgHover().darker();
            }
            case DANGER -> {
                baseBg = ThemeManager.getError();
                baseFg = Color.WHITE;
                hoverBg = ThemeManager.getError().brighter();
                hoverFg = Color.WHITE;
                pressBg = ThemeManager.getError().darker();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = 8;

        Color bg, fg;
        ButtonModel model = getModel();

        if (!isEnabled()) {
            bg = ThemeManager.getBgTertiary();
            fg = ThemeManager.getTextMuted();
        } else if (model.isPressed() && model.isArmed()) {
            bg = pressBg;
            fg = hoverFg;
        } else if (model.isRollover()) {
            bg = hoverBg;
            fg = hoverFg;
        } else {
            bg = baseBg;
            fg = baseFg;
        }

        if (variant == ActionButton.Variant.OUTLINE) {
            g2.setColor(fg);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(1, 1, w - 3, h - 3, arc, arc));
        } else {
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
        }

        g2.dispose();

        setForeground(fg);
        super.paintComponent(g);
    }

    public Variant getVariant() { return variant; }
}