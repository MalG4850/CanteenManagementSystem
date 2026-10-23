package canteen.ui.dashboard;

import canteen.ui.components.RoundedPanel;
import canteen.ui.components.ActionButton;
import canteen.ui.components.SearchField;
import canteen.ui.theme.ThemeManager;
import canteen.MenuItem;
import canteen.MenuService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MenuManagementPanel extends JPanel {

    private MenuService menuService;
    private JTable menuTable;
    private DefaultTableModel tableModel;
    private SearchField searchField;
    private JComboBox<String> categoryFilter;
    private JButton addItemBtn;
    private JButton refreshBtn;
    private NumberFormat currencyFmt;
    private MenuItem editingItem = null;

    public MenuManagementPanel() {
        this.menuService = new MenuService();
        this.currencyFmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents();
        loadMenu();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel titleLabel = new JLabel("📋 Menu Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(ThemeManager.getTextPrimary());

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        toolbar.setOpaque(false);

        searchField = new SearchField("Search menu items...");
        searchField.setPreferredSize(new Dimension(280, 40));
        searchField.addActionListener(e -> filterTable());

        categoryFilter = new JComboBox<>();
        categoryFilter.addItem("All Categories");
        for (String cat : menuService.getCategories()) {
            categoryFilter.addItem(cat);
        }
        categoryFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryFilter.setPreferredSize(new Dimension(160, 36));
        categoryFilter.addActionListener(e -> filterTable());

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
        refreshBtn.addActionListener(e -> loadMenu());

        addItemBtn = new JButton("➕ Add Item");
        addItemBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addItemBtn.setForeground(Color.WHITE);
        addItemBtn.setBackground(ThemeManager.getAccent());
        addItemBtn.setFocusPainted(false);
        addItemBtn.setBorder(new EmptyBorder(8, 20, 8, 20));
        addItemBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addItemBtn.addActionListener(e -> showItemDialog(null));

        toolbar.add(searchField);
        toolbar.add(categoryFilter);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(refreshBtn);
        toolbar.add(addItemBtn);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(toolbar, BorderLayout.EAST);

        // Table
        tableModel = new DefaultTableModel(new String[]{"ID", "Item Name", "Category", "Price", "Available", "Actions"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 5; }
            @Override public Class<?> getColumnClass(int col) {
                return col == 4 ? Boolean.class : (col == 5 ? JButton.class : Object.class);
            }
        };

        menuTable = new JTable(tableModel);
        menuTable.setRowHeight(40);
        menuTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        menuTable.setGridColor(ThemeManager.getBorderLight());
        menuTable.setShowGrid(true);
        menuTable.setIntercellSpacing(new Dimension(0, 1));
        menuTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        menuTable.getTableHeader().setForeground(ThemeManager.getTextSecondary());
        menuTable.getTableHeader().setBackground(ThemeManager.getBgTertiary());
        menuTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getBorderLight()));
        menuTable.getTableHeader().setReorderingAllowed(false);
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuTable.setFillsViewportHeight(true);

        // Column widths
        menuTable.getColumnModel().getColumn(0).setMaxWidth(60);   // ID
        menuTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Name
        menuTable.getColumnModel().getColumn(2).setPreferredWidth(130); // Category
        menuTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Price
        menuTable.getColumnModel().getColumn(4).setPreferredWidth(90);  // Available
        menuTable.getColumnModel().getColumn(5).setPreferredWidth(140); // Actions

        // Action buttons renderer/editor
        menuTable.getColumnModel().getColumn(5).setCellRenderer(new ActionButtonRenderer());
        menuTable.getColumnModel().getColumn(5).setCellEditor(new ActionButtonEditor(new JCheckBox()));

        // Checkbox renderer for availability
        menuTable.getColumnModel().getColumn(4).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            JCheckBox cb = new JCheckBox();
            cb.setSelected(value != null && (Boolean) value);
            cb.setHorizontalAlignment(SwingConstants.CENTER);
            cb.setOpaque(true);
            cb.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return cb;
        });
        menuTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            @Override public boolean stopCellEditing() {
                int row = menuTable.getEditingRow();
                int col = menuTable.getEditingColumn();
                if (row >= 0 && col == 4) {
                    Boolean newVal = (Boolean) getCellEditorValue();
                    int itemId = (int) tableModel.getValueAt(row, 0);
                    menuService.setAvailability(itemId, newVal);
                }
                return super.stopCellEditing();
            }
        });

        JScrollPane scrollPane = new JScrollPane(menuTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.getBorderLight()));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadMenu() {
        SwingUtilities.invokeLater(() -> {
            List<MenuItem> items = menuService.getAllItems(true);
            tableModel.setRowCount(0);

            for (MenuItem item : items) {
                tableModel.addRow(new Object[]{
                    item.getId(),
                    item.getName(),
                    item.getCategory(),
                    currencyFmt.format(item.getPrice()),
                    item.isAvailable(),
                    "Edit"
                });
            }
            filterTable();
        });
    }

    private void filterTable() {
        String searchText = searchField.getText().toLowerCase().trim();
        String categoryFilterText = (String) categoryFilter.getSelectedItem();

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        menuTable.setRowSorter(sorter);

        List<RowFilter<DefaultTableModel, Object>> filters = new java.util.ArrayList<>();

        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + searchText, 1));
        }

        if (!"All Categories".equals(categoryFilterText)) {
            filters.add(RowFilter.regexFilter("^" + categoryFilterText + "$", 2));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else if (filters.size() == 1) {
            sorter.setRowFilter(filters.get(0));
        } else {
            sorter.setRowFilter(RowFilter.orFilter(filters));
        }
    }

    private void showItemDialog(MenuItem existingItem) {
        boolean isEdit = existingItem != null;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Menu Item" : "Add Menu Item", true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(ThemeManager.getBgSecondary());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Item Name:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(20);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (isEdit) nameField.setText(existingItem.getName());
        formPanel.add(nameField, gbc);

        // Category
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> catCombo = new JComboBox<>(menuService.getCategories().toArray(new String[0]));
        catCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        catCombo.setEditable(true);
        if (isEdit) catCombo.setSelectedItem(existingItem.getCategory());
        formPanel.add(catCombo, gbc);

        // Price
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Price (₹):"), gbc);
        gbc.gridx = 1;
        JTextField priceField = new JTextField(20);
        priceField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (isEdit) priceField.setText(String.valueOf(existingItem.getPrice()));
        formPanel.add(priceField, gbc);

        // Available
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Available:"), gbc);
        gbc.gridx = 1;
        JCheckBox availableCheck = new JCheckBox("Yes");
        availableCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        availableCheck.setOpaque(false);
        if (isEdit) availableCheck.setSelected(existingItem.isAvailable());
        else availableCheck.setSelected(true);
        formPanel.add(availableCheck, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        ActionButton cancelBtn = new ActionButton("Cancel", ActionButton.Variant.SECONDARY);
        cancelBtn.addActionListener(e -> dialog.dispose());

        ActionButton saveBtn = new ActionButton(isEdit ? "Save Changes" : "Add Item", ActionButton.Variant.PRIMARY);
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String category = (String) catCombo.getSelectedItem();
            String priceStr = priceField.getText().trim();
            boolean available = availableCheck.isSelected();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Item name is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double price;
            try {
                price = Double.parseDouble(priceStr);
                if (price < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Enter a valid price", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean success;
            if (isEdit) {
                success = menuService.updateItem(existingItem.getId(), name, price, category, available);
            } else {
                success = menuService.addItem(name, price, category);
                if (success && !available) {
                    List<MenuItem> items = menuService.getAllItems(true);
                    for (MenuItem mi : items) {
                        if (mi.getName().equals(name) && mi.getPrice() == price) {
                            menuService.setAvailability(mi.getId(), false);
                            break;
                        }
                    }
                }
            }

            if (success) {
                dialog.dispose();
                loadMenu();
                refreshCategoryFilter();
            } else {
                JOptionPane.showMessageDialog(dialog, "Operation failed. Item may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void refreshCategoryFilter() {
        String selected = (String) categoryFilter.getSelectedItem();
        categoryFilter.removeAllItems();
        categoryFilter.addItem("All Categories");
        for (String cat : menuService.getCategories()) {
            categoryFilter.addItem(cat);
        }
        if (selected != null) categoryFilter.setSelectedItem(selected);
    }

    // Action button renderer for Edit/Delete
    private class ActionButtonRenderer extends JButton implements TableCellRenderer {
        public ActionButtonRenderer() {
            setOpaque(true);
            setText("Edit");
            setMargin(new Insets(2, 8, 2, 8));
            setFont(new Font("Segoe UI", Font.PLAIN, 11));
        }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    private class ActionButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int clickedRow;

        public ActionButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Edit");
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            clickedRow = row;
            return button;
        }

        @Override public Object getCellEditorValue() {
            if (clickedRow >= 0 && clickedRow < menuTable.getRowCount()) {
                int modelRow = menuTable.convertRowIndexToModel(clickedRow);
                int itemId = (int) tableModel.getValueAt(modelRow, 0);
                String itemName = (String) tableModel.getValueAt(modelRow, 1);
                String category = (String) tableModel.getValueAt(modelRow, 2);
                double price = currencyFmt.parse(tableModel.getValueAt(modelRow, 3).toString(), new java.text.ParsePosition(0)).doubleValue();
                boolean available = (Boolean) tableModel.getValueAt(modelRow, 4);

                MenuItem item = new MenuItem(itemId, itemName, price, category, available);
                showItemDialog(item);
            }
            return "Edit";
        }

        @Override public boolean stopCellEditing() {
            return super.stopCellEditing();
        }
    }
}