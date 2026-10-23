package canteen.ui.dashboard;

import canteen.ui.components.RoundedPanel;
import canteen.ui.components.StatusBadge;
import canteen.ui.components.ActionButton;
import canteen.ui.components.SearchField;
import canteen.ui.theme.ThemeManager;
import canteen.Order;
import canteen.OrderService;
import canteen.OrderStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class OrdersPanel extends JPanel {

    private OrderService orderService;
    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private SearchField searchField;
    private JComboBox<String> statusFilter;
    private JButton refreshBtn;
    private NumberFormat currencyFmt;
    private java.util.Timer autoRefreshTimer;

    public OrdersPanel(OrderService orderService) {
        this.orderService = orderService;
        this.currencyFmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents();
        loadOrders();
        startAutoRefresh();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel titleLabel = new JLabel("🧾 Orders History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(ThemeManager.getTextPrimary());

        // Toolbar with search, filter, refresh
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        toolbar.setOpaque(false);

        searchField = new SearchField("Search orders...");
        searchField.setPreferredSize(new Dimension(280, 40));
        searchField.addActionListener(e -> filterTable());

        statusFilter = new JComboBox<>(new String[]{"All", "PENDING", "PREPARING", "READY", "SERVED", "CANCELLED"});
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilter.setPreferredSize(new Dimension(140, 36));
        statusFilter.addActionListener(e -> filterTable());

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
        refreshBtn.addActionListener(e -> loadOrders());

        toolbar.add(searchField);
        toolbar.add(statusFilter);
        toolbar.add(refreshBtn);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(toolbar, BorderLayout.EAST);

        // Table
        tableModel = new DefaultTableModel(new String[]{"Order ID", "Customer", "Mobile", "Items", "Total", "Status", "Date/Time"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                return col == 5 ? StatusBadge.class : Object.class;
            }
        };

        ordersTable = new JTable(tableModel);
        ordersTable.setRowHeight(44);
        ordersTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ordersTable.setGridColor(ThemeManager.getBorderLight());
        ordersTable.setShowGrid(true);
        ordersTable.setIntercellSpacing(new Dimension(0, 1));
        ordersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        ordersTable.getTableHeader().setForeground(ThemeManager.getTextSecondary());
        ordersTable.getTableHeader().setBackground(ThemeManager.getBgTertiary());
        ordersTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getBorderLight()));
        ordersTable.getTableHeader().setReorderingAllowed(false);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordersTable.setFillsViewportHeight(true);

        // Column widths
        ordersTable.getColumnModel().getColumn(0).setMaxWidth(90);   // Order ID
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(180); // Customer
        ordersTable.getColumnModel().getColumn(2).setPreferredWidth(130); // Mobile
        ordersTable.getColumnModel().getColumn(3).setPreferredWidth(250); // Items
        ordersTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Total
        ordersTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Status
        ordersTable.getColumnModel().getColumn(6).setPreferredWidth(160); // Date/Time

        // Custom renderer for status column
        ordersTable.getColumnModel().getColumn(5).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            if (value instanceof StatusBadge badge) {
                return badge;
            }
            JLabel label = new JLabel(value != null ? value.toString() : "");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setOpaque(true);
            label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return label;
        });

        // Double-click to view details
        ordersTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = ordersTable.getSelectedRow();
                    if (row >= 0) {
                        int modelRow = ordersTable.convertRowIndexToModel(row);
                        int orderId = (int) tableModel.getValueAt(modelRow, 0);
                        showOrderDetails(orderId);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.getBorderLight()));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadOrders() {
        new SwingWorker<List<Order>, Void>() {
            @Override
            protected List<Order> doInBackground() {
                return orderService.getRecentOrders(200);
            }

            @Override
            protected void done() {
                try {
                    List<Order> orders = get();
                    tableModel.setRowCount(0);

                    for (Order order : orders) {
                        StringBuilder itemsSb = new StringBuilder();
                        for (int i = 0; i < order.getItems().size(); i++) {
                            canteen.OrderItem item = order.getItems().get(i);
                            if (i > 0) itemsSb.append(", ");
                            itemsSb.append(item.getItemName()).append(" x").append(item.getQuantity());
                        }

                        tableModel.addRow(new Object[]{
                            order.getOrderId(),
                            order.getCustomerName(),
                            order.getCustomerMobile(),
                            itemsSb.toString(),
                            currencyFmt.format(order.getTotalAmount()),
                            new StatusBadge(order.getStatus()),
                            order.getFormattedOrderDate()
                        });
                    }
                    filterTable();
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void filterTable() {
        String searchText = searchField.getText().toLowerCase().trim();
        String statusFilterText = (String) statusFilter.getSelectedItem();

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        ordersTable.setRowSorter(sorter);

        List<RowFilter<DefaultTableModel, Object>> filters = new java.util.ArrayList<>();

        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + searchText, 0, 1, 2, 3));
        }

        if (!"All".equals(statusFilterText)) {
            filters.add(RowFilter.regexFilter("^" + statusFilterText + "$", 5));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else if (filters.size() == 1) {
            sorter.setRowFilter(filters.get(0));
        } else {
            sorter.setRowFilter(RowFilter.orFilter(filters));
        }
    }

    private void showOrderDetails(int orderId) {
        orderService.findById(orderId).ifPresent(order -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Order #").append(order.getOrderId()).append("\n");
            sb.append("Customer: ").append(order.getCustomerName()).append(" (").append(order.getCustomerMobile()).append(")\n");
            sb.append("Date: ").append(order.getFormattedOrderDate()).append("\n");
            sb.append("Status: ").append(order.getStatus().getDisplayName()).append("\n");
            sb.append("QR Token: ").append(order.getQrToken()).append("\n");
            sb.append("WhatsApp Sent: ").append(order.isWhatsappSent() ? "Yes" : "No").append("\n\n");
            sb.append("Items:\n");
            for (canteen.OrderItem item : order.getItems()) {
                sb.append("  ").append(item.getItemName())
                  .append(" x").append(item.getQuantity())
                  .append(" = ").append(currencyFmt.format(item.getLineTotal()))
                  .append("\n");
            }
            sb.append("\nSubtotal: ").append(currencyFmt.format(order.getBaseAmount()));
            sb.append("\nGST (5%): ").append(currencyFmt.format(order.getGstAmount()));
            sb.append("\nTotal: ").append(currencyFmt.format(order.getTotalAmount()));

            JTextArea area = new JTextArea(sb.toString());
            area.setEditable(false);
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            area.setBorder(new EmptyBorder(10, 10, 10, 10));

            JScrollPane scroll = new JScrollPane(area);
            scroll.setPreferredSize(new Dimension(450, 500));

            JOptionPane.showMessageDialog(this, scroll, "Order #" + orderId + " Details", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public void refresh() { loadOrders(); }

    private void startAutoRefresh() {
        autoRefreshTimer = new Timer("OrdersPanel-AutoRefresh", true);
        autoRefreshTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override public void run() {
                SwingUtilities.invokeLater(() -> loadOrders());
            }
        }, 30000, 30000);
    }

    public void stopAutoRefresh() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.cancel();
            autoRefreshTimer = null;
        }
    }
}