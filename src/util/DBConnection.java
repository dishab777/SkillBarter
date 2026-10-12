package util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL =
        "jdbc:postgresql://YOUR_POOLER_HOST:5432/postgres?sslmode=require";
    private static final String USER = "YOUR_POOLER_USERNAME";

    public static Connection getConnection() throws SQLException {
        String password = System.getenv("SUPABASE_DB_PASSWORD");
        if (password == null || password.isEmpty()) {
            throw new SQLException("Set SUPABASE_DB_PASSWORD as an environment variable.");
        }
        return DriverManager.getConnection(URL, USER, password);
    }
}
