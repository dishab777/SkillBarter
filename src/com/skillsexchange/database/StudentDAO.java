package com.skillsexchange.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.skillsexchange.model.Student;

public class StudentDAO {

    public void addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO Student (student_id, name, email) VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, student.getUserId());
            ps.setString(2, student.getName());
            ps.setString(3, student.getEmail());

            ps.executeUpdate();
        }
    }

    public Student getStudent(int id) throws SQLException {
        String sql = "SELECT * FROM Student WHERE student_id = ?";

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

    public void displayStudents() throws SQLException {
        String sql = "SELECT * FROM Student";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\nSTUDENTS IN DATABASE:");

            while (rs.next()) {
                System.out.println(
                    rs.getInt("student_id") + " | " +
                    rs.getString("name") + " | " +
                    rs.getString("email")
                );
            }
        }
    }
}