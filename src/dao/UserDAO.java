package dao;

import java.sql.*;
import model.Student;
import util.DBConnection;

/**
 * Data Access Object (DAO) for User / Student database operations.
 * Demonstrates JDBC best practices: PreparedStatement, try-with-resources,
 * and auto-generated key retrieval.
 */
public class UserDAO {

    /**
     * Registers a new student in the database.
     *
     * @param student The Student model object to insert
     * @return Generated user_id, or -1 if insertion failed
     * @throws SQLException If database error or unique constraint violated
     */
    public int registerUser(Student student) throws SQLException {
        String sql = "INSERT INTO public.users (name, email, password_hash, department, semester) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING user_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, student.getName());
            stmt.setString(2, student.getEmail().toLowerCase());
            stmt.setString(3, student.getPasswordHash());
            stmt.setString(4, student.getDepartment());
            stmt.setInt(5, student.getSemester());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    student.setUserID(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Finds a student by their unique email address.
     *
     * @param email The email to search for
     * @return Student object if found, or null
     * @throws SQLException On database failure
     */
    public Student findByEmail(String email) throws SQLException {
        String sql = "SELECT user_id, name, email, password_hash, department, semester " +
                     "FROM public.users WHERE LOWER(email) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToStudent(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds a student by their primary key user_id.
     *
     * @param userId The unique user ID
     * @return Student object if found, or null
     * @throws SQLException On database failure
     */
    public Student findById(int userId) throws SQLException {
        String sql = "SELECT user_id, name, email, password_hash, department, semester " +
                     "FROM public.users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToStudent(rs);
                }
            }
        }
        return null;
    }

    /**
     * Updates editable profile fields for a student.
     *
     * @param userId     Target user ID
     * @param name       New name
     * @param department New department
     * @param semester   New semester
     * @return true if updated successfully
     * @throws SQLException On database error
     */
    public boolean updateProfile(int userId, String name, String department, int semester) throws SQLException {
        String sql = "UPDATE public.users SET name = ?, department = ?, semester = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name.trim());
            stmt.setString(2, department.trim());
            stmt.setInt(3, semester);
            stmt.setInt(4, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Checks if an email is already registered.
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM public.users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Student mapRowToStudent(ResultSet rs) throws SQLException {
        return new Student(
            rs.getInt("user_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password_hash"),
            rs.getString("department"),
            rs.getInt("semester")
        );
    }
}
