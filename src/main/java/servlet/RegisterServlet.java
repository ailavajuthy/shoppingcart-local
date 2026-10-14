package servlet;
import dao.UserDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String phone = req.getParameter("phone");
        if (phone==null||!phone.matches("^[0-9]{10,15}$")){
            req.setAttribute("error", "Phone number must be 10–15 digits.");
            req.getRequestDispatcher("register.jsp").forward(req, res);
            return;
        }
        try {
            UserDAO userDAO = new UserDAO();
            int userId = userDAO.registerUser(name, email, password, phone);
            if (userId != -1) {
                HttpSession session = req.getSession();
                auth.AuthSessions.signIn(req, res, userId);
                res.sendRedirect("products");
            } else {
                req.setAttribute("error", "Registration failed. Email or phone may already exist.");
                req.getRequestDispatcher("register.jsp").forward(req, res);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg="Registration failed. Please try again.";
            if(e.getMessage()!=null){
                if (e.getMessage().contains("chk_phone")){
                    errorMsg = "Invalid phone number format.";
                } else if (e.getMessage().contains("users_email_key")){
                    errorMsg = "Email already exists.";
                } else if (e.getMessage().contains("users_phone_key")){
                    errorMsg = "Phone number already exists.";
                }
                req.setAttribute("error", errorMsg);
                req.getRequestDispatcher("register.jsp").forward(req, res);
            }
        }
    }
}