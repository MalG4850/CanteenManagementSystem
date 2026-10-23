package canteen;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class OrderDao {
    private final AppConfig config = AppConfig.getInstance();

public Optional<Order> createOrder(String customerName, String customerMobile,
                                        double baseAmount, double gstAmount, double totalAmount,
                                        String qrToken, List<CartItem> cartItems) {
        String insertOrder = """
            INSERT INTO food_orders (customer_name, customer_mobile, base_amount, gst_amount, total_amount, qr_token, short_code, code_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        String insertItem = "INSERT INTO order_items (order_id, item_id, quantity, item_price) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = config.getDataSource().getConnection();
            conn.setAutoCommit(false);

            // generate short code for today within the same transaction
            ShortCodeService shortCodeService = new ShortCodeService();
            String shortCode = shortCodeService.nextCode(conn);
            LocalDate codeDate = LocalDate.now();

            int orderId;
            try (PreparedStatement orderStmt = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setString(1, customerName);
                orderStmt.setString(2, customerMobile);
                orderStmt.setDouble(3, baseAmount);
                orderStmt.setDouble(4, gstAmount);
                orderStmt.setDouble(5, totalAmount);
                orderStmt.setString(6, qrToken);
                orderStmt.setString(7, shortCode);
                orderStmt.setDate(8, java.sql.Date.valueOf(codeDate));
                orderStmt.executeUpdate();

                try (ResultSet keys = orderStmt.getGeneratedKeys()) {
                    if (!keys.next()) {
                        conn.rollback();
                        return Optional.empty();
                    }
                    orderId = keys.getInt(1);
                }
            }

            try (PreparedStatement itemStmt = conn.prepareStatement(insertItem)) {
                for (CartItem item : cartItems) {
                    itemStmt.setInt(1, orderId);
                    itemStmt.setInt(2, item.getItemId());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setDouble(4, item.getPricePerUnit());
                    itemStmt.addBatch();
                }
                itemStmt.executeBatch();
            }

            conn.commit();
            return findById(orderId);

        } catch (SQLException e) {
            System.err.println("❌ OrderDao.createOrder: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            }
            return Optional.empty();
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public Optional<Order> findById(int orderId) {
        String orderSql = "SELECT * FROM food_orders WHERE order_id = ?";
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {

            orderStmt.setInt(1, orderId);
            try (ResultSet rs = orderStmt.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Order order = mapOrder(rs);
                populateItemsForOrders(conn, List.of(order));
                return Optional.of(order);
            }
        } catch (SQLException e) {
            System.err.println("❌ OrderDao.findById: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Order> findByQrToken(String qrToken) {
        String sql = "SELECT * FROM food_orders WHERE qr_token = ?";
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, qrToken);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrder(rs);
                    populateItemsForOrders(conn, List.of(order));
                    return Optional.of(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ OrderDao.findByQrToken: " + e.getMessage());
        }
        return Optional.empty();
    }

    public int getOrderIdByToken(String qrToken) {
        String sql = "SELECT order_id FROM food_orders WHERE qr_token = ?";
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, qrToken);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("order_id");
            }
        } catch (SQLException e) {
            System.err.println("❌ OrderDao.getOrderIdByToken: " + e.getMessage());
        }
        return -1;
    }

    public List<Order> getOrdersByStatus(OrderStatus status, int limit) {
        String sql = "SELECT * FROM food_orders WHERE order_status = ? ORDER BY order_date DESC LIMIT ?";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrder(rs));
                }
            }
            populateItemsForOrders(conn, orders);
        } catch (SQLException e) {
            System.err.println("❌ OrderDao.getOrdersByStatus: " + e.getMessage());
        }
        return orders;
    }

    public List<Order> getRecentOrders(int limit) {
        String sql = "SELECT * FROM food_orders ORDER BY order_date DESC LIMIT ?";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrder(rs));
                }
            }
            populateItemsForOrders(conn, orders);
        } catch (SQLException e) {
            System.err.println("❌ OrderDao.getRecentOrders: " + e.getMessage());
        }
        return orders;
    }

    private void populateItemsForOrders(Connection conn, List<Order> orders) {
        if (orders == null || orders.isEmpty()) return;
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < orders.size(); i++) {
            if (i > 0) inClause.append(",");
            inClause.append("?");
        }
        String sql = "SELECT oi.id, oi.order_id, oi.item_id, m.item_name, oi.quantity, oi.item_price " +
                     "FROM order_items oi JOIN food_menu m ON oi.item_id = m.id " +
                     "WHERE oi.order_id IN (" + inClause + ") ORDER BY oi.id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < orders.size(); i++) {
                stmt.setInt(i + 1, orders.get(i).getOrderId());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                Map<Integer, Order> map = orders.stream().collect(Collectors.toMap(Order::getOrderId, o -> o, (a, b) -> a));
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    Order o = map.get(orderId);
                    if (o != null) {
                        o.addItem(new OrderItem(
                            rs.getInt("id"),
                            orderId,
                            rs.getInt("item_id"),
                            rs.getString("item_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("item_price")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ OrderDao.populateItemsForOrders: " + e.getMessage());
        }
    }

    public boolean updateStatus(int orderId, OrderStatus newStatus) {
        String sql = "UPDATE food_orders SET order_status = ?, updated_at = CURRENT_TIMESTAMP WHERE order_id = ?";
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus.name());
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ OrderDao.updateStatus: " + e.getMessage());
            return false;
        }
    }

    public boolean markWhatsAppSent(int orderId) {
        String sql = "UPDATE food_orders SET whatsapp_sent = TRUE WHERE order_id = ?";
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ OrderDao.markWhatsAppSent: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelOrder(int orderId) {
        String sql = "UPDATE food_orders SET order_status = 'CANCELLED', updated_at = CURRENT_TIMESTAMP WHERE order_id = ?";
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ OrderDao.cancelOrder: " + e.getMessage());
            return false;
        }
    }

    public double getDailyRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM food_orders WHERE DATE(order_date) = CURRENT_DATE AND order_status != 'CANCELLED'";
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("❌ OrderDao.getDailyRevenue: " + e.getMessage());
        }
        return 0;
    }

    public int getDailyOrderCount() {
        String sql = "SELECT COUNT(*) FROM food_orders WHERE DATE(order_date) = CURRENT_DATE AND order_status != 'CANCELLED'";
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("❌ OrderDao.getDailyOrderCount: " + e.getMessage());
        }
        return 0;
    }

    public double getMonthlyRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM food_orders WHERE MONTH(order_date) = MONTH(CURRENT_DATE) AND YEAR(order_date) = YEAR(CURRENT_DATE) AND order_status != 'CANCELLED'";
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("❌ OrderDao.getMonthlyRevenue: " + e.getMessage());
        }
        return 0;
    }

    public int getMonthlyOrderCount() {
        String sql = "SELECT COUNT(*) FROM food_orders WHERE MONTH(order_date) = MONTH(CURRENT_DATE) AND YEAR(order_date) = YEAR(CURRENT_DATE) AND order_status != 'CANCELLED'";
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("❌ OrderDao.getMonthlyOrderCount: " + e.getMessage());
        }
        return 0;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        return new Order(
            rs.getInt("order_id"),
            rs.getString("customer_name"),
            rs.getString("customer_mobile"),
            rs.getDouble("base_amount"),
            rs.getDouble("gst_amount"),
            rs.getDouble("total_amount"),
            OrderStatus.fromString(rs.getString("order_status")),
            rs.getString("qr_token"),
            rs.getBoolean("whatsapp_sent"),
            rs.getTimestamp("order_date"),
            rs.getTimestamp("updated_at"),
            rs.getString("short_code"),
            rs.getObject("code_date", LocalDate.class)
        );
    }
}