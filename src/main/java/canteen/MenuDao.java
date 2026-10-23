package canteen;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MenuDao {
    private final AppConfig config = AppConfig.getInstance();

    public List<MenuItem> getAllItems() {
        return getAllItems(false);
    }

    public List<MenuItem> getAllItems(boolean includeUnavailable) {
        String sql = includeUnavailable
            ? "SELECT id, item_name, price, category, is_available FROM food_menu ORDER BY category, item_name"
            : "SELECT id, item_name, price, category, is_available FROM food_menu WHERE is_available = TRUE ORDER BY category, item_name";
        List<MenuItem> items = new ArrayList<>();
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                items.add(new MenuItem(
                    rs.getInt("id"),
                    rs.getString("item_name"),
                    rs.getDouble("price"),
                    rs.getString("category"),
                    rs.getBoolean("is_available")
                ));
            }
        } catch (SQLException e) {
            System.err.println("❌ MenuDao.getAllItems: " + e.getMessage());
        }
        return items;
    }

    public List<MenuItem> getItemsByCategory(String category) {
        String sql = "SELECT id, item_name, price, category, is_available FROM food_menu WHERE category = ? AND is_available = TRUE ORDER BY item_name";
        List<MenuItem> items = new ArrayList<>();
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new MenuItem(
                        rs.getInt("id"),
                        rs.getString("item_name"),
                        rs.getDouble("price"),
                        rs.getString("category"),
                        rs.getBoolean("is_available")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ MenuDao.getItemsByCategory: " + e.getMessage());
        }
        return items;
    }

    public List<String> getCategories() {
        String sql = "SELECT DISTINCT category FROM food_menu ORDER BY category";
        List<String> categories = new ArrayList<>();
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String cat = rs.getString("category");
                if (cat != null && !cat.trim().isEmpty()) {
                    categories.add(cat.trim());
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ MenuDao.getCategories: " + e.getMessage());
        }
        return categories;
    }

    public Optional<MenuItem> findById(int itemId) {
        String sql = "SELECT id, item_name, price, category, is_available FROM food_menu WHERE id = ?";
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, itemId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new MenuItem(
                        rs.getInt("id"),
                        rs.getString("item_name"),
                        rs.getDouble("price"),
                        rs.getString("category"),
                        rs.getBoolean("is_available")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ MenuDao.findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean addItem(String name, double price, String category) {
        String sql = "INSERT INTO food_menu (item_name, price, category, is_available) VALUES (?, ?, ?, TRUE)";
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setString(3, category != null && !category.trim().isEmpty() ? category.trim() : "General");
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ MenuDao.addItem: " + e.getMessage());
            return false;
        }
    }

    public boolean updateItem(int itemId, String name, double price, String category, boolean available) {
        String sql = "UPDATE food_menu SET item_name = ?, price = ?, category = ?, is_available = ? WHERE id = ?";
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setString(3, category != null && !category.trim().isEmpty() ? category.trim() : "General");
            stmt.setBoolean(4, available);
            stmt.setInt(5, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ MenuDao.updateItem: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePrice(int itemId, double newPrice) {
        String sql = "UPDATE food_menu SET price = ? WHERE id = ?";
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newPrice);
            stmt.setInt(2, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ MenuDao.updatePrice: " + e.getMessage());
            return false;
        }
    }

    public boolean setAvailability(int itemId, boolean available) {
        String sql = "UPDATE food_menu SET is_available = ? WHERE id = ?";
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, available);
            stmt.setInt(2, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ MenuDao.setAvailability: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteItem(int itemId) {
        String sql = "DELETE FROM food_menu WHERE id = ?";
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ MenuDao.deleteItem: " + e.getMessage());
            return false;
        }
    }
}