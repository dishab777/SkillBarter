package util;

import java.util.*;

/**
 * Lightweight JSON utility for formatting and parsing API responses
 * without introducing heavy third-party JSON dependencies.
 */
public class JsonUtil {

    /**
     * Creates a standardized success JSON response string.
     */
    public static String success(String message, Object data) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\":true,\"message\":").append(toJsonString(message));
        if (data != null) {
            sb.append(",\"data\":").append(toJson(data));
        } else {
            sb.append(",\"data\":null");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Creates a standardized failure JSON response string.
     */
    public static String failure(String message) {
        return "{\"success\":false,\"message\":" + toJsonString(message) + "}";
    }

    /**
     * Converts a Java object (Map, List, String, Number, Boolean, null) to a JSON string.
     */
    @SuppressWarnings("unchecked")
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return toJsonString((String) obj);
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append(toJsonString(entry.getKey())).append(":").append(toJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof Collection) {
            Collection<?> col = (Collection<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : col) {
                if (!first) sb.append(",");
                sb.append(toJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        return toJsonString(obj.toString());
    }

    /**
     * Escapes special characters for a JSON string literal.
     */
    public static String toJsonString(String str) {
        if (str == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 32) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    /**
     * Simple JSON parser for single-level JSON objects (e.g. {"name":"value","key":123}).
     */
    public static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) {
            return map;
        }
        String trimmed = json.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        if (trimmed.isEmpty()) {
            return map;
        }

        // Tokenize by commas taking quotes into account
        List<String> pairs = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == '"' && (i == 0 || trimmed.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
                current.append(c);
            } else if (c == ',' && !inQuotes) {
                pairs.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            pairs.add(current.toString().trim());
        }

        for (String pair : pairs) {
            int colonIndex = pair.indexOf(':');
            if (colonIndex != -1) {
                String key = cleanToken(pair.substring(0, colonIndex).trim());
                String value = cleanToken(pair.substring(colonIndex + 1).trim());
                map.put(key, value);
            }
        }
        return map;
    }

    private static String cleanToken(String token) {
        if (token == null) return "";
        token = token.trim();
        if (token.startsWith("\"") && token.endsWith("\"") && token.length() >= 2) {
            token = token.substring(1, token.length() - 1);
            // Replace escaped quotes and backslashes
            token = token.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r");
        }
        return token;
    }
}
