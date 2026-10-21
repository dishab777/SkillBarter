package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for establishing JDBC connections to Supabase PostgreSQL over SSL.
 * Reads connection settings securely from environment variables.
 */
public class DBConnection {

    static {
        try {
            // Explicitly load the PostgreSQL driver class
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Warning: PostgreSQL JDBC driver not found on classpath: " + e.getMessage());
        }
    }

    /**
     * Establishes a database connection to Supabase PostgreSQL.
     *
     * @return Active Connection object
     * @throws SQLException If connection cannot be established or environment variables are missing
     */
    public static Connection getConnection() throws SQLException {
        String host = System.getenv("SUPABASE_DB_HOST");
        String portStr = System.getenv("SUPABASE_DB_PORT");
        String dbName = System.getenv("SUPABASE_DB_NAME");
        String user = System.getenv("SUPABASE_DB_USER");
        String password = System.getenv("SUPABASE_DB_PASSWORD");

        if (host == null || host.trim().isEmpty()) {
            throw new SQLException("Missing environment variable: SUPABASE_DB_HOST. Please configure your Supabase host.");
        }
        if (user == null || user.trim().isEmpty()) {
            throw new SQLException("Missing environment variable: SUPABASE_DB_USER. Please configure your Supabase user.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new SQLException("Missing environment variable: SUPABASE_DB_PASSWORD. Please configure your Supabase password.");
        }

        int port = 5432;
        if (portStr != null && !portStr.trim().isEmpty()) {
            try {
                port = Integer.parseInt(portStr.trim());
            } catch (NumberFormatException e) {
                // fallback to 5432
            }
        }

        String db = (dbName != null && !dbName.trim().isEmpty()) ? dbName.trim() : "postgres";

        // Construct standard PostgreSQL JDBC URL with SSL enabled for Supabase
        String url = String.format("jdbc:postgresql://%s:%d/%s?sslmode=require", host.trim(), port, db);

        return DriverManager.getConnection(url, user.trim(), password.trim());
    }
}
