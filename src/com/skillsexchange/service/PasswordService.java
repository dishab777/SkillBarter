package com.skillsexchange.service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordService {

    private static final int BCRYPT_COST = 12;
    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    private PasswordService() {
    }

    public static String hash(char[] password) {
        if (password == null || password.length == 0) {
            clear(password);
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        String passwordText = new String(password);
        byte[] passwordBytes = passwordText.getBytes(StandardCharsets.UTF_8);
        try {
            if (passwordBytes.length > BCRYPT_MAX_PASSWORD_BYTES) {
                throw new IllegalArgumentException(
                        "Password must be 72 UTF-8 bytes or fewer for BCrypt."
                );
            }
            return BCrypt.hashpw(passwordText, BCrypt.gensalt(BCRYPT_COST));
        } finally {
            clear(password);
            Arrays.fill(passwordBytes, (byte) 0);
        }
    }

    public static boolean verify(char[] password, String passwordHash) {
        if (password == null || password.length == 0
                || passwordHash == null || passwordHash.trim().isEmpty()) {
            clear(password);
            return false;
        }

        String passwordText = new String(password);
        byte[] passwordBytes = passwordText.getBytes(StandardCharsets.UTF_8);
        try {
            if (passwordBytes.length > BCRYPT_MAX_PASSWORD_BYTES) {
                return false;
            }
            return BCrypt.checkpw(passwordText, passwordHash);
        } finally {
            clear(password);
            Arrays.fill(passwordBytes, (byte) 0);
        }
    }

    private static void clear(char[] password) {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }
}
