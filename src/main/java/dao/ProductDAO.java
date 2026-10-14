package dao;

import db.DBConnection;
import java.sql.*;
import java.util.*;

public class ProductDAO {

    public List<Map<String, Object>> getAllProducts() throws SQLException {
        List<Map<String, Object>> products = new ArrayList<>();
        String sql = "select product_id, product_name, description, price, stock_quantity, category from product";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("product_id", rs.getInt("product_id"));
                p.put("product_name", rs.getString("product_name"));
                p.put("description", rs.getString("description"));
                p.put("price", rs.getBigDecimal("price"));
                p.put("stock_quantity", rs.getInt("stock_quantity"));
                p.put("category", rs.getString("category"));
                products.add(p);
            }
        }
        return products;
    }

    public List<Map<String, Object>> getProductsByCategory(String category) throws SQLException {
        List<Map<String, Object>> products = new ArrayList<>();
        String sql = "select product_id, product_name, description, price, stock_quantity, category from product where category = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("product_id", rs.getInt("product_id"));
                p.put("product_name", rs.getString("product_name"));
                p.put("description", rs.getString("description"));
                p.put("price", rs.getBigDecimal("price"));
                p.put("stock_quantity", rs.getInt("stock_quantity"));
                p.put("category", rs.getString("category"));
                products.add(p);
            }
        }
        return products;
    }

    // ✅ SEARCH METHOD (clean + correct)
    public List<Map<String, Object>> searchProducts(String search, String category) throws SQLException {
        List<Map<String, Object>> products = new ArrayList<>();

        String sql;

        if (category == null || category.equals("all")) {
            sql = "SELECT * FROM product WHERE LOWER(product_name) LIKE ?";
        } else {
            sql = "SELECT * FROM product WHERE LOWER(product_name) LIKE ? AND category = ?";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + search.toLowerCase() + "%");

            if (category != null && !category.equals("all")) {
                ps.setString(2, category);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("product_id", rs.getInt("product_id"));
                p.put("product_name", rs.getString("product_name"));
                p.put("description", rs.getString("description"));
                p.put("price", rs.getBigDecimal("price"));
                p.put("stock_quantity", rs.getInt("stock_quantity"));
                p.put("category", rs.getString("category"));
                products.add(p);
            }
        }

        return products;
    }
}