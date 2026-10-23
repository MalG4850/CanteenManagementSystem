package canteen;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CategoryDao {
    private final AppConfig config = AppConfig.getInstance();

    public List<String> getAllCategories() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT name FROM category ORDER BY name";
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("❌ CategoryDao.getAllCategories: " + e.getMessage());
        }
        return list;
    }

    public boolean addCategory(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        String sql = "INSERT IGNORE INTO category (name) VALUES (?)";
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name.trim());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ CategoryDao.addCategory: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteCategory(String name) {
        String sql = "DELETE FROM category WHERE name = ?";
        try (Connection conn = config.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ CategoryDao.deleteCategory: " + e.getMessage());
            return false;
        }
    }
}