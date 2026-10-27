package com.skillsexchange.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.skillsexchange.model.Student;

public class StudentDAO {

    // 1. Register a student in the PostgreSQL database
    public void addStudent(Student student, String passwordHash) throws SQLException {
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("A password hash is required.");
        }

        String sql = "INSERT INTO public.student "
                   + "(student_id, name, email, password_hash) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, student.getUserId());
            ps.setString(2, student.getName());
            ps.setString(3, student.getEmail());
            ps.setString(4, passwordHash);

            int rowsInserted = ps.executeUpdate();

            if (rowsInserted != 1) {
                throw new SQLException(
                    "Student registration failed. No row was inserted."
                );
            }
        }
    }

    public Student getStudentByEmail(String email) throws SQLException {
        String sql = "SELECT student_id, name, email "
                   + "FROM public.student WHERE lower(email) = lower(?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("email")
                    );
                }
            }
        }
        return null;
    }

    // 2. Find a student by ID
    public Student getStudent(int id) throws SQLException {

        String sql = "SELECT student_id, name, email "
                   + "FROM public.student WHERE student_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new Student(
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("email")
                    );
                }
            }
        }

        return null;
    }

    // 3. Retrieve all students from the database
    public List<Student> getAllStudents() throws SQLException {

        List<Student> students = new ArrayList<>();

        String sql = "SELECT student_id, name, email "
                   + "FROM public.student ORDER BY student_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Student student = new Student(
                    rs.getInt("student_id"),
                    rs.getString("name"),
                    rs.getString("email")
                );

                students.add(student);
            }
        }

        return students;
    }

    public void updateStudent(int studentId, String name, String email) throws SQLException {
        String sql = "UPDATE public.student SET name = ?, email = ? WHERE student_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setInt(3, studentId);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated != 1) {
                throw new SQLException("Student update failed. No row was updated.");
            }
        }
    }

    // 4. Display all students stored in PostgreSQL
    public void displayStudents() throws SQLException {

        List<Student> students = getAllStudents();

        System.out.println("\n========== STUDENTS IN DATABASE ==========");

        if (students.isEmpty()) {
            System.out.println("No students found in the database.");
            return;
        }

        for (Student student : students) {
            System.out.println(
                student.getUserId() + " | "
                + student.getName() + " | "
                + student.getEmail()
            );
        }

        System.out.println("==========================================");
        System.out.println("Total students: " + students.size());
    }
}