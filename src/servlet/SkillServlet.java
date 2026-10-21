package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Skill;
import service.SkillExchangeService;
import util.JsonUtil;

/**
 * Servlet handling skill management: adding, listing, discovering, and deleting.
 * Routes: /api/skills, /api/skills/discover, /api/skills/delete
 */
@WebServlet(name = "SkillServlet", urlPatterns = {"/api/skills", "/api/skills/discover", "/api/skills/delete"})
public class SkillServlet extends HttpServlet {

    private final SkillExchangeService skillService = new SkillExchangeService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(JsonUtil.failure("Unauthorized. Please log in first."));
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String path = request.getServletPath();

        try {
            if ("/api/skills/discover".equals(path)) {
                // List discoverable skills offered by peers
                List<Map<String, Object>> discoverable = skillService.getDiscoverableSkills(userId);
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(JsonUtil.success("Discoverable skills retrieved.", discoverable));
            } else {
                // List current user's skills
                Map<String, List<Skill>> categorized = skillService.getCategorizedSkills(userId);

                // Convert models to maps for clean JSON output
                Map<String, Object> data = new HashMap<>();
                List<Map<String, Object>> teachList = new ArrayList<>();
                for (Skill s : categorized.get("TEACH")) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("skillID", s.getSkillID());
                    m.put("skillName", s.getSkillName());
                    m.put("skillType", s.getSkillType());
                    teachList.add(m);
                }

                List<Map<String, Object>> learnList = new ArrayList<>();
                for (Skill s : categorized.get("LEARN")) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("skillID", s.getSkillID());
                    m.put("skillName", s.getSkillName());
                    m.put("skillType", s.getSkillType());
                    learnList.add(m);
                }

                data.put("teachSkills", teachList);
                data.put("learnSkills", learnList);

                response.setStatus(HttpServletResponse.SC_OK);
                out.print(JsonUtil.success("User skills retrieved.", data));
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.failure("Database error while retrieving skills."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(JsonUtil.failure("Unauthorized. Please log in first."));
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String path = request.getServletPath();

        // Check if this is a delete request routed via POST
        if ("/api/skills/delete".equals(path) || "delete".equalsIgnoreCase(request.getParameter("action"))) {
            handleDelete(request, response, userId, out);
            return;
        }

        // Add a skill
        String skillName = request.getParameter("skillName");
        String skillType = request.getParameter("skillType");

        if (skillName == null || skillType == null) {
            String body = readBody(request);
            Map<String, String> jsonMap = JsonUtil.parseSimpleJson(body);
            if (jsonMap.containsKey("skillName")) skillName = jsonMap.get("skillName");
            if (jsonMap.containsKey("skillType")) skillType = jsonMap.get("skillType");
        }

        if (skillName == null || skillName.trim().isEmpty() ||
            skillType == null || skillType.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.failure("Skill name and skill type (TEACH or LEARN) are required."));
            return;
        }

        try {
            int skillId = skillService.addSkill(userId, skillName.trim(), skillType.trim());
            if (skillId > 0) {
                Map<String, Object> result = new HashMap<>();
                result.put("skillID", skillId);
                result.put("skillName", skillName.trim());
                result.put("skillType", skillType.toUpperCase().trim());

                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(JsonUtil.success("Skill added successfully.", result));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(JsonUtil.failure("Could not add skill."));
            }
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.failure(e.getMessage()));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.failure("Database error while adding skill."));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(JsonUtil.failure("Unauthorized. Please log in first."));
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        handleDelete(request, response, userId, out);
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response, int userId, PrintWriter out) {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            idStr = request.getParameter("skillId");
        }

        if (idStr == null || idStr.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.failure("Skill ID is required for deletion."));
            return;
        }

        try {
            int skillId = Integer.parseInt(idStr.trim());
            boolean deleted = skillService.removeSkill(skillId, userId);
            if (deleted) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(JsonUtil.success("Skill removed successfully.", null));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(JsonUtil.failure("Skill not found or you are not authorized to delete it."));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.failure("Invalid skill ID."));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.failure("Database error while removing skill."));
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
