package canteen.ui.dashboard;

import canteen.ui.components.RoundedPanel;
import canteen.ui.components.StatusBadge;
import canteen.ui.components.ActionButton;
import canteen.ui.components.WrapLayout;
import canteen.ui.theme.ThemeManager;
import canteen.Order;
import canteen.OrderService;
import canteen.OrderStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class KitchenDisplayPanel extends JPanel {

    private OrderService orderService;
    private JPanel ordersGrid;
    private JScrollPane scrollPane;
    private FilterTabGroup filterTabs;
    private JLabel emptyLabel;
    private OrderStatus currentFilter = null;
    private java.util.Timer autoRefreshTimer;

    public KitchenDisplayPanel(OrderService orderService) {
        this.orderService = orderService;
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents();
        loadOrders();
        startAutoRefresh();
    }

    private void initComponents() {
        // Header with title and filter tabs
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel titleLabel = new JLabel("🍽️ Kitchen Display");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(ThemeManager.getTextPrimary());

        filterTabs = new FilterTabGroup(status -> {
            currentFilter = status;
            loadOrders();
        });

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(filterTabs, BorderLayout.EAST);

        // Orders Grid with WrapLayout - larger cards for kitchen view
        ordersGrid = new JPanel(new WrapLayout(20, 20));
        ordersGrid.setOpaque(false);

        scrollPane = new JScrollPane(ordersGrid);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));

        // Empty state
        emptyLabel = new JLabel("No orders in kitchen");
        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        emptyLabel.setForeground(ThemeManager.getTextMuted());
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emptyLabel.setBorder(new EmptyBorder(100, 20, 100, 20));

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadOrders() {
        OrderStatus filter = currentFilter;
        new SwingWorker<List<Order>, Void>() {
            @Override
            protected List<Order> doInBackground() {
                if (filter == null) {
                    List<Order> list = new java.util.ArrayList<>();
                    list.addAll(orderService.getPendingOrders());
                    list.addAll(orderService.getPreparingOrders());
                    list.addAll(orderService.getReadyOrders());
                    return list;
                } else {
                    return switch (filter) {
                        case PENDING -> orderService.getPendingOrders();
                        case PREPARING -> orderService.getPreparingOrders();
                        case READY -> orderService.getReadyOrders();
                        default -> java.util.Collections.emptyList();
                    };
                }
            }

            @Override
            protected void done() {
                try {
                    List<Order> orders = get();
                    ordersGrid.removeAll();

                    if (orders.isEmpty()) {
                        ordersGrid.add(emptyLabel);
                    } else {
                        for (Order order : orders) {
                            KitchenOrderCard card = new KitchenOrderCard(order, KitchenDisplayPanel.this::onOrderAction);
                            ordersGrid.add(card);
                        }
                    }

                    ordersGrid.revalidate();
                    ordersGrid.repaint();
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void onOrderAction(Order order, OrderStatus nextStatus) {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return orderService.advanceStatus(order.getOrderId(), order.getStatus(), nextStatus);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        loadOrders();
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    public void refresh() { loadOrders(); }

    private void startAutoRefresh() {
        autoRefreshTimer = new Timer("KitchenDisplay-AutoRefresh", true);
        autoRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> loadOrders());
            }
        }, 5000, 5000); // Every 5 seconds for kitchen
    }

    public void stopAutoRefresh() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.cancel();
            autoRefreshTimer = null;
        }
    }

    // Kitchen-specific order card - larger, more prominent
    private static class KitchenOrderCard extends RoundedPanel {
        private final Order order;
        private final OrderCard.OrderCardListener listener;

        public KitchenOrderCard(Order order, OrderCard.OrderCardListener listener) {
            super(new BorderLayout(0, 0));
            this.order = order;
            this.listener = listener;

            setPanelBackgroundColor(ThemeManager.getBgSecondary());
            setArc(16);
            setShadowOffset(4);
            setDrawShadow(true);
            setDrawBorder(true);
            setBorderColor(getStatusBorderColor(order.getStatus()));
            setBorder(new EmptyBorder(20, 20, 20, 20));
            setPreferredSize(new Dimension(380, 0));

            initComponents();
        }

        private Color getStatusBorderColor(OrderStatus status) {
            return switch (status) {
                case PENDING -> ThemeManager.getWarning();
                case PREPARING -> ThemeManager.getInfo();
                case READY -> ThemeManager.getSuccess();
                default -> ThemeManager.getBorderLight();
            };
        }

        private void initComponents() {
            // Header: Order ID, Status Badge, Time
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setOpaque(false);

            JLabel orderIdLabel = new JLabel("#" + order.getOrderId());
            orderIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
            orderIdLabel.setForeground(ThemeManager.getTextPrimary());

            StatusBadge statusBadge = new StatusBadge(order.getStatus());
            statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
            statusBadge.setBorder(new EmptyBorder(6, 14, 6, 14));

            JLabel timeLabel = new JLabel(order.getFormattedOrderDate());
            timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            timeLabel.setForeground(ThemeManager.getTextMuted());

            JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            leftHeader.setOpaque(false);
            leftHeader.add(orderIdLabel);
            leftHeader.add(statusBadge);

            headerPanel.add(leftHeader, BorderLayout.WEST);
            headerPanel.add(timeLabel, BorderLayout.EAST);

            // Customer Info
            JPanel customerPanel = new JPanel(new BorderLayout());
            customerPanel.setOpaque(false);
            customerPanel.setBorder(new EmptyBorder(12, 0, 12, 0));

            JLabel customerLabel = new JLabel("👤 " + order.getCustomerName());
            customerLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            customerLabel.setForeground(ThemeManager.getTextPrimary());

            JLabel mobileLabel = new JLabel("📞 " + order.getCustomerMobile());
            mobileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            mobileLabel.setForeground(ThemeManager.getTextSecondary());

            JPanel customerInfo = new JPanel();
            customerInfo.setLayout(new BoxLayout(customerInfo, BoxLayout.Y_AXIS));
            customerInfo.setOpaque(false);
            customerInfo.add(customerLabel);
            customerInfo.add(Box.createRigidArea(new Dimension(0, 4)));
            customerInfo.add(mobileLabel);

            customerPanel.add(customerInfo, BorderLayout.WEST);

            // Items - larger for kitchen visibility
            JPanel itemsPanel = new JPanel();
            itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
            itemsPanel.setOpaque(false);
            itemsPanel.setBorder(new EmptyBorder(8, 0, 8, 0));

            for (canteen.OrderItem item : order.getItems()) {
                JPanel itemRow = new JPanel(new BorderLayout());
                itemRow.setOpaque(false);
                itemRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                itemRow.setBorder(new EmptyBorder(4, 0, 4, 0));

                JLabel itemName = new JLabel("  " + item.getItemName() + "  x" + item.getQuantity());
                itemName.setFont(new Font("Segoe UI", Font.BOLD, 14));
                itemName.setForeground(ThemeManager.getTextPrimary());

                java.text.NumberFormat fmt = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("en", "IN"));
                JLabel itemTotal = new JLabel(fmt.format(item.getLineTotal()) + "  ");
                itemTotal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                itemTotal.setForeground(ThemeManager.getTextSecondary());

                itemRow.add(itemName, BorderLayout.WEST);
                itemRow.add(itemTotal, BorderLayout.EAST);
                itemsPanel.add(itemRow);
            }

            // Footer: Total + Large Action Button
            JPanel footerPanel = new JPanel(new BorderLayout());
            footerPanel.setOpaque(false);
            footerPanel.setBorder(new EmptyBorder(12, 0, 0, 0));

            java.text.NumberFormat currencyFmt = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("en", "IN"));
            JLabel totalLabel = new JLabel("Total: " + currencyFmt.format(order.getTotalAmount()));
            totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            totalLabel.setForeground(ThemeManager.getTextPrimary());

            OrderStatus nextStatus = getNextStatus(order.getStatus());
            ActionButton actionBtn = null;
            if (nextStatus != null) {
                actionBtn = new ActionButton(getActionButtonText(nextStatus), ActionButton.Variant.PRIMARY);
                actionBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
                actionBtn.setPreferredSize(new Dimension(200, 48));
                actionBtn.addActionListener(e -> {
                    if (listener != null) listener.onAction(order, nextStatus);
                });
            } else {
                JLabel servedLabel = new JLabel("✓ SERVED");
                servedLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                servedLabel.setForeground(ThemeManager.getSuccess());
                footerPanel.add(servedLabel, BorderLayout.EAST);
            }

            footerPanel.add(totalLabel, BorderLayout.WEST);
            if (actionBtn != null) footerPanel.add(actionBtn, BorderLayout.EAST);

            // Assemble
            JPanel centerContent = new JPanel();
            centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
            centerContent.setOpaque(false);
            centerContent.add(headerPanel);
            centerContent.add(customerPanel);
            centerContent.add(itemsPanel);
            centerContent.add(Box.createVerticalGlue());
            centerContent.add(footerPanel);

            add(centerContent, BorderLayout.CENTER);
        }

        private OrderStatus getNextStatus(OrderStatus current) {
            return switch (current) {
                case PENDING -> OrderStatus.PREPARING;
                case PREPARING -> OrderStatus.READY;
                case READY -> OrderStatus.SERVED;
                default -> null;
            };
        }

        private String getActionButtonText(OrderStatus nextStatus) {
            return switch (nextStatus) {
                case PREPARING -> "▶ Start Preparing";
                case READY -> "✓ Mark Ready";
                case SERVED -> "✓ Mark Served";
                default -> "Next";
            };
        }
    }
}