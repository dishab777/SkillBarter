package com.skillsexchange.server;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.skillsexchange.database.DBConnection;
import com.skillsexchange.database.UserDAO;
import com.skillsexchange.model.Skill;
import com.skillsexchange.model.Student;
import com.skillsexchange.service.AuthService;

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
import java.util.Locale;
import java.util.Map;

public class ApiServer {

    private static final int DEFAULT_PORT = 8080;
    private static final int SESSION_MAX_AGE_SECONDS = 12 * 60 * 60;
    private static final String SESSION_COOKIE = "SKILLBARTER_SESSION";
    private static final String ALLOWED_ORIGIN = configuredOrigin();
    private static final Gson GSON = new Gson();
    private static final UserDAO USER_DAO = new UserDAO();
    private static final AuthService AUTH_SERVICE = new AuthService(USER_DAO);

    public static void main(String[] args) throws IOException {
        int port = configuredPort(args);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api", ApiServer::handleApi);
        server.setExecutor(null);
        server.start();

        System.out.println("SkillBarter API listening on http://localhost:" + port);
        System.out.println("Allowed frontend origin: " + ALLOWED_ORIGIN);
        System.out.println("Press Ctrl+C to stop the server.");
    }

    private static int configuredPort(String[] args) {
        String configured = args != null && args.length > 0
                ? args[0]
                : System.getenv("API_PORT");
        if (configured == null || configured.trim().isEmpty()) {
            return DEFAULT_PORT;
        }

        try {
            int port = Integer.parseInt(configured.trim());
            if (port < 1 || port > 65535) {
                throw new NumberFormatException();
            }
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("API port must be between 1 and 65535.");
        }
    }

    private static String configuredOrigin() {
        String origin = System.getenv("CORS_ALLOWED_ORIGIN");
        return origin == null || origin.trim().isEmpty()
                ? "http://localhost:5173"
                : origin.trim();
    }

    private static void handleApi(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        if (origin != null && !ALLOWED_ORIGIN.equals(origin)) {
            sendError(exchange, 403, "This frontend origin is not allowed.");
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            if (origin != null && !ALLOWED_ORIGIN.equals(origin)) {
                sendError(exchange, 403, "This frontend origin is not allowed.");
                return;
            }
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {
            if ("/api/health".equals(path)) {
                handleHealth(exchange, method);
                return;
            }

            if (path != null && path.startsWith("/api/auth/")) {
                handleAuth(exchange, method, path);
                return;
            }

            Student currentUser = authenticatedUser(exchange);
            if (currentUser == null) {
                sendError(exchange, 401, "Authentication is required.");
                return;
            }

            if ("/api/students".equals(path)) {
                handleStudents(exchange, method);
            } else if (path != null && path.startsWith("/api/students/")) {
                handleStudentDetail(exchange, method, path, currentUser);
            } else if ("/api/mentors".equals(path)) {
                handleMentors(exchange, method, currentUser);
            } else if ("/api/sessions".equals(path) || path.startsWith("/api/sessions/")) {
                sendError(exchange, 501,
                        "Session scheduling is not persisted by the current database schema.");
            } else if ("/api/ratings".equals(path)) {
                sendError(exchange, 501,
                        "Ratings are not persisted by the current database schema.");
            } else {
                sendError(exchange, 404, "Endpoint not found.");
            }
        } catch (AuthService.InvalidCredentialsException e) {
            sendError(exchange, 401, "Invalid email or password.");
        } catch (IllegalArgumentException e) {
            sendError(exchange, 400, e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            if ("23505".equals(e.getSQLState())) {
                sendError(exchange, 409, "That student ID or email address is already registered.");
            } else {
                sendError(exchange, 500, "The database could not complete the request.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "The server could not complete the request.");
        }
    }

    private static void handleHealth(HttpExchange exchange, String method)
            throws IOException {
        if (!"GET".equalsIgnoreCase(method)) {
            sendError(exchange, 405, "GET is the only supported method for /api/health.");
            return;
        }

        try {
            USER_DAO.checkConnection();
            sendJson(exchange, 200, success("API and Supabase PostgreSQL are reachable.",
                    Map.of("status", "ok", "database", "connected")));
        } catch (SQLException e) {
            e.printStackTrace();
            sendError(exchange, 503, "The API is running, but the database is unavailable.");
        }
    }

    private static void handleAuth(HttpExchange exchange, String method, String path)
            throws IOException, SQLException {
        if ("/api/auth/register".equals(path)) {
            if (!"POST".equalsIgnoreCase(method)) {
                sendError(exchange, 405, "POST is required for registration.");
                return;
            }

            RegisterRequest request = readJson(exchange, RegisterRequest.class);
            if (request == null) {
                sendError(exchange, 400, "Registration details are required.");
                return;
            }

            Student student = AUTH_SERVICE.register(
                    request.studentId,
                    request.name,
                    request.email,
                    request.password
            );
            sendJson(exchange, 201,
                    success("Account registered successfully. Please sign in.",
                            studentToMap(student)));
            return;
        }

        if ("/api/auth/login".equals(path)) {
            if (!"POST".equalsIgnoreCase(method)) {
                sendError(exchange, 405, "POST is required for login.");
                return;
            }

            LoginRequest request = readJson(exchange, LoginRequest.class);
            if (request == null) {
                throw new AuthService.InvalidCredentialsException();
            }

            AuthService.LoginResult result =
                    AUTH_SERVICE.login(request.email, request.password);
            exchange.getResponseHeaders().add(
                    "Set-Cookie",
                    sessionCookie(result.getToken(), SESSION_MAX_AGE_SECONDS)
            );
            sendJson(exchange, 200,
                    success("Signed in successfully.", studentToMap(result.getStudent())));
            return;
        }

        if ("/api/auth/me".equals(path)) {
            if (!"GET".equalsIgnoreCase(method)) {
                sendError(exchange, 405, "GET is required for the current-user endpoint.");
                return;
            }

            Student student = authenticatedUser(exchange);
            if (student == null) {
                clearSessionCookie(exchange);
                sendError(exchange, 401, "Authentication is required.");
                return;
            }

            Student profile = USER_DAO.findStudentById(student.getUserId());
            if (profile == null) {
                AUTH_SERVICE.logout(sessionToken(exchange));
                clearSessionCookie(exchange);
                sendError(exchange, 401, "Authentication is required.");
                return;
            }
            sendJson(exchange, 200, success("Current user retrieved.", studentToMap(profile)));
            return;
        }

        if ("/api/auth/logout".equals(path)) {
            if (!"POST".equalsIgnoreCase(method)) {
                sendError(exchange, 405, "POST is required for logout.");
                return;
            }

            AUTH_SERVICE.logout(sessionToken(exchange));
            clearSessionCookie(exchange);
            sendJson(exchange, 200, success("Signed out successfully.", Map.of()));
            return;
        }

        sendError(exchange, 404, "Authentication endpoint not found.");
    }

    private static void handleStudents(HttpExchange exchange, String method)
            throws IOException, SQLException {
        if (!"GET".equalsIgnoreCase(method)) {
            sendError(exchange, 405, "GET is the only supported method for /api/students.");
            return;
        }

        String query = exchange.getRequestURI().getRawQuery();
        if (query != null) {
            Map<String, String> parameters = parseQuery(query);
            if (parameters.containsKey("id")) {
                int id = parsePositiveInt(parameters.get("id"), "Student ID");
                Student student = USER_DAO.findStudentById(id);
                if (student == null) {
                    sendError(exchange, 404, "Student not found.");
                    return;
                }
                sendJson(exchange, 200, success("Student retrieved.", studentToMap(student)));
                return;
            }
        }

        List<Map<String, Object>> students = new ArrayList<>();
        for (Student student : USER_DAO.findAllStudents()) {
            students.add(studentToMap(student));
        }
        sendJson(exchange, 200, success("Students retrieved.", students));
    }

    private static void handleStudentDetail(
            HttpExchange exchange,
            String method,
            String path,
            Student currentUser) throws IOException, SQLException {
        String studentPath = path.substring("/api/students/".length());
        if (studentPath.endsWith("/skills")) {
            handleAddSkill(exchange, method, studentPath, currentUser);
            return;
        }

        int userId = parsePositiveInt(studentPath, "Student ID");
        if (userId != currentUser.getUserId()) {
            sendError(exchange, 403, "You can only change your own profile.");
            return;
        }
        if (!"PUT".equalsIgnoreCase(method)) {
            sendError(exchange, 405, "PUT is required to update a student profile.");
            return;
        }

        ProfileRequest request = readJson(exchange, ProfileRequest.class);
        if (request == null || request.name == null || request.name.trim().isEmpty()) {
            sendError(exchange, 400, "Full name is required.");
            return;
        }
        String email = request.email == null
                ? ""
                : request.email.trim().toLowerCase(Locale.ROOT);
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            sendError(exchange, 400, "Enter a valid email address.");
            return;
        }

        USER_DAO.updateProfile(userId, request.name.trim(), email);
        sendJson(exchange, 200, success("Profile updated.",
                studentToMap(USER_DAO.findStudentById(userId))));
    }

    private static void handleAddSkill(
            HttpExchange exchange,
            String method,
            String studentPath,
            Student currentUser) throws IOException, SQLException {
        String id = studentPath.substring(0, studentPath.length() - "/skills".length());
        int userId = parsePositiveInt(id, "Student ID");
        if (userId != currentUser.getUserId()) {
            sendError(exchange, 403, "You can only add skills to your own profile.");
            return;
        }
        if (!"POST".equalsIgnoreCase(method)) {
            sendError(exchange, 405, "POST is required to add a skill.");
            return;
        }

        SkillRequest request = readJson(exchange, SkillRequest.class);
        if (request == null || request.offered == null) {
            sendError(exchange, 400, "Choose whether you can teach or want to learn this skill.");
            return;
        }
        if (request.skillName == null
                || request.skillName.trim().isEmpty()) {
            sendError(exchange, 400, "Skill name is required.");
            return;
        }

        String skillName = request.skillName.trim();
        if (skillName.length() > 100) {
            sendError(exchange, 400, "Skill name must be 100 characters or fewer.");
            return;
        }

        USER_DAO.addSkill(
                userId,
                skillName,
                request.offered
        );
        Student updated = USER_DAO.findStudentById(userId);
        sendJson(exchange, 201, success("Skill saved.", studentToMap(updated)));
    }

    private static void handleMentors(
            HttpExchange exchange,
            String method,
            Student currentUser) throws IOException, SQLException {
        if (!"GET".equalsIgnoreCase(method)) {
            sendError(exchange, 405, "GET is the only supported method for /api/mentors.");
            return;
        }

        String query = exchange.getRequestURI().getRawQuery();
        Map<String, String> parameters =
                query == null ? new LinkedHashMap<>() : parseQuery(query);
        String skillName = parameters.get("skillName");
        if (skillName == null || skillName.trim().isEmpty()) {
            sendError(exchange, 400, "skillName is required.");
            return;
        }

        List<Map<String, Object>> matches = new ArrayList<>();
        for (Student student : USER_DAO.findAllStudents()) {
            if (student.getUserId() == currentUser.getUserId()) {
                continue;
            }
            for (Skill skill : student.getOfferedSkills()) {
                if (skill.getSkillName().equalsIgnoreCase(skillName.trim())) {
                    Map<String, Object> match = new LinkedHashMap<>();
                    match.put("mentor", studentToMap(student));
                    match.put("skill", skillToMap(skill));
                    matches.add(match);
                }
            }
        }
        sendJson(exchange, 200, success("Mentor matches retrieved.", matches));
    }

    private static Student authenticatedUser(HttpExchange exchange) {
        return AUTH_SERVICE.findSession(sessionToken(exchange));
    }

    private static String sessionToken(HttpExchange exchange) {
        List<String> cookieHeaders = exchange.getRequestHeaders().get("Cookie");
        if (cookieHeaders == null) {
            return null;
        }

        for (String cookieHeader : cookieHeaders) {
            for (String cookie : cookieHeader.split(";")) {
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && SESSION_COOKIE.equals(parts[0])) {
                    return parts[1];
                }
            }
        }
        return null;
    }

    private static String sessionCookie(String token, int maxAge) {
        String cookie = SESSION_COOKIE + "=" + token
                + "; HttpOnly; SameSite=Lax; Path=/; Max-Age=" + maxAge;
        if (Boolean.parseBoolean(System.getenv("APP_COOKIE_SECURE"))) {
            cookie += "; Secure";
        }
        return cookie;
    }

    private static void clearSessionCookie(HttpExchange exchange) {
        exchange.getResponseHeaders().add(
                "Set-Cookie",
                sessionCookie("", 0)
        );
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Vary", "Origin");
        headers.set("Cache-Control", "no-store");
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
        headers.set("Access-Control-Allow-Credentials", "true");

        String origin = exchange.getRequestHeaders().getFirst("Origin");
        if (origin != null && ALLOWED_ORIGIN.equals(origin)) {
            headers.set("Access-Control-Allow-Origin", origin);
        }
        headers.set("Content-Type", "application/json; charset=UTF-8");
    }

    private static <T> T readJson(HttpExchange exchange, Class<T> type)
            throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            String body = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            if (body.trim().isEmpty()) {
                return null;
            }
            return GSON.fromJson(body, type);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("Invalid JSON request.");
        }
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String pair : query.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                    : "";
            values.put(key, value);
        }
        return values;
    }

    private static int parsePositiveInt(String value, String fieldName) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException(
                        fieldName + " must be a positive integer."
                );
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid integer.");
        }
    }

    private static Map<String, Object> studentToMap(Student student) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("userId", student.getUserId());
        profile.put("name", student.getName());
        profile.put("email", student.getEmail());
        profile.put("offeredSkills", skillsToMap(student.getOfferedSkills()));
        profile.put("wantedSkills", skillsToMap(student.getWantedSkills()));
        return profile;
    }

    private static List<Map<String, Object>> skillsToMap(List<Skill> skills) {
        List<Map<String, Object>> values = new ArrayList<>();
        for (Skill skill : skills) {
            values.add(skillToMap(skill));
        }
        return values;
    }

    private static Map<String, Object> skillToMap(Skill skill) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("skillId", skill.getSkillId());
        value.put("skillName", skill.getSkillName());
        return value;
    }

    private static Map<String, Object> success(String message, Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private static void sendJson(
            HttpExchange exchange,
            int statusCode,
            Object body) throws IOException {
        byte[] response = GSON.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8"
        );
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }

    private static void sendError(
            HttpExchange exchange,
            int statusCode,
            String message) throws IOException {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("message", message);
        sendJson(exchange, statusCode, response);
    }

    private static final class RegisterRequest {
        int studentId;
        String name;
        String email;
        String password;
    }

    private static final class LoginRequest {
        String email;
        String password;
    }

    private static final class ProfileRequest {
        String name;
        String email;
    }

    private static final class SkillRequest {
        Boolean offered;
        String skillName;
    }
}
