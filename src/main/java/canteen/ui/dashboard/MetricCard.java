package canteen.ui.dashboard;

import canteen.ui.components.RoundedPanel;
import canteen.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class MetricCard extends RoundedPanel {

    private final JLabel iconLabel;
    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel trendLabel;
    private final String title;
    private final String icon;

    public MetricCard(String title, String icon, String value, String trend, Color trendColor) {
        super(new BorderLayout(0, 8), ThemeManager.getBgSecondary());
        this.title = title;
        this.icon = icon;

        setBorder(new javax.swing.border.EmptyBorder(16, 20, 16, 20));
        setPreferredSize(new Dimension(0, 120));

        // Icon
        iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconLabel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 8, 0));

        // Title
        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(ThemeManager.getTextSecondary());

        // Value
        valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(ThemeManager.getTextPrimary());

        // Trend
        trendLabel = new JLabel(trend);
        trendLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        trendLabel.setForeground(trendColor != null ? trendColor : ThemeManager.getTextMuted());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(iconLabel, BorderLayout.WEST);
        topPanel.add(titleLabel, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(valueLabel, BorderLayout.CENTER);
        centerPanel.add(trendLabel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void setValue(String value) { valueLabel.setText(value); }
    public void setTrend(String trend, Color color) { trendLabel.setText(trend); trendLabel.setForeground(color); }
    public void setTitle(String title) { titleLabel.setText(title); }

    public String getTitle() { return title; }
    public String getIcon() { return icon; }
}