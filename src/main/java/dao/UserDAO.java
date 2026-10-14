package dao;
import db.DBConnection;
import java.sql.*;
public class UserDAO {
    public int registerUser(String name, String email, String password, String phone) throws
            SQLException {
        String sql = "insert into users (name, email, password, phone) values (?, ?, ?, ?)  RETURNING user_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, phone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("user_id");
        }
        return -1;
    }
    public int loginUser(String email, String password) throws SQLException {
        String sql = "select user_id from users where email = ? and password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("user_id");
        }
        return -1;
    }
}