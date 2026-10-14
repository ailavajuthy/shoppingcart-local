package servlet;
import dao.ProductDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;
import java.util.Map;
@WebServlet("/products")
public class ProductServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if (req.getSession().getAttribute("user_id") == null) {
            res.sendRedirect("login.jsp");
            return;
        }
        try {
            ProductDAO productDAO = new ProductDAO();
            String category = req.getParameter("category");
            String search=req.getParameter("search");
            List<Map<String, Object>> products;

            if((search!=null&&!search.trim().isEmpty())){
                products=productDAO.searchProducts(search.trim(), category);
            }
            else if(category==null||category.equals("all")){
                products=productDAO.getAllProducts();
            }
            else{
                products = productDAO.getProductsByCategory(category);
            }
            String error=(String)req.getSession().getAttribute("error");
            if(error!=null){
                req.setAttribute("error",error);
                req.getSession().removeAttribute("error");
            }
            req.setAttribute("products", products);
            req.getRequestDispatcher("products.jsp").forward(req, res);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}