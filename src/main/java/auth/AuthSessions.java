package auth;
import db.DBConnection;
import jakarta.servlet.http.*;
import java.sql.*;
import java.util.UUID;
public final class AuthSessions {
    private static void cookie(HttpServletResponse res, String token, boolean remove) {
        res.addHeader("Set-Cookie", "SC_AUTH=" + token + "; Path=/; HttpOnly; SameSite=Lax; Max-Age=" + (remove ? "0" : "86400"));
    }
    public static String token(HttpServletRequest req) {
        if (req.getCookies() != null) for (Cookie c : req.getCookies()) if (c.getName().equals("SC_AUTH")) return c.getValue();
        return null;
    }
    public static void signIn(HttpServletRequest req, HttpServletResponse res, int userId) throws SQLException {
        signOut(req, res);
        String token = UUID.randomUUID().toString();
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("INSERT INTO app_sessions(token,user_id,expires_at) VALUES (?, ?, CURRENT_TIMESTAMP + INTERVAL '1 day')")) {
            p.setString(1, token); p.setInt(2,userId); p.executeUpdate();
        }
        req.getSession().setAttribute("user_id", userId); cookie(res, token, false);
    }
    public static void signOut(HttpServletRequest req, HttpServletResponse res) throws SQLException {
        String token = token(req);
        if (token != null) try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("DELETE FROM app_sessions WHERE token=?")) { p.setString(1,token); p.executeUpdate(); }
        if (req.getSession(false) != null) req.getSession(false).invalidate();
        cookie(res, "", true);
    }
}
