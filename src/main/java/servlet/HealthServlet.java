package servlet;
import db.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
@WebServlet(urlPatterns={"/health/live", "/health/ready", "/demo/products", "/version"})
public class HealthServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        String path = req.getServletPath();
        if (path.equals("/health/live")) { res.getWriter().print("{\"status\":\"alive\"}"); return; }
        if (path.equals("/version")) {
            String version = System.getenv().getOrDefault("APP_VERSION", "dev").replaceAll("[^a-zA-Z0-9_.-]", "_");
            res.getWriter().print("{\"version\":\"" + version + "\"}"); return;
        }
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement()) {
            if (path.equals("/health/ready")) {
                try (ResultSet rs = s.executeQuery("SELECT 1")) { rs.next(); }
                res.getWriter().print("{\"status\":\"ready\"}");
            } else {
                try (ResultSet rs = s.executeQuery("SELECT count(*) FROM product")) {
                    rs.next(); res.getWriter().print("{\"product_count\":" + rs.getInt(1) + "}");
                }
            }
        } catch (SQLException e) { res.setStatus(503); res.getWriter().print("{\"error\":\"database unavailable\"}"); }
    }
}
