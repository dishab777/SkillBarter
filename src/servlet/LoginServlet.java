package servlet;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import model.Student;
import util.JsonUtil;
import util.PasswordUtil;

/**
 * Servlet handling user login and session creation.
 * Route: /api/login
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/api/login", "/login"})
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || password == null) {
            String body = readBody(request);
            Map<String, String> jsonMap = JsonUtil.parseSimpleJson(body);
            if (jsonMap.containsKey("email")) email = jsonMap.get("email");
            if (jsonMap.containsKey("password")) password = jsonMap.get("password");
        }

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.failure("Email and password are required."));
            return;
        }

        try {
            Student student = userDAO.findByEmail(email.trim());

            if (student == null || !PasswordUtil.verifyPassword(password, student.getPasswordHash())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(JsonUtil.failure("Invalid email or password."));
                return;
            }

            // Create or invalidate and create session
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", student.getUserID());
            session.setAttribute("userName", student.getName());
            session.setAttribute("userEmail", student.getEmail());
            session.setAttribute("department", student.getDepartment());
            session.setAttribute("semester", student.getSemester());
            session.setAttribute("role", student.getRole());

            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", student.getUserID());
            userData.put("name", student.getName());
            userData.put("email", student.getEmail());
            userData.put("department", student.getDepartment());
            userData.put("semester", student.getSemester());
            userData.put("role", student.getRole());

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(JsonUtil.success("Login successful!", userData));

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.failure("Database error occurred during login."));
        }
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
