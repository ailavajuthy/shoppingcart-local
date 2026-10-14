package dao;
import db.DBConnection;
import java.sql.*;
import java.util.*;
public class CartDAO {

    public String addToCart(int userId, int productId, int quantity) throws
            SQLException {
        String sql = "select add_to_cart(?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        }
        return "ERROR";
    }
    public double getCartTotal(int userId) throws SQLException {
        String sql = "select get_cart_total(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        }
        return 0.0;
    }
    public List<Map<String, Object>> getCartItems(int userId) throws SQLException {
        List<Map<String, Object>> items = new ArrayList<>();
        String sql = "select p.product_id, p.product_name, p.price, " +
                "ci.quantity, (p.price * ci.quantity) as item_total " +
                "from shopping_cart sc " +
                "join cart_items ci on sc.cart_id = ci.cart_id " +
                "join product p on ci.product_id = p.product_id " +
                "where sc.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("product_id", rs.getInt("product_id"));
                item.put("product_name", rs.getString("product_name"));
                item.put("price", rs.getBigDecimal("price"));
                item.put("quantity", rs.getInt("quantity"));
                item.put("item_total", rs.getBigDecimal("item_total"));
                items.add(item);
            }
        }
        return items;
    }
    public boolean removeFromCart(int userId, int productId) throws SQLException {
        String sql = "delete from cart_items ci " +
                "using shopping_cart sc " +
                "where ci.cart_id = sc.cart_id " +
                "and sc.user_id = ? AND ci.product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        }
    }
    public boolean updateQuantity(int userId, int productId, int quantity) throws
            SQLException {
        String sql = "update cart_items ci set quantity = ? " +
                "from shopping_cart sc " +
                "where ci.cart_id = sc.cart_id " +
                "and sc.user_id = ? AND ci.product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, userId);
            ps.setInt(3, productId);
            return ps.executeUpdate() > 0;
        }
    }
    public int getQuantity(int userId,int productId)throws SQLException{
        String sql="select ci.quantity " +
                "from shopping_cart sc " +
                "join cart_items ci on sc.cart_id=ci.cart_id " +
                "where sc.user_id=? and ci.product_id=?";
        try(Connection conn=DBConnection.getConnection();
            PreparedStatement ps=conn.prepareStatement(sql)){

            ps.setInt(1,userId);
            ps.setInt(2,productId);

            ResultSet rs=ps.executeQuery();
            if(rs.next()){
                return rs.getInt("quantity");
            }
        }
        return 0;
    }
    public int getStock(int productId)throws SQLException{
        String sql="SELECT stock_quantity FROM product WHERE product_id=?";
        try(Connection conn=DBConnection.getConnection();
            PreparedStatement ps=conn.prepareStatement(sql)){

            ps.setInt(1,productId);

            ResultSet rs=ps.executeQuery();
            if(rs.next()){
                return rs.getInt("stock_quantity");
            }
        }
        return 0;
    }

}
