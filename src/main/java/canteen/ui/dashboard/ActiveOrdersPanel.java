package canteen.ui.dashboard;

import canteen.ui.components.RoundedPanel;
import canteen.ui.components.WrapLayout;
import canteen.ui.theme.ThemeManager;
import canteen.Order;
import canteen.OrderService;
import canteen.OrderStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ActiveOrdersPanel extends JPanel {

    private OrderService orderService;
    private OrderCard.OrderCardListener cardListener;
    private FilterTabGroup filterTabs;
    private JPanel ordersGrid;
    private JScrollPane scrollPane;
    private JLabel emptyLabel;
    private OrderStatus currentFilter = null;

    public ActiveOrdersPanel(OrderService orderService, OrderCard.OrderCardListener cardListener) {
        this.orderService = orderService;
        this.cardListener = cardListener;
        setLayout(new BorderLayout());
        setOpaque(false);

        initComponents();
        loadOrders();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel titleLabel = new JLabel("Active Orders");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ThemeManager.getTextPrimary());

        filterTabs = new FilterTabGroup(status -> {
            currentFilter = status;
            loadOrders();
        });

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(filterTabs, BorderLayout.EAST);

        // Orders Grid with WrapLayout
        ordersGrid = new JPanel(new WrapLayout(16, 16));
        ordersGrid.setOpaque(false);

        scrollPane = new JScrollPane(ordersGrid);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

        // Empty state
        emptyLabel = new JLabel("No orders found");
        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emptyLabel.setForeground(ThemeManager.getTextMuted());
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emptyLabel.setBorder(new EmptyBorder(60, 20, 60, 20));

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(scrollPane, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);
    }

    public void loadOrders() {
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
                            OrderCard card = new OrderCard(order, cardListener);
                            ordersGrid.add(card);
                        }
                    }

                    ordersGrid.revalidate();
                    ordersGrid.repaint();
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    public void refresh() { loadOrders(); }
    public FilterTabGroup getFilterTabs() { return filterTabs; }
}