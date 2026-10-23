package canteen;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

public final class CashierFrame extends JFrame {
    private final MenuService menuService = new MenuService();
    private final OrderService orderService = new OrderService();
    private final WhatsAppService whatsAppService = new WhatsAppService(AppConfig.getInstance());
    private final List<CartItem> cart = new ArrayList<>();

    private JTable menuTable;
    private DefaultTableModel menuModel;
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel subtotalLabel;
    private JLabel gstLabel;
    private JLabel totalLabel;
    private JTextField customerNameField;
    private JTextField customerPhoneField;
    private JComboBox<String> categoryCombo;

    public CashierFrame() {
        super("Canteen Management System - Cashier");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initComponents();
        loadMenu();
        updateCartDisplay();
    }

    private void initComponents() {
        // Top toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JLabel("Category:"));
        categoryCombo = new JComboBox<>();
        categoryCombo.addItem("All");
        for (String cat : menuService.getCategories()) {
            categoryCombo.addItem(cat);
        }
        categoryCombo.addActionListener(e -> filterMenu());
        toolbar.add(categoryCombo);
        toolbar.addSeparator();
        JButton refreshBtn = new JButton("Refresh Menu");
        refreshBtn.addActionListener(e -> loadMenu());
        toolbar.add(refreshBtn);
        toolbar.addSeparator();
        JButton historyBtn = new JButton("Order History");
        historyBtn.addActionListener(e -> showHistoryDialog());
        toolbar.add(historyBtn);
        add(toolbar, BorderLayout.NORTH);

        // Center: Split pane with menu and cart
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(650);
        splitPane.setResizeWeight(0.6);

        // Left: Menu
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBorder(BorderFactory.createTitledBorder("Menu"));
        menuModel = new DefaultTableModel(new String[]{"ID", "Item", "Category", "Price", "Action"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 4; }
            @Override public Class<?> getColumnClass(int col) { return col == 4 ? JButton.class : Object.class; }
        };
        menuTable = new JTable(menuModel);
        menuTable.setRowHeight(36);
        menuTable.getColumnModel().getColumn(0).setMaxWidth(60);
        menuTable.getColumnModel().getColumn(3).setMaxWidth(80);
        menuTable.getColumnModel().getColumn(4).setMaxWidth(100);
        menuTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        menuTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
        menuPanel.add(new JScrollPane(menuTable), BorderLayout.CENTER);
        splitPane.setLeftComponent(menuPanel);

        // Right: Cart + Customer
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(400, 0));

        // Cart table
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBorder(BorderFactory.createTitledBorder("Cart"));
        cartModel = new DefaultTableModel(new String[]{"Item", "Qty", "Price", "Total", ""}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 1 || col == 4; }
            @Override public Class<?> getColumnClass(int col) {
                return col == 1 ? Integer.class : (col == 4 ? JButton.class : Object.class);
            }
        };
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(32);
        cartTable.getColumnModel().getColumn(1).setMaxWidth(60);
        cartTable.getColumnModel().getColumn(2).setMaxWidth(70);
        cartTable.getColumnModel().getColumn(3).setMaxWidth(80);
        cartTable.getColumnModel().getColumn(4).setMaxWidth(40);
        cartTable.getColumn("").setCellRenderer(new RemoveButtonRenderer());
        cartTable.getColumn("").setCellEditor(new RemoveButtonEditor(new JCheckBox()));
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        // Totals
        JPanel totalsPanel = new JPanel(new GridLayout(3, 2, 8, 4));
        totalsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        totalsPanel.add(new JLabel("Subtotal:")); subtotalLabel = new JLabel("₹0.00", SwingConstants.RIGHT); totalsPanel.add(subtotalLabel);
        totalsPanel.add(new JLabel("GST (5%):")); gstLabel = new JLabel("₹0.00", SwingConstants.RIGHT); totalsPanel.add(gstLabel);
        totalsPanel.add(new JLabel("Total:")); totalLabel = new JLabel("₹0.00", SwingConstants.RIGHT); totalsPanel.add(totalLabel);
        Font totalFont = totalLabel.getFont().deriveFont(Font.BOLD, 16f);
        totalLabel.setFont(totalFont);
        cartPanel.add(totalsPanel, BorderLayout.SOUTH);

        rightPanel.add(cartPanel, BorderLayout.CENTER);

        // Customer info panel
        JPanel customerPanel = new JPanel(new GridBagLayout());
        customerPanel.setBorder(BorderFactory.createTitledBorder("Customer Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; customerPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; customerNameField = new JTextField(15); customerPanel.add(customerNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; customerPanel.add(new JLabel("Mobile:"), gbc);
        gbc.gridx = 1; customerPhoneField = new JTextField(15); customerPanel.add(customerPhoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton checkoutBtn = new JButton("Checkout");
        checkoutBtn.setPreferredSize(new Dimension(0, 40));
        checkoutBtn.setFont(checkoutBtn.getFont().deriveFont(Font.BOLD, 14f));
        checkoutBtn.addActionListener(e -> processCheckout());
        customerPanel.add(checkoutBtn, gbc);

        rightPanel.add(customerPanel, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);

        // Status bar
        JLabel statusLabel = new JLabel("Ready | Press F5 to refresh menu");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(statusLabel, BorderLayout.SOUTH);

        // Keyboard shortcuts
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F5"), "refresh");
        getRootPane().getActionMap().put("refresh", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { loadMenu(); }
        });
    }

    private void loadMenu() {
        String selectedCat = (String) categoryCombo.getSelectedItem();
        List<MenuItem> items = "All".equals(selectedCat) ? menuService.getAllItems() : menuService.getItemsByCategory(selectedCat);

        menuModel.setRowCount(0);
        for (MenuItem item : items) {
            menuModel.addRow(new Object[]{
                item.getId(),
                item.getName(),
                item.getCategory(),
                String.format("₹%.2f", item.getPrice()),
                "Add"
            });
        }
    }

    private void filterMenu() {
        loadMenu();
    }

    private void addToCart(int itemId, String itemName, double price) {
        for (CartItem ci : cart) {
            if (ci.getItemId() == itemId) {
                ci.setQuantity(ci.getQuantity() + 1);
                updateCartDisplay();
                return;
            }
        }
        cart.add(new CartItem(itemId, itemName, 1, price));
        updateCartDisplay();
    }

    private void updateCartDisplay() {
        cartModel.setRowCount(0);
        double subtotal = 0;
        for (int i = 0; i < cart.size(); i++) {
            CartItem ci = cart.get(i);
            cartModel.addRow(new Object[]{
                ci.getItemName(),
                ci.getQuantity(),
                String.format("₹%.2f", ci.getPricePerUnit()),
                String.format("₹%.2f", ci.getTotalPrice()),
                "✕"
            });
            subtotal += ci.getTotalPrice();
        }
        double gst = subtotal * 0.05;
        double total = subtotal + gst;
        subtotalLabel.setText(String.format("₹%.2f", subtotal));
        gstLabel.setText(String.format("₹%.2f", gst));
        totalLabel.setText(String.format("₹%.2f", total));
    }

    private void processCheckout() {
        String name = customerNameField.getText().trim();
        String phone = customerPhoneField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter customer name", "Missing Info", JOptionPane.WARNING_MESSAGE);
            customerNameField.requestFocus();
            return;
        }
        if (!phone.matches("^\\d{10}$")) {
            JOptionPane.showMessageDialog(this, "Enter valid 10-digit mobile number", "Invalid Mobile", JOptionPane.WARNING_MESSAGE);
            customerPhoneField.requestFocus();
            return;
        }
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show confirming dialog
        double subtotal = cart.stream().mapToDouble(CartItem::getTotalPrice).sum();
        double gst = subtotal * 0.05;
        double total = subtotal + gst;

        String msg = String.format(
            "Confirm Order:\nCustomer: %s (%s)\nItems: %d\nSubtotal: ₹%.2f\nGST: ₹%.2f\nTotal: ₹%.2f",
            name, phone, cart.size(), subtotal, gst, total
        );
        int confirm = JOptionPane.showConfirmDialog(this, msg, "Confirm Checkout", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Process order
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        OrderService.OrderResult result = orderService.placeOrder(name, phone, cart);

        SwingUtilities.invokeLater(() -> {
            setCursor(Cursor.getDefaultCursor());
            if (result.isSuccess()) {
                Order order = result.getOrder();
                showQrDialog(order);
                // Clear cart and customer fields
                cart.clear();
                customerNameField.setText("");
                customerPhoneField.setText("");
                updateCartDisplay();
            } else {
                JOptionPane.showMessageDialog(this, "Order failed: " + result.getError(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showQrDialog(Order order) {
        QrDialog dialog = new QrDialog(this, order, whatsAppService);
        dialog.setVisible(true);
    }

    private void showHistoryDialog() {
        HistoryDialog dialog = new HistoryDialog(this, orderService);
        dialog.setVisible(true);
    }

    // --- Button Renderers/Editors for Menu Table ---
    private static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        ButtonRenderer() { setOpaque(true); setText("Add"); setMargin(new Insets(2, 8, 2, 8)); }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int clickedRow;

        ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Add");
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            clickedRow = row;
            return button;
        }

        @Override public Object getCellEditorValue() {
            int id = (int) menuModel.getValueAt(clickedRow, 0);
            String name = (String) menuModel.getValueAt(clickedRow, 1);
            double price = Double.parseDouble(((String) menuModel.getValueAt(clickedRow, 3)).replace("₹", ""));
            addToCart(id, name, price);
            return "Add";
        }

        @Override public boolean stopCellEditing() {
            return super.stopCellEditing();
        }
    }

    // --- Remove Button for Cart Table ---
    private static class RemoveButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        RemoveButtonRenderer() { setText("✕"); setMargin(new Insets(0, 4, 0, 4)); setForeground(Color.RED); setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false); }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            return this;
        }
    }

    private class RemoveButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int clickedRow;

        RemoveButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("✕");
            button.setForeground(Color.RED);
            button.setFocusPainted(false);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            clickedRow = row;
            return button;
        }

        @Override public Object getCellEditorValue() {
            if (clickedRow >= 0 && clickedRow < cart.size()) {
                cart.remove(clickedRow);
                updateCartDisplay();
            }
            return "✕";
        }

        @Override public boolean stopCellEditing() {
            return super.stopCellEditing();
        }
    }
}