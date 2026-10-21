package servlet;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;
import model.Student;
import util.JsonUtil;
import util.PasswordUtil;

/**
 * Servlet handling student registration.
 * Route: /api/register
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/api/register", "/register"})
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String department = request.getParameter("department");
        String semesterStr = request.getParameter("semester");

        // Support JSON request body if form parameters were not provided
        if (name == null || email == null || password == null) {
            String body = readBody(request);
            Map<String, String> jsonMap = JsonUtil.parseSimpleJson(body);
            if (jsonMap.containsKey("name")) name = jsonMap.get("name");
            if (jsonMap.containsKey("email")) email = jsonMap.get("email");
            if (jsonMap.containsKey("password")) password = jsonMap.get("password");
            if (jsonMap.containsKey("department")) department = jsonMap.get("department");
            if (jsonMap.containsKey("semester")) semesterStr = jsonMap.get("semester");
        }

        // Validate mandatory input
        if (name == null || name.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            department == null || department.trim().isEmpty() ||
            semesterStr == null || semesterStr.trim().isEmpty()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.failure("All fields are required: name, email, password, department, semester."));
            return;
        }

        int semester;
        try {
            semester = Integer.parseInt(semesterStr.trim());
            if (semester < 1 || semester > 12) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(JsonUtil.failure("Semester must be between 1 and 12."));
                return;
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.failure("Invalid semester format."));
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.failure("Please provide a valid email address."));
            return;
        }

        if (password.length() < 6) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.failure("Password must be at least 6 characters long."));
            return;
        }

        try {
            if (userDAO.emailExists(email)) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print(JsonUtil.failure("An account with this email already exists."));
                return;
            }

            // Secure password hashing with PBKDF2
            String passwordHash = PasswordUtil.hashPassword(password);
            Student student = new Student(0, name, email, passwordHash, department, semester);

            int userId = userDAO.registerUser(student);
            if (userId > 0) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(JsonUtil.success("Registration successful! You can now log in.", null));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(JsonUtil.failure("Could not register user. Please try again."));
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.failure("Database error occurred during registration."));
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
