package canteen;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public final class ShortCodeService {

    /**
     * Returns the next available 3‑digit short code for today.
     * Uses the current DB connection (must be within a transaction) to avoid races.
     *
     * @param conn active connection (auto‑commit must be false)
     * @return zero‑padded code like "001", "042", … up to "999"
     */
    public String nextCode(Connection conn) throws SQLException {
        LocalDate today = LocalDate.now();
        String sql = "SELECT MAX(short_code) FROM food_orders WHERE code_date = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(today));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String max = rs.getString(1);
                    if (max != null) {
                        int next = Integer.parseInt(max) + 1;
                        if (next > 999) {
                            throw new IllegalStateException("Daily short‑code limit (999) reached");
                        }
                        return String.format("%03d", next);
                    }
                }
            }
        }
        // No orders today yet
        return "001";
    }
}