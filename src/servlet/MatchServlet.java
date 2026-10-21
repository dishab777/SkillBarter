package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import service.SkillExchangeService;
import util.JsonUtil;

/**
 * Servlet handling match suggestions, requesting exchanges, status transitions,
 * and rating completed exchanges.
 * Routes: /api/matches, /api/matches/suggestions, /api/matches/status, /api/matches/rating
 */
@WebServlet(name = "MatchServlet", urlPatterns = {
    "/api/matches",
    "/api/matches/suggestions",
    "/api/matches/status",
    "/api/matches/rating"
})
public class MatchServlet extends HttpServlet {

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
            if ("/api/matches/suggestions".equals(path)) {
                // Find reciprocal matching partners
                List<Map<String, Object>> suggestions = skillService.findMatchSuggestions(userId);
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(JsonUtil.success("Match suggestions retrieved.", suggestions));
            } else {
                // List student's existing matches
                List<Map<String, Object>> matches = skillService.getStudentMatches(userId);
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(JsonUtil.success("Matches retrieved.", matches));
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.failure("Database error while retrieving matches: " + e.getMessage()));
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

        String body = readBody(request);
        Map<String, String> jsonMap = JsonUtil.parseSimpleJson(body);

        if ("/api/matches/rating".equals(path) || "rating".equalsIgnoreCase(request.getParameter("action"))) {
            handleRating(request, response, userId, jsonMap, out);
        } else if ("/api/matches/status".equals(path) || "status".equalsIgnoreCase(request.getParameter("action"))) {
            handleStatusUpdate(request, response, userId, jsonMap, out);
        } else {
            // Create a new match request
            handleCreateMatch(request, response, userId, jsonMap, out);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
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
        String body = readBody(request);
        Map<String, String> jsonMap = JsonUtil.parseSimpleJson(body);

        handleStatusUpdate(request, response, userId, jsonMap, out);
    }

    private void handleCreateMatch(HttpServletRequest request, HttpServletResponse response, int userId,
                                   Map<String, String> jsonMap, PrintWriter out) {

        String partnerIdStr = request.getParameter("partnerId");
        String skillName = request.getParameter("skillName");

        if (partnerIdStr == null && jsonMap.containsKey("partnerId")) {
            partnerIdStr = jsonMap.get("partnerId");
        }
        if (skillName == null && jsonMap.containsKey("skillName")) {
            skillName = jsonMap.get("skillName");
        }

        if (partnerIdStr == null || skillName == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.failure("Partner ID and skill name are required."));
            return;
        }

        try {
            int partnerId = Integer.parseInt(partnerIdStr.trim());
            int matchId = skillService.requestMatch(userId, partnerId, skillName);

            if (matchId > 0) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(JsonUtil.success("Skill exchange request sent successfully!", null));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(JsonUtil.failure("Failed to send match request."));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.failure("Invalid partner ID format."));
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.failure(e.getMessage()));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.failure("Database error while requesting match."));
        }
    }

    private void handleStatusUpdate(HttpServletRequest request, HttpServletResponse response, int userId,
                                    Map<String, String> jsonMap, PrintWriter out) {

        String matchIdStr = request.getParameter("matchId");
        String status = request.getParameter("status");

        if (matchIdStr == null && jsonMap.containsKey("matchId")) {
            matchIdStr = jsonMap.get("matchId");
        }
        if (status == null && jsonMap.containsKey("status")) {
            status = jsonMap.get("status");
        }

        if (matchIdStr == null || status == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.failure("Match ID and status are required."));
            return;
        }

        try {
            int matchId = Integer.parseInt(matchIdStr.trim());
            boolean updated = skillService.updateMatchStatus(matchId, status, userId);

            if (updated) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(JsonUtil.success("Match status updated to " + status.toUpperCase() + ".", null));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(JsonUtil.failure("Failed to update match status."));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.failure("Invalid match ID."));
        } catch (SecurityException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print(JsonUtil.failure(e.getMessage()));
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.failure(e.getMessage()));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.failure("Database error while updating match status."));
        }
    }

    private void handleRating(HttpServletRequest request, HttpServletResponse response, int userId,
                              Map<String, String> jsonMap, PrintWriter out) {

        String matchIdStr = request.getParameter("matchId");
        String ratingStr = request.getParameter("rating");

        if (matchIdStr == null && jsonMap.containsKey("matchId")) {
            matchIdStr = jsonMap.get("matchId");
        }
        if (ratingStr == null && jsonMap.containsKey("rating")) {
            ratingStr = jsonMap.get("rating");
        }

        if (matchIdStr == null || ratingStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.failure("Match ID and rating (1-5) are required."));
            return;
        }

        try {
            int matchId = Integer.parseInt(matchIdStr.trim());
            int rating = Integer.parseInt(ratingStr.trim());

            boolean rated = skillService.submitRating(matchId, userId, rating);
            if (rated) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(JsonUtil.success("Exchange rating submitted successfully!", null));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(JsonUtil.failure("Failed to submit rating."));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.failure("Invalid match ID or rating number."));
        } catch (SecurityException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print(JsonUtil.failure(e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.failure(e.getMessage()));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.failure("Error while submitting rating."));
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
