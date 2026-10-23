package canteen.ui.components;

import canteen.ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class RoundedPanel extends JPanel {

    private int arc = 12;
    private int shadowOffset = 3;
    private Color backgroundColor;
    private Color borderColor;
    private boolean drawShadow = true;
    private boolean drawBorder = false;

    public RoundedPanel() {
        this(ThemeManager.getBgSecondary());
    }

    public RoundedPanel(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        setOpaque(false);
        setBorder(new EmptyBorder(shadowOffset, 0, shadowOffset, shadowOffset));
        setLayout(new BorderLayout());
    }

    public RoundedPanel(LayoutManager layout) {
        this();
        setLayout(layout);
    }

    public RoundedPanel(LayoutManager layout, Color backgroundColor) {
        this(backgroundColor);
        setLayout(layout);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth() - shadowOffset;
        int h = getHeight() - shadowOffset;

        if (drawShadow) {
            Color shadow = ThemeManager.getShadow();
            g2.setColor(shadow);
            g2.fill(new RoundRectangle2D.Float(shadowOffset, shadowOffset, w, h, arc, arc));
        }

        g2.setColor(backgroundColor != null ? backgroundColor : ThemeManager.getBgSecondary());
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        if (drawBorder && borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));
        }

        g2.dispose();
        super.paintComponent(g);
    }

    public int getArc() { return arc; }
    public void setArc(int arc) { this.arc = arc; repaint(); }

    public int getShadowOffset() { return shadowOffset; }
    public void setShadowOffset(int shadowOffset) { this.shadowOffset = shadowOffset; setBorder(new EmptyBorder(shadowOffset, 0, shadowOffset, shadowOffset)); repaint(); }

    public boolean isDrawShadow() { return drawShadow; }
    public void setDrawShadow(boolean drawShadow) { this.drawShadow = drawShadow; repaint(); }

    public boolean isDrawBorder() { return drawBorder; }
    public void setDrawBorder(boolean drawBorder) { this.drawBorder = drawBorder; repaint(); }

    public Color getPanelBackgroundColor() { return backgroundColor; }
    public void setPanelBackgroundColor(Color backgroundColor) { this.backgroundColor = backgroundColor; repaint(); }

    public Color getBorderColor() { return borderColor; }
    public void setBorderColor(Color borderColor) { this.borderColor = borderColor; repaint(); }

    public void addHoverEffect() {
        Color originalBg = backgroundColor != null ? backgroundColor : ThemeManager.getBgSecondary();
        Color hoverBg = ThemeManager.getBgHover();

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (isEnabled()) setPanelBackgroundColor(hoverBg);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (isEnabled()) setPanelBackgroundColor(originalBg);
            }
        });
    }
}