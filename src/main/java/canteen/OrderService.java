package canteen;

import java.util.List;

public final class OrderService {
    private final OrderDao orderDao = new OrderDao();
    private final TokenService tokenService;
    private static final double GST_RATE = 0.05; // 5%

    public OrderService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public OrderService() {
        this(new TokenService(AppConfig.getInstance().getCanteenSecret()));
    }

    public OrderResult placeOrder(String customerName, String customerMobile, List<CartItem> cart) {
        if (customerName == null || customerName.trim().isEmpty()) {
            return OrderResult.failure("Customer name is required");
        }
        if (customerMobile == null || !customerMobile.matches("^\\d{10}$")) {
            return OrderResult.failure("Valid 10-digit mobile number required");
        }
        if (cart == null || cart.isEmpty()) {
            return OrderResult.failure("Cart is empty");
        }

        double baseAmount = cart.stream().mapToDouble(CartItem::getTotalPrice).sum();
        double gstAmount = baseAmount * GST_RATE;
        double totalAmount = baseAmount + gstAmount;

        String qrToken = tokenService.generateToken(0); // temporary, will use actual orderId
        // We need to generate token with actual orderId, so we'll create order first with placeholder
        // Better approach: generate token after we have orderId
        // For now, generate a unique token and use it
        qrToken = tokenService.generateToken((int)(System.currentTimeMillis() % 1000000));

        var result = orderDao.createOrder(customerName, customerMobile, baseAmount, gstAmount, totalAmount, qrToken, cart);
        if (result.isEmpty()) {
            return OrderResult.failure("Failed to create order in database");
        }

        Order order = result.get();
        // Regenerate token with actual orderId
        String finalToken = tokenService.generateToken(order.getOrderId());
        if (!finalToken.equals(qrToken)) {
            // Update token in database
            updateQrToken(order.getOrderId(), finalToken);
            order = orderDao.findById(order.getOrderId()).orElse(order);
        }

        return OrderResult.success(order);
    }

    private boolean updateQrToken(int orderId, String newToken) {
        String sql = "UPDATE food_orders SET qr_token = ? WHERE order_id = ?";
        try (var conn = AppConfig.getInstance().getDataSource().getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newToken);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Failed to update QR token: " + e.getMessage());
            return false;
        }
    }

    public boolean advanceStatus(int orderId, OrderStatus currentStatus, OrderStatus nextStatus) {
        if (!currentStatus.canTransitionTo(nextStatus)) {
            return false;
        }
        return orderDao.updateStatus(orderId, nextStatus);
    }

    public boolean setOrderStatus(int orderId, OrderStatus newStatus) {
        OrderStatus current = getOrderStatus(orderId);
        if (current == null) return false;
        if (!current.canTransitionTo(newStatus)) {
            return false;
        }
        return orderDao.updateStatus(orderId, newStatus);
    }

    public boolean markServed(int orderId) {
        return orderDao.updateStatus(orderId, OrderStatus.SERVED);
    }

    public boolean cancelOrder(int orderId) {
        return orderDao.cancelOrder(orderId);
    }

    public OrderStatus getOrderStatus(int orderId) {
        return orderDao.findById(orderId).map(Order::getStatus).orElse(null);
    }

    public OrderStatus getOrderStatusByToken(String token) {
        return orderDao.findByQrToken(token).map(Order::getStatus).orElse(null);
    }

    public int getOrderIdByToken(String token) {
        return orderDao.findByQrToken(token).map(Order::getOrderId).orElse(-1);
    }

    public List<Order> getPendingOrders() {
        return orderDao.getOrdersByStatus(OrderStatus.PENDING, 50);
    }

    public List<Order> getPreparingOrders() {
        return orderDao.getOrdersByStatus(OrderStatus.PREPARING, 50);
    }

    public List<Order> getReadyOrders() {
        return orderDao.getOrdersByStatus(OrderStatus.READY, 50);
    }

    public List<Order> getOrdersByStatus(OrderStatus status, int limit) {
        return orderDao.getOrdersByStatus(status, limit);
    }

    public List<Order> getRecentOrders(int limit) {
        return orderDao.getRecentOrders(limit);
    }

    public java.util.Optional<Order> findById(int orderId) {
        return orderDao.findById(orderId);
    }

    public double getDailyRevenue() {
        return orderDao.getDailyRevenue();
    }

    public int getDailyOrderCount() {
        return orderDao.getDailyOrderCount();
    }

    public double getMonthlyRevenue() {
        return orderDao.getMonthlyRevenue();
    }

    public int getMonthlyOrderCount() {
        return orderDao.getMonthlyOrderCount();
    }

    public static class OrderResult {
        private final boolean success;
        private final Order order;
        private final String error;

        private OrderResult(boolean success, Order order, String error) {
            this.success = success;
            this.order = order;
            this.error = error;
        }

        public static OrderResult success(Order order) {
            return new OrderResult(true, order, null);
        }

        public static OrderResult failure(String error) {
            return new OrderResult(false, null, error);
        }

        public boolean isSuccess() { return success; }
        public Order getOrder() { return order; }
        public String getError() { return error; }
    }
}