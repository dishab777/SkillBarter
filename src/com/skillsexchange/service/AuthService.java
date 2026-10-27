package com.skillsexchange.service;

import com.skillsexchange.database.UserDAO;
import com.skillsexchange.model.Student;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {

    private static final int PASSWORD_ITERATIONS = 310_000;
    private static final int HASH_BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final long SESSION_LIFETIME_MILLIS = 12L * 60 * 60 * 1000;

    private final UserDAO userDAO;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, LoginSession> sessions = new ConcurrentHashMap<>();

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public Student register(
            int studentId,
            String nameValue,
            String emailValue,
            String password) throws SQLException {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be a positive integer.");
        }

        String name = nameValue == null ? "" : nameValue.trim();
        String email = normalizeEmail(emailValue);
        validateName(name);
        validatePassword(password);

        Student student = new Student(studentId, name, email);
        userDAO.register(student, email, hashPassword(password));
        return student;
    }

    public LoginResult login(String emailValue, String password)
            throws SQLException {
        String email = emailValue == null
                ? ""
                : emailValue.trim().toLowerCase(Locale.ROOT);
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || password == null || password.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        UserDAO.Account account = userDAO.findAccountByEmail(email);
        if (account == null || !verifyPassword(password, account.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        long expiresAt = System.currentTimeMillis() + SESSION_LIFETIME_MILLIS;
        sessions.put(token, new LoginSession(account.getStudent(), expiresAt));
        removeExpiredSessions();
        return new LoginResult(account.getStudent(), token);
    }

    public Student findSession(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        LoginSession session = sessions.get(token);
        if (session == null) {
            return null;
        }
        if (session.expiresAt < System.currentTimeMillis()) {
            sessions.remove(token);
            return null;
        }
        return session.student;
    }

    public void logout(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    private String normalizeEmail(String emailValue) {
        String email = emailValue == null
                ? ""
                : emailValue.trim().toLowerCase(Locale.ROOT);
        if (email.isEmpty() || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        return email;
    }

    private void validateName(String name) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (name.length() > 120) {
            throw new IllegalArgumentException("Full name must be 120 characters or fewer.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        if (password.length() > 128) {
            throw new IllegalArgumentException("Password must be 128 characters or fewer.");
        }
    }

    private String hashPassword(String password) {
        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        byte[] hash = derivePasswordHash(password, salt, PASSWORD_ITERATIONS);

        return "pbkdf2_sha256$" + PASSWORD_ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    private boolean verifyPassword(String password, String encodedHash) {
        if (encodedHash == null) {
            return false;
        }

        String[] parts = encodedHash.split("\\$", -1);
        if (parts.length != 4 || !"pbkdf2_sha256".equals(parts[0])) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 100_000 || iterations > 1_000_000) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
            byte[] actualHash = derivePasswordHash(password, salt, iterations);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] derivePasswordHash(String password, byte[] salt, int iterations) {
        PBEKeySpec keySpec = new PBEKeySpec(
                password.toCharArray(), salt, iterations, HASH_BITS
        );
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(keySpec)
                    .getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is unavailable.", e);
        } finally {
            keySpec.clearPassword();
        }
    }

    private void removeExpiredSessions() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> entry.getValue().expiresAt < now);
    }

    public static final class LoginResult {
        private final Student student;
        private final String token;

        private LoginResult(Student student, String token) {
            this.student = student;
            this.token = token;
        }

        public Student getStudent() {
            return student;
        }

        public String getToken() {
            return token;
        }
    }

    private static final class LoginSession {
        private final Student student;
        private final long expiresAt;

        private LoginSession(Student student, long expiresAt) {
            this.student = student;
            this.expiresAt = expiresAt;
        }
    }

    public static final class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() {
            super("Invalid email or password.");
        }
    }
}
