package auth;
import db.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
public class AuthFilter implements Filter {
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getServletPath();
        if (path.startsWith("/health/") || path.equals("/metrics") || path.startsWith("/demo/") || path.equals("/version")) { chain.doFilter(request,response); return; }
        // Auth lives in PostgreSQL, so a different replica can serve the next request.
        req.getSession().removeAttribute("user_id");
        String token = AuthSessions.token(req);
        if (token != null) try (Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement("SELECT user_id FROM app_sessions WHERE token=? AND expires_at>CURRENT_TIMESTAMP")) {
            p.setString(1,token); try(ResultSet rs=p.executeQuery()) { if(rs.next()) req.getSession().setAttribute("user_id",rs.getInt(1)); }
        } catch(SQLException e) { throw new ServletException("Session database unavailable",e); }
        chain.doFilter(request,response);
    }
}
