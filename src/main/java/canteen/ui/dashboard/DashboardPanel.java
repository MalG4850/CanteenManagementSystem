package canteen.ui.dashboard;

import canteen.ui.components.RoundedPanel;
import canteen.ui.theme.ThemeManager;
import canteen.Order;
import canteen.OrderService;
import canteen.OrderStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DashboardPanel extends JPanel {

    private OrderService orderService;
    private MetricCardsPanel metricCards;
    private SplitContentPanel splitContent;
    private JButton refreshBtn;
    private JButton themeToggleBtn;
    private Timer autoRefreshTimer;

    public DashboardPanel(OrderService orderService) {
        this.orderService = orderService;
        setLayout(new GridBagLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.weightx = 1.0;

        initComponents(gbc);
        startAutoRefresh();
        loadInitialData();
    }

    private void initComponents(GridBagConstraints gbc) {
        // Row 0: Metric Cards
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.NORTH;

        metricCards = new MetricCardsPanel();
        add(metricCards, gbc);

        // Row 1: Split Content (Active Orders + Side Panel)
        gbc.gridy = 1;
        gbc.weighty = 1.0;

        splitContent = new SplitContentPanel(orderService, this::onOrderAction);
        add(splitContent, gbc);

        // Bottom buttons: Theme toggle + Refresh
        refreshBtn = new JButton("⟳ Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.setForeground(ThemeManager.getAccent());
        refreshBtn.setBackground(ThemeManager.getBgSecondary());
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getAccent()),
            new EmptyBorder(8, 16, 8, 16)
        ));
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshAll());

        themeToggleBtn = new JButton(ThemeManager.getTheme() == ThemeManager.Theme.DARK ? "☀️ Light" : "🌙 Dark");
        themeToggleBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        themeToggleBtn.setForeground(ThemeManager.getTextPrimary());
        themeToggleBtn.setBackground(ThemeManager.getBgSecondary());
        themeToggleBtn.setFocusPainted(false);
        themeToggleBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorderMedium()),
            new EmptyBorder(8, 16, 8, 16)
        ));
        themeToggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        themeToggleBtn.addActionListener(e -> toggleTheme());

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(themeToggleBtn);
        buttonWrapper.add(refreshBtn);

        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.insets = new Insets(16, 0, 0, 0);
        add(buttonWrapper, gbc);
    }

    private void onOrderAction(Order order, OrderStatus nextStatus) {
        boolean success = orderService.advanceStatus(order.getOrderId(), order.getStatus(), nextStatus);
        if (success) {
            refreshAll();
            // Show toast notification
            showToast("Order #" + order.getOrderId() + " → " + nextStatus.getDisplayName());
        } else {
            showToast("Failed to update order status", true);
        }
    }

    private void loadInitialData() {
        SwingUtilities.invokeLater(this::refreshMetrics);
    }

    private void refreshMetrics() {
        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() {
                double dailyRevenue = orderService.getDailyRevenue();
                int dailyOrders = orderService.getDailyOrderCount();
                int pendingCount = orderService.getPendingOrders().size();
                return new Object[]{dailyRevenue, dailyOrders, pendingCount};
            }

            @Override
            protected void done() {
                try {
                    Object[] res = get();
                    double dailyRevenue = (Double) res[0];
                    int dailyOrders = (Integer) res[1];
                    int pendingCount = (Integer) res[2];
                    java.text.NumberFormat fmt = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("en", "IN"));

                    metricCards.updateTotalSales(fmt.format(dailyRevenue), "+" + dailyOrders + " orders today");
                    metricCards.updatePendingOrders(String.valueOf(pendingCount), pendingCount > 0 ? pendingCount + " active" : "All clear");
                    metricCards.updateTodayRevenue(fmt.format(dailyRevenue), "+0% vs yesterday");
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    public void refreshAll() {
        refreshMetrics();
        splitContent.refresh();
    }

    private void startAutoRefresh() {
        autoRefreshTimer = new Timer(30000, e -> refreshAll());
        autoRefreshTimer.setRepeats(true);
        autoRefreshTimer.start();
    }

    public void stopAutoRefresh() {
        if (autoRefreshTimer != null) autoRefreshTimer.stop();
    }

    private void toggleTheme() {
        ThemeManager.toggleTheme();
        boolean isDark = ThemeManager.getTheme() == ThemeManager.Theme.DARK;
        themeToggleBtn.setText(isDark ? "☀️ Light" : "🌙 Dark");
        ThemeManager.refreshAllFrames();
    }

    private void showToast(String message) { showToast(message, false); }
    private void showToast(String message, boolean isError) {
        // Simple JOptionPane for now - could be replaced with a custom toast component
        if (isError) {
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            // Could implement a non-blocking toast later
            System.out.println("[TOAST] " + message);
        }
    }
}