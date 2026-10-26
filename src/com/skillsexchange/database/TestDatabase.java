
package com.skillsexchange.database;

import java.sql.Connection;
import java.util.Scanner;

import com.skillsexchange.model.Student;

public class TestDatabase {

    public static void main(String[] args) {

        StudentDAO dao = new StudentDAO();

        // Test database connection
        try (Connection con = DBConnection.getConnection()) {
            System.out.println("MySQL connected successfully!");
        } catch (Exception e) {
            System.out.println("Connection failed: " + e.getMessage());
            return;
        }

        try (Scanner sc = new Scanner(System.in)) {

            System.out.print("Enter Student ID: ");
            int id = Integer.parseInt(sc.nextLine());

            System.out.print("Enter Student Name: ");
            String name = sc.nextLine();

            System.out.print("Enter Student Email: ");
            String email = sc.nextLine();

            Student student = new Student(id, name, email);

            // Save student
            dao.addStudent(student);
            System.out.println("Student saved to MySQL!");

            // Retrieve student
            Student savedStudent = dao.getStudent(id);

            if (savedStudent != null) {
                System.out.println("\nSTUDENT RETRIEVED:");
                savedStudent.displayProfile();
            }

            // Display all students
            dao.displayStudents();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
