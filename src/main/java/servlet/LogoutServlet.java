package servlet;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try { auth.AuthSessions.signOut(req,res); res.sendRedirect("login.jsp"); }
        catch (java.sql.SQLException e) { throw new ServletException(e); }
    }
}
