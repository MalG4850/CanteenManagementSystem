package canteen;

import java.sql.*;

public class CanteenAdminService {

    public void addItem(String itemName, double price) {
        String query = "INSERT INTO food_menu (item_name, price) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, itemName);
            stmt.setDouble(2, price);
            stmt.executeUpdate();
            System.out.println("✔ Item added to menu successfully: " + itemName);
        } catch (SQLException e) {
            System.err.println("❌ Error adding item: " + e.getMessage());
        }
    }

    public void deleteItem(int itemId) {
        String query = "DELETE FROM food_menu WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, itemId);
            int rows = stmt.executeUpdate();
            if (rows > 0) System.out.println("✔ Item dropped from menu successfully.");
            else System.out.println("⚠️ Item matching ID not found.");
        } catch (SQLException e) {
            System.err.println("❌ Error removing item: " + e.getMessage());
        }
    }

    public void updatePrice(int itemId, double newPrice) {
        String query = "UPDATE food_menu SET price = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, newPrice);
            stmt.setInt(2, itemId);
            int rows = stmt.executeUpdate();
            if (rows > 0) System.out.println("✔ Base price updated successfully.");
            else System.out.println("⚠️ Item matching ID not found.");
        } catch (SQLException e) {
            System.err.println("❌ Error updating rate: " + e.getMessage());
        }
    }

    public void viewMenu() {
        String query = "SELECT * FROM food_menu";
        System.out.println("\n--- CANTEEN FOOD MENU LIST ---");
        System.out.printf("%-5s %-25s %-10s\n", "ID", "Item Name", "Price");
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                System.out.printf("%-5d %-25s ₹%-10.2f\n", 
                    rs.getInt("id"), rs.getString("item_name"), rs.getDouble("price"));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error pulling food menu records: " + e.getMessage());
        }
    }

    public CartItem fetchItemForCart(int itemId, int qty) {
        String query = "SELECT item_name, price FROM food_menu WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, itemId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new CartItem(itemId, rs.getString("item_name"), qty, rs.getDouble("price"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching cart structural properties: " + e.getMessage());
        }
        return null;
    }

    public void findOrder(int orderId) {
        String orderQuery = "SELECT * FROM food_orders WHERE order_id = ?";
        String itemsQuery = "SELECT m.item_name, i.quantity, i.item_price, (i.quantity * i.item_price) AS line_total " +
                            "FROM order_items i JOIN food_menu m ON i.item_id = m.id WHERE i.order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement orderStmt = conn.prepareStatement(orderQuery);
             PreparedStatement itemsStmt = conn.prepareStatement(itemsQuery)) {
            
            orderStmt.setInt(1, orderId);
            try (ResultSet orderRs = orderStmt.executeQuery()) {
                if (orderRs.next()) {
                    System.out.println("\n=========================================");
                    System.out.println("          RETRIEVED ORDER PROFILE        ");
                    System.out.println("=========================================");
                    System.out.println("Order ID:       #" + orderRs.getInt("order_id"));
                    System.out.println("Customer Name:  " + orderRs.getString("customer_name"));
                    System.out.println("Mobile Number:  " + orderRs.getString("customer_mobile"));
                    System.out.println("Placed On:      " + orderRs.getTimestamp("order_date"));
                    System.out.println("-----------------------------------------");
                    System.out.printf("%-18s %-5s %-8s %-8s\n", "Item Name", "Qty", "Rate", "Total");
                    System.out.println("-----------------------------------------");

                    itemsStmt.setInt(1, orderId);
                    try (ResultSet itemsRs = itemsStmt.executeQuery()) {
                        while (itemsRs.next()) {
                            System.out.printf("%-18s %-5d ₹%-7.2f ₹%-7.2f\n",
                                itemsRs.getString("item_name"),
                                itemsRs.getInt("quantity"),
                                itemsRs.getDouble("item_price"),
                                itemsRs.getDouble("line_total"));
                        }
                    }

                    System.out.println("-----------------------------------------");
                    System.out.printf("Base Amount:                   ₹%.2f\n", orderRs.getDouble("base_amount"));
                    System.out.printf("GST Value (5%%):                ₹%.2f\n", orderRs.getDouble("gst_amount"));
                    System.out.printf("NET TOTAL PAID:                ₹%.2f\n", orderRs.getDouble("total_amount"));
                    System.out.println("=========================================\n");
                } else {
                    System.out.println("⚠️ System record missing for Order ID: #" + orderId);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Processing lookup execution crash: " + e.getMessage());
        }
    }

    public void displayTotalDailyRevenue() {
        String query = "SELECT COUNT(order_id) AS total_orders, SUM(base_amount) AS total_base, SUM(total_amount) AS daily_total " +
                       "FROM food_orders WHERE DATE(order_date) = CURRENT_DATE";
                       
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next() && rs.getInt("total_orders") > 0) {
                System.out.println("\n=========================================");
                System.out.println("     DAILY REVENUE REPORT (TODAY)        ");
                System.out.println("=========================================");
                System.out.printf("Total Bills Settled:        %d\n", rs.getInt("total_orders"));
                System.out.printf("Net Base Sales:             ₹%.2f\n", rs.getDouble("total_base"));
                System.out.printf("Gross Collected (Inc. GST): ₹%.2f\n", rs.getDouble("daily_total"));
                System.out.println("=========================================\n");
            } else {
                System.out.println("\n⚠️ No sales transactions recorded today yet.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error calculating daily metrics: " + e.getMessage());
        }
    }

    public void displayTotalMonthlyRevenue() {
        String query = "SELECT COUNT(order_id) AS total_orders, SUM(base_amount) AS total_base, SUM(total_amount) AS monthly_total " +
                       "FROM food_orders WHERE MONTH(order_date) = MONTH(CURRENT_DATE) AND YEAR(order_date) = YEAR(CURRENT_DATE)";
                       
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next() && rs.getInt("total_orders") > 0) {
                System.out.println("\n=========================================");
                System.out.println("     MONTHLY REVENUE REPORT (CURRENT)    ");
                System.out.println("=========================================");
                System.out.printf("Total Bills Settled:        %d\n", rs.getInt("total_orders"));
                System.out.printf("Net Base Sales:             ₹%.2f\n", rs.getDouble("total_base"));
                System.out.printf("Gross Collected (Inc. GST): ₹%.2f\n", rs.getDouble("monthly_total"));
                System.out.println("=========================================\n");
            } else {
                System.out.println("\n⚠️ No sales transactions recorded for this month.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error calculating monthly aggregates: " + e.getMessage());
        }
    }

    public void deleteOrder(int orderId) {
        String query = "DELETE FROM food_orders WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            int rows = stmt.executeUpdate();
            if (rows > 0) System.out.println("✔ Order records dropped successfully.");
            else System.out.println("⚠️ Order not found.");
        } catch (SQLException e) {
            System.err.println("❌ Error dropping transaction record: " + e.getMessage());
        }
    }
}
