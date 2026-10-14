package dao;
import db.DBConnection;
import java.sql.*;
import java.util.*;
public class OrderDAO {
    public String checkout(int userId) throws SQLException {
        String sql = "select checkout_cart(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        }
        return "ERROR";
    }
    public List<Map<String, Object>> getOrderHistory(int userId) throws SQLException {
        List<Map<String, Object>> orders = new ArrayList<>();
        String sql = "select o.order_id, o.order_date, o.total_amount, o.payment_status, " +
        "p.product_name, oi.quantity, oi.unit_price " +
                "from orders o, order_items oi, product p " +
                "where o.order_id = oi.order_id " +
                "and oi.product_id = p.product_id " +
                "and o.user_id = ? " +
                "order by o.order_date desc";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> order = new HashMap<>();
                order.put("order_id", rs.getInt("order_id"));
                order.put("order_date", rs.getTimestamp("order_date"));
                order.put("total_amount", rs.getBigDecimal("total_amount"));
                order.put("status", rs.getString("payment_status"));
                order.put("product_name", rs.getString("product_name"));
                order.put("quantity", rs.getInt("quantity"));
                order.put("unit_price", rs.getBigDecimal("unit_price"));
                orders.add(order);
            }
        }
        return orders;
    }
}