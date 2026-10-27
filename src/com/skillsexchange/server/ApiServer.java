package com.skillsexchange.server;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import com.skillsexchange.database.DBConnection;
import com.skillsexchange.database.StudentDAO;
import com.skillsexchange.model.Rating;
import com.skillsexchange.model.Session;
import com.skillsexchange.model.Skill;
import com.skillsexchange.model.Student;
import com.skillsexchange.service.SkillExchangeSystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApiServer {

    private static final int DEFAULT_PORT = 8080;
    private static final Gson gson = new Gson();
    private static final StudentDAO studentDAO = new StudentDAO();
    private static final SkillExchangeSystem system = new SkillExchangeSystem();

    static {
        loadStudentsFromDatabase();
    }

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;

        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port supplied. Falling back to 8080.");
            }
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api", ApiServer::handleApi);
        server.setExecutor(null);
        server.start();

        System.out.println("==================================");
        System.out.println("SkillBarter API server is running");
        System.out.println("API base: http://localhost:" + port + "/api");
        System.out.println("Press Ctrl+C to stop the server.");
        System.out.println("==================================");
    }

    private static void handleApi(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {
            if ("/api/health".equals(path)) {
                handleHealth(exchange, method);
            } else if ("/api/students".equals(path)) {
                handleStudents(exchange, method);
            } else if (path != null && path.startsWith("/api/students/")) {
                handleStudentDetail(exchange, method, path);
            } else if ("/api/mentors".equals(path)) {
                handleMentors(exchange, method);
            } else if ("/api/sessions".equals(path)) {
                handleSessions(exchange, method);
            } else if (path != null && path.startsWith("/api/sessions/")) {
                handleSessionDetail(exchange, method, path);
            } else if ("/api/ratings".equals(path)) {
                handleRatings(exchange, method);
            } else {
                sendError(exchange, 404, "Endpoint not found.");
            }
        } catch (IllegalArgumentException e) {
            sendError(exchange, 400, e.getMessage());
        } catch (IllegalStateException e) {
            sendError(exchange, 409, e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            sendError(exchange, 500, "Database error while processing the request.");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "The server could not complete the request.");
        }
    }

    private static void handleHealth(HttpExchange exchange, String method) throws IOException {
        if (!"GET".equalsIgnoreCase(method)) {
            sendError(exchange, 405, "GET is the only supported method for /api/health.");
            return;
        }

        sendJson(exchange, 200, successResponse("SkillBarter API is running.", Map.of("status", "ok")));
    }

    private static void handleStudents(HttpExchange exchange, String method)
            throws IOException, SQLException {

        if ("GET".equalsIgnoreCase(method)) {
            String query = exchange.getRequestURI().getRawQuery();

            if (query != null && !query.isEmpty()) {
                Map<String, String> params = parseQuery(query);
                if (params.containsKey("id")) {
                    int studentId = parsePositiveInt(params.get("id"), "Student ID");
                    Student student = system.getStudent(studentId);
                    if (student == null) {
                        sendError(exchange, 404, "Student not found.");
                        return;
                    }

                    sendJson(exchange, 200, successResponse("Student retrieved successfully.", studentToMap(student)));
                    return;
                }
            }

            List<Map<String, Object>> students = new ArrayList<>();
            for (Student student : system.getAllStudents()) {
                students.add(studentToMap(student));
            }
            sendJson(exchange, 200, successResponse("Students retrieved successfully.", students));
            return;
        }

        if ("POST".equalsIgnoreCase(method)) {
            StudentRequest request = readJsonBody(exchange, StudentRequest.class);
            if (request == null) {
                sendError(exchange, 400, "Request body is required.");
                return;
            }

            if (request.studentId == null || request.studentId <= 0) {
                sendError(exchange, 400, "Student ID is required.");
                return;
            }

            if (request.name == null || request.name.trim().isEmpty()) {
                sendError(exchange, 400, "Student name is required.");
                return;
            }

            if (request.email == null || request.email.trim().isEmpty()) {
                sendError(exchange, 400, "Student email is required.");
                return;
            }

            String name = request.name.trim();
            String email = request.email.trim();

            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                sendError(exchange, 400, "Enter a valid email address.");
                return;
            }

            if (system.getStudent(request.studentId) != null) {
                sendError(exchange, 409, "A student with this ID already exists.");
                return;
            }

            Student student = new Student(request.studentId, name, email);

            if (DBConnection.isConfigured()) {
                try {
                    studentDAO.addStudent(student);
                } catch (SQLException sqlException) {
                    if ("23505".equals(sqlException.getSQLState())) {
                        sendError(exchange, 409, "A student with this ID or email already exists.");
                        return;
                    }
                    throw sqlException;
                }
            }

            system.registerStudent(student);
            sendJson(exchange, 201, successResponse("Student registered successfully.", studentToMap(student)));
            return;
        }

        sendError(exchange, 405, "Supported methods for /api/students are GET and POST.");
    }

    private static void handleStudentDetail(HttpExchange exchange, String method, String path)
            throws IOException, SQLException {

        String studentIdText = path.substring("/api/students/".length()).trim();
        if (studentIdText.isEmpty()) {
            sendError(exchange, 404, "Student path is missing an ID.");
            return;
        }

        if (studentIdText.contains("/skills")) {
            handleStudentSkill(exchange, method, studentIdText);
            return;
        }

        int studentId = parsePositiveInt(studentIdText, "Student ID");
        Student student = system.getStudent(studentId);

        if (student == null) {
            sendError(exchange, 404, "Student not found.");
            return;
        }

        if ("PUT".equalsIgnoreCase(method)) {
            StudentUpdateRequest request = readJsonBody(exchange, StudentUpdateRequest.class);
            if (request == null) {
                sendError(exchange, 400, "Request body is required.");
                return;
            }

            if (request.name == null || request.name.trim().isEmpty()) {
                sendError(exchange, 400, "Student name is required.");
                return;
            }

            if (request.email == null || request.email.trim().isEmpty()) {
                sendError(exchange, 400, "Student email is required.");
                return;
            }

            String trimmedName = request.name.trim();
            String trimmedEmail = request.email.trim();

            if (!trimmedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                sendError(exchange, 400, "Enter a valid email address.");
                return;
            }

            system.updateStudent(studentId, trimmedName, trimmedEmail);
            if (DBConnection.isConfigured()) {
                studentDAO.updateStudent(studentId, trimmedName, trimmedEmail);
            }
            Student updated = system.getStudent(studentId);
            sendJson(exchange, 200, successResponse("Student updated successfully.", studentToMap(updated)));
            return;
        }

        sendError(exchange, 405, "Supported methods for /api/students/{id} are PUT.");
    }

    private static void handleStudentSkill(HttpExchange exchange, String method, String studentPath)
            throws IOException, SQLException {

        String studentIdText = studentPath.substring(0, studentPath.indexOf("/skills"));
        int studentId = parsePositiveInt(studentIdText, "Student ID");
        Student student = system.getStudent(studentId);

        if (student == null) {
            sendError(exchange, 404, "Student not found.");
            return;
        }

        if (!"POST".equalsIgnoreCase(method)) {
            sendError(exchange, 405, "POST is required for /api/students/{id}/skills.");
            return;
        }

        StudentSkillRequest request = readJsonBody(exchange, StudentSkillRequest.class);
        if (request == null) {
            sendError(exchange, 400, "Request body is required.");
            return;
        }

        if (request.skillName == null || request.skillName.trim().isEmpty()) {
            sendError(exchange, 400, "Skill name is required.");
            return;
        }

        if (request.category == null || request.category.trim().isEmpty()) {
            sendError(exchange, 400, "Skill category is required.");
            return;
        }

        Skill.Level level = parseSkillLevel(request.level);
        String skillName = request.skillName.trim();
        String category = request.category.trim();
        String learningGoal = request.learningGoal == null ? "" : request.learningGoal.trim();

        int nextSkillId = 1;
        for (Skill offered : student.getOfferedSkills()) {
            nextSkillId = Math.max(nextSkillId, offered.getSkillId() + 1);
        }
        for (Skill wanted : student.getWantedSkills()) {
            nextSkillId = Math.max(nextSkillId, wanted.getSkillId() + 1);
        }

        Skill skill = new Skill(nextSkillId, skillName, category, level, learningGoal);
        system.addSkillToStudent(studentId, skill, request.offered != null && request.offered);
        sendJson(exchange, 201, successResponse("Skill added successfully.", studentToMap(student)));
    }

    private static void handleMentors(HttpExchange exchange, String method) throws IOException {
        if (!"GET".equalsIgnoreCase(method)) {
            sendError(exchange, 405, "GET is the only supported method for /api/mentors.");
            return;
        }

        String query = exchange.getRequestURI().getRawQuery();
        Map<String, String> params = query == null ? new LinkedHashMap<>() : parseQuery(query);
        String skillName = params.get("skillName");
        String learnerIdText = params.get("learnerId");

        if (skillName == null || skillName.trim().isEmpty()) {
            sendError(exchange, 400, "skillName is required.");
            return;
        }

        if (learnerIdText == null || learnerIdText.trim().isEmpty()) {
            sendError(exchange, 400, "learnerId is required.");
            return;
        }

        int learnerId = parsePositiveInt(learnerIdText, "learnerId");
        List<Map<String, Object>> matches = new ArrayList<>();
        for (var match : system.findMentors(skillName.trim(), learnerId)) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("mentor", studentToMap(match.getMentor()));
            entry.put("skill", skillToMap(match.getSkill()));
            entry.put("matchScore", match.getMatchScore());
            matches.add(entry);
        }

        sendJson(exchange, 200, successResponse("Mentor matches retrieved successfully.", matches));
    }

    private static void handleSessions(HttpExchange exchange, String method) throws IOException {
        if ("GET".equalsIgnoreCase(method)) {
            List<Map<String, Object>> sessions = new ArrayList<>();
            for (Session session : system.getAllSessions()) {
                sessions.add(sessionToMap(session));
            }
            sendJson(exchange, 200, successResponse("Sessions retrieved successfully.", sessions));
            return;
        }

        if ("POST".equalsIgnoreCase(method)) {
            SessionRequest request = readJsonBody(exchange, SessionRequest.class);
            if (request == null) {
                sendError(exchange, 400, "Request body is required.");
                return;
            }

            if (request.mentorId == null || request.learnerId == null) {
                sendError(exchange, 400, "mentorId and learnerId are required.");
                return;
            }

            if (request.skillName == null || request.skillName.trim().isEmpty()) {
                sendError(exchange, 400, "Skill name is required.");
                return;
            }

            if (request.date == null || request.date.trim().isEmpty()) {
                sendError(exchange, 400, "Session date is required.");
                return;
            }

            if (request.time == null || request.time.trim().isEmpty()) {
                sendError(exchange, 400, "Session time is required.");
                return;
            }

            Session session = system.scheduleSession(
                    request.mentorId,
                    request.learnerId,
                    request.skillName.trim(),
                    request.date.trim(),
                    request.time.trim()
            );
            sendJson(exchange, 201, successResponse("Session scheduled successfully.", sessionToMap(session)));
            return;
        }

        sendError(exchange, 405, "Supported methods for /api/sessions are GET and POST.");
    }

    private static void handleSessionDetail(HttpExchange exchange, String method, String path)
            throws IOException {

        String sessionIdText = path.substring("/api/sessions/".length()).trim();
        if (sessionIdText.isEmpty()) {
            sendError(exchange, 404, "Session path is missing an ID.");
            return;
        }

        if (sessionIdText.contains("/complete")) {
            handleCompleteSession(exchange, method, sessionIdText);
            return;
        }

        if (sessionIdText.contains("/ratings")) {
            handleSessionRating(exchange, method, sessionIdText);
            return;
        }

        sendError(exchange, 404, "Unsupported session route.");
    }

    private static void handleCompleteSession(HttpExchange exchange, String method, String sessionPath)
            throws IOException {

        String sessionIdText = sessionPath.substring(0, sessionPath.indexOf("/complete"));
        int sessionId = parsePositiveInt(sessionIdText, "Session ID");

        if (!"PUT".equalsIgnoreCase(method)) {
            sendError(exchange, 405, "PUT is required for /api/sessions/{id}/complete.");
            return;
        }

        Session session = system.getSession(sessionId);
        if (session == null) {
            sendError(exchange, 404, "Session not found.");
            return;
        }

        system.completeSession(sessionId);
        sendJson(exchange, 200, successResponse("Session marked complete.", sessionToMap(system.getSession(sessionId))));
    }

    private static void handleSessionRating(HttpExchange exchange, String method, String sessionPath)
            throws IOException {

        String sessionIdText = sessionPath.substring(0, sessionPath.indexOf("/ratings"));
        int sessionId = parsePositiveInt(sessionIdText, "Session ID");

        if (!"POST".equalsIgnoreCase(method)) {
            sendError(exchange, 405, "POST is required for /api/sessions/{id}/ratings.");
            return;
        }

        RatingRequest request = readJsonBody(exchange, RatingRequest.class);
        if (request == null) {
            sendError(exchange, 400, "Request body is required.");
            return;
        }

        if (request.score == null || request.score < 1 || request.score > 5) {
            sendError(exchange, 400, "Rating must be between 1 and 5.");
            return;
        }

        String feedback = request.feedback == null ? "" : request.feedback.trim();
        Rating rating = system.rateMentor(sessionId, request.score, feedback);
        sendJson(exchange, 201, successResponse("Rating added successfully.", ratingToMap(rating)));
    }

    private static void handleRatings(HttpExchange exchange, String method) throws IOException {
        if (!"GET".equalsIgnoreCase(method)) {
            sendError(exchange, 405, "GET is the only supported method for /api/ratings.");
            return;
        }

        List<Map<String, Object>> ratings = new ArrayList<>();
        for (Rating rating : system.getAllRatings()) {
            ratings.add(ratingToMap(rating));
        }
        sendJson(exchange, 200, successResponse("Ratings retrieved successfully.", ratings));
    }

    private static Map<String, Object> successResponse(String message, Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private static Map<String, Object> studentToMap(Student student) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", student.getUserId());
        result.put("name", student.getName());
        result.put("email", student.getEmail());
        result.put("offeredSkills", skillsToMap(student.getOfferedSkills()));
        result.put("wantedSkills", skillsToMap(student.getWantedSkills()));
        result.put("ratings", ratingsToMap(student.getRatings()));
        return result;
    }

    private static List<Map<String, Object>> skillsToMap(List<Skill> skills) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Skill skill : skills) {
            result.add(skillToMap(skill));
        }
        return result;
    }

    private static Map<String, Object> skillToMap(Skill skill) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("skillId", skill.getSkillId());
        result.put("skillName", skill.getSkillName());
        result.put("category", skill.getCategory());
        result.put("level", skill.getLevel() == null ? "NONE" : skill.getLevel().name());
        result.put("learningGoal", skill.getLearningGoal());
        return result;
    }

    private static List<Map<String, Object>> ratingsToMap(List<Rating> ratings) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Rating rating : ratings) {
            result.add(ratingToMap(rating));
        }
        return result;
    }

    private static Map<String, Object> ratingToMap(Rating rating) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ratingId", rating.getRatingId());
        result.put("fromStudent", studentSummary(rating.getFromStudent()));
        result.put("toStudent", studentSummary(rating.getToStudent()));
        result.put("score", rating.getScore());
        result.put("feedback", rating.getFeedback());
        return result;
    }

    private static Map<String, Object> studentSummary(Student student) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", student.getUserId());
        result.put("name", student.getName());
        result.put("email", student.getEmail());
        return result;
    }

    private static Map<String, Object> sessionToMap(Session session) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", session.getSessionId());
        result.put("mentor", studentToMap(session.getMentor()));
        result.put("learner", studentToMap(session.getLearner()));
        result.put("skill", skillToMap(session.getSkill()));
        result.put("date", session.getDate());
        result.put("time", session.getTime());
        result.put("status", session.getStatus().name());
        return result;
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new LinkedHashMap<>();
        for (String pair : query.split("&")) {
            if (pair == null || pair.isEmpty()) {
                continue;
            }

            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }
        return params;
    }

    private static <T> T readJsonBody(HttpExchange exchange, Class<T> type) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            String requestBody = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            if (requestBody == null || requestBody.isBlank()) {
                return null;
            }
            return gson.fromJson(requestBody, type);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("Invalid JSON request.");
        }
    }

    private static int parsePositiveInt(String value, String fieldName) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException(fieldName + " must be a positive integer.");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid integer.");
        }
    }

    private static Skill.Level parseSkillLevel(String levelText) {
        if (levelText == null || levelText.trim().isEmpty()) {
            return Skill.Level.BEGINNER;
        }

        try {
            return Skill.Level.valueOf(levelText.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Skill level must be one of BEGINNER, INTERMEDIATE, ADVANCED or EXPERT.");
        }
    }

    private static void loadStudentsFromDatabase() {
        try {
            if (!DBConnection.isConfigured()) {
                System.out.println("Supabase database is not configured. The API will run in in-memory mode.");
                return;
            }

            List<Student> students = studentDAO.getAllStudents();
            for (Student student : students) {
                system.registerStudent(student);
            }
            System.out.println("Loaded " + students.size() + " student(s) from Supabase PostgreSQL.");
        } catch (Exception e) {
            System.out.println("Could not load students from the database: " + e.getMessage());
        }
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "http://localhost:5173");
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
        headers.set("Content-Type", "application/json; charset=UTF-8");
    }

    private static void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
        byte[] response = gson.toJson(data).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }

    private static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("message", message);
        sendJson(exchange, statusCode, response);
    }

    private static class StudentRequest {
        Integer studentId;
        String name;
        String email;
    }

    private static class StudentUpdateRequest {
        String name;
        String email;
    }

    private static class StudentSkillRequest {
        Boolean offered;
        String skillName;
        String category;
        String level;
        String learningGoal;
    }

    private static class SessionRequest {
        Integer mentorId;
        Integer learnerId;
        String skillName;
        String date;
        String time;
    }

    private static class RatingRequest {
        Integer score;
        String feedback;
    }
}