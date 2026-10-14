package db;
import java.sql.*;
public class DBConnection {
    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }
    public static Connection getConnection() throws SQLException {
        try { Class.forName("org.postgresql.Driver"); }
        catch (ClassNotFoundException e) { throw new SQLException("PostgreSQL driver not found", e); }
        return DriverManager.getConnection(env("DB_URL", "jdbc:postgresql://localhost:5432/shopping_cart_db?connectTimeout=3&socketTimeout=5"),
                env("DB_USER", "shoppingcart"), env("DB_PASSWORD", "local-demo-password"));
    }
}
