package servlet;
import dao.CartDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;
import java.util.Map;
@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        if (session.getAttribute("user_id") == null) {
            res.sendRedirect("login.jsp");
            return;
        }
        int userId = (int) session.getAttribute("user_id");
        try {
            CartDAO cartDAO = new CartDAO();
            List<Map<String, Object>> items = cartDAO.getCartItems(userId);
            double total = cartDAO.getCartTotal(userId);
            req.setAttribute("cartItems", items);
            req.setAttribute("cartTotal", total);
            req.getRequestDispatcher("cart.jsp").forward(req, res);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        if (session.getAttribute("user_id") == null) {
            res.sendRedirect("login.jsp");
            return;
        }
        int userId = (int) session.getAttribute("user_id");
        String action = req.getParameter("action");
        try {
            CartDAO cartDAO = new CartDAO();
            if ("add".equals(action)) {
                int productId = Integer.parseInt(req.getParameter("product_id"));
                int quantity = Integer.parseInt(req.getParameter("quantity"));
                int existingQty=cartDAO.getQuantity(userId,productId);
                int stock=cartDAO.getStock(productId);
                if(existingQty+quantity>stock){
                    session.setAttribute("error","Cannot add more than available stock");
                    res.sendRedirect("products");
                    return;
                }
                cartDAO.addToCart(userId, productId, quantity);
                res.sendRedirect("products");
            } else if ("remove".equals(action)) {
                int productId = Integer.parseInt(req.getParameter("product_id"));
                cartDAO.removeFromCart(userId, productId);
                res.sendRedirect("cart");
            } else if ("update".equals(action)) {
                int productId = Integer.parseInt(req.getParameter("product_id"));
                int quantity = Integer.parseInt(req.getParameter("quantity"));
                cartDAO.updateQuantity(userId, productId, quantity);
                res.sendRedirect("cart");
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}