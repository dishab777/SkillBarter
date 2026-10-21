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

/**
 * Servlet handling student profile retrieval and updates.
 * Route: /api/profile
 */
@WebServlet(name = "ProfileServlet", urlPatterns = {"/api/profile", "/profile"})
public class ProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(JsonUtil.failure("Unauthorized. Please log in to view your profile."));
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        try {
            Student student = userDAO.findById(userId);
            if (student == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(JsonUtil.failure("Student record not found."));
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("userId", student.getUserID());
            data.put("name", student.getName());
            data.put("email", student.getEmail());
            data.put("department", student.getDepartment());
            data.put("semester", student.getSemester());
            data.put("role", student.getRole());

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(JsonUtil.success("Profile retrieved.", data));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.failure("Database error retrieving profile."));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        updateProfile(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        updateProfile(request, response);
    }

    private void updateProfile(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(JsonUtil.failure("Unauthorized. Please log in to update your profile."));
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        String name = request.getParameter("name");
        String department = request.getParameter("department");
        String semesterStr = request.getParameter("semester");

        if (name == null || department == null || semesterStr == null) {
            String body = readBody(request);
            Map<String, String> jsonMap = JsonUtil.parseSimpleJson(body);
            if (jsonMap.containsKey("name")) name = jsonMap.get("name");
            if (jsonMap.containsKey("department")) department = jsonMap.get("department");
            if (jsonMap.containsKey("semester")) semesterStr = jsonMap.get("semester");
        }

        if (name == null || name.trim().isEmpty() ||
            department == null || department.trim().isEmpty() ||
            semesterStr == null || semesterStr.trim().isEmpty()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.failure("Name, department, and semester are required to update profile."));
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

        try {
            boolean updated = userDAO.updateProfile(userId, name, department, semester);
            if (updated) {
                session.setAttribute("userName", name.trim());
                session.setAttribute("department", department.trim());
                session.setAttribute("semester", semester);

                Map<String, Object> data = new HashMap<>();
                data.put("userId", userId);
                data.put("name", name.trim());
                data.put("department", department.trim());
                data.put("semester", semester);

                response.setStatus(HttpServletResponse.SC_OK);
                out.print(JsonUtil.success("Profile updated successfully.", data));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(JsonUtil.failure("Failed to update profile."));
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.failure("Database error while updating profile."));
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
