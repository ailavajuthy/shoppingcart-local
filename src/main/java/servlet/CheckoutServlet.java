package servlet;
import dao.OrderDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        if (session.getAttribute("user_id") == null) {
            res.sendRedirect("login.jsp");
            return;
        }
        int userId = (int) session.getAttribute("user_id");
        try {
            OrderDAO orderDAO = new OrderDAO();
            String result = orderDAO.checkout(userId);
            if (result.startsWith("SUCCESS")) {
                res.sendRedirect("orders");
            } else {
                req.setAttribute("error", result);
                req.getRequestDispatcher("cart.jsp").forward(req, res);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
