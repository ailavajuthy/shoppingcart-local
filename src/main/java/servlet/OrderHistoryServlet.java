package servlet;
import dao.OrderDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;
import java.util.Map;
@WebServlet("/orders")
public class OrderHistoryServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        if (session.getAttribute("user_id") == null) {
            res.sendRedirect("login.jsp");
            return;
        }
        int userId = (int) session.getAttribute("user_id");
        try {
            OrderDAO orderDAO = new OrderDAO();
            List<Map<String, Object>> orders = orderDAO.getOrderHistory(userId);
            req.setAttribute("orders", orders);
            req.getRequestDispatcher("orders.jsp").forward(req, res);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}