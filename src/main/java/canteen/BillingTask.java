package canteen;

import java.sql.*;
import java.util.List;

public class BillingTask implements Runnable {
    private String customerName;
    private String customerMobile;
    private List<CartItem> cart;

    public BillingTask(String customerName, String customerMobile, List<CartItem> cart) {
        this.customerName = customerName;
        this.customerMobile = customerMobile;
        this.cart = cart;
    }

    @Override
    public void run() {
        double baseAmount = 0;
        for (CartItem item : cart) {
            baseAmount += item.getTotalPrice();
        }
        double gstAmount = baseAmount * 0.05; 
        double totalAmount = baseAmount + gstAmount;
        String qrToken = new TokenService(AppConfig.getInstance().getCanteenSecret())
            .generateToken((int)(System.currentTimeMillis() % 1000000));

        String insertOrder = "INSERT INTO food_orders (customer_name, customer_mobile, base_amount, gst_amount, total_amount, qr_token) VALUES (?, ?, ?, ?, ?, ?)";
        String insertItem = "INSERT INTO order_items (order_id, item_id, quantity, item_price) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); 

            try (PreparedStatement orderStmt = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setString(1, customerName);
                orderStmt.setString(2, customerMobile);
                orderStmt.setDouble(3, baseAmount);
                orderStmt.setDouble(4, gstAmount);
                orderStmt.setDouble(5, totalAmount);
                orderStmt.setString(6, qrToken);
                orderStmt.executeUpdate();

                int orderId = 0;
                try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) orderId = generatedKeys.getInt(1);
                }

                try (PreparedStatement itemStmt = conn.prepareStatement(insertItem)) {
                    for (CartItem item : cart) {
                        itemStmt.setInt(1, orderId);
                        itemStmt.setInt(2, item.getItemId());
                        itemStmt.setInt(3, item.getQuantity());
                        itemStmt.setDouble(4, item.getPricePerUnit());
                        itemStmt.addBatch();
                    }
                    itemStmt.executeBatch();
                }

                conn.commit(); 

                BillProcessor printer = new CanteenBillPrinter(customerName, customerMobile, cart);
                printer.PrintInvoice(orderId, baseAmount, gstAmount, totalAmount);

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("❌ Database transaction rolled back due to error: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("❌ Local MySQL execution failure: " + e.getMessage());
        }
    }
}
