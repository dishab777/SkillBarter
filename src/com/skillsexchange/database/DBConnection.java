package com.skillsexchange.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String HOST =
            System.getenv("SUPABASE_DB_HOST");

    private static final String PORT =
            System.getenv("SUPABASE_DB_PORT");

    private static final String DATABASE =
            System.getenv("SUPABASE_DB_NAME");

    private static final String USER =
            System.getenv("SUPABASE_DB_USER");

    private static final String PASSWORD =
            System.getenv("SUPABASE_DB_PASSWORD");

    public static boolean isConfigured() {
        return HOST != null && !HOST.trim().isEmpty()
                && PORT != null && !PORT.trim().isEmpty()
                && DATABASE != null && !DATABASE.trim().isEmpty()
                && USER != null && !USER.trim().isEmpty()
                && PASSWORD != null && !PASSWORD.trim().isEmpty();
    }

    public static Connection getConnection() throws SQLException {

        if (!isConfigured()) {
            throw new SQLException(
                    "Database configuration is missing. "
                    + "Set the SUPABASE_DB_* environment variables."
            );
        }

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "PostgreSQL JDBC driver not found in the classpath.", e
            );
        }

        String url = "jdbc:postgresql://" + HOST + ":"
                + PORT + "/" + DATABASE + "?sslmode=require";

        return DriverManager.getConnection(url, USER, PASSWORD);
    }
}