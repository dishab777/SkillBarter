package com.skillsexchange;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.io.Console;

import com.skillsexchange.database.StudentDAO;
import com.skillsexchange.model.Match;
import com.skillsexchange.model.Rating;
import com.skillsexchange.model.Session;
import com.skillsexchange.model.Skill;
import com.skillsexchange.model.Student;
import com.skillsexchange.service.SkillExchangeSystem;
import com.skillsexchange.service.PasswordService;

public class Main {

    static Scanner sc = new Scanner(System.in);
    private static final Console CONSOLE = System.console();
    static SkillExchangeSystem system = new SkillExchangeSystem();
    static StudentDAO studentDAO = new StudentDAO();

    public static void main(String[] args) {

        // Load previously registered students from PostgreSQL
        loadStudentsFromDatabase();

        int choice = -1;

        do {
            System.out.println("\n=================================");
            System.out.println("     PEER SKILL EXCHANGE");
            System.out.println("=================================");
            System.out.println("1. Register Student");
            System.out.println("2. Add Offered Skill");
            System.out.println("3. Add Wanted Skill");
            System.out.println("4. View Student Profile");
            System.out.println("5. Search for Mentors");
            System.out.println("6. Schedule Session");
            System.out.println("7. View Sessions");
            System.out.println("8. Complete Session");
            System.out.println("9. Rate Mentor");
            System.out.println("10. View All Students");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            try {
                choice = Integer.parseInt(readLine().trim());

                switch (choice) {

                    case 1:
                        registerStudent();
                        break;

                    case 2:
                        addSkill(true);
                        break;

                    case 3:
                        addSkill(false);
                        break;

                    case 4:
                        viewProfile();
                        break;

                    case 5:
                        searchMentors();
                        break;

                    case 6:
                        scheduleSession();
                        break;

                    case 7:
                        system.displaySessions();
                        break;

                    case 8:
                        completeSession();
                        break;

                    case 9:
                        rateMentor();
                        break;

                    case 10:
                        displayAllStudents();
                        break;

                    case 0:
                        System.out.println(
                            "Thank you for using the platform!"
                        );
                        break;

                    default:
                        System.out.println(
                            "Invalid choice! Please select a valid option."
                        );
                }

            } catch (NumberFormatException e) {
                System.out.println(
                    "Invalid input. Please enter a valid number."
                );

            } catch (SQLException e) {
                if ("23505".equals(e.getSQLState())) {
                    System.out.println(
                        "Registration failed: that student ID or email is already registered."
                    );
                } else {
                    System.out.println(
                        "Database operation failed. No success was reported."
                    );
                }

            } catch (Exception e) {
                System.out.println(
                    "Error: " + e.getMessage()
                );
            }

        } while (choice != 0);

        sc.close();
    }

    // Load students saved in PostgreSQL into the application
    public static void loadStudentsFromDatabase() {

        try {
            List<Student> students = studentDAO.getAllStudents();

            for (Student student : students) {
                system.registerStudent(student);
            }

            System.out.println(
                "Database connection successful."
            );

            System.out.println(
                students.size() + " student(s) loaded from PostgreSQL."
            );

        } catch (SQLException e) {
            System.out.println(
                "Could not load students from PostgreSQL: "
                + e.getMessage()
            );

            System.out.println(
                "Please check your database configuration and connection."
            );
        }
    }

    // 1. Register Student
    public static void registerStudent() throws SQLException {

        System.out.print("Enter Student ID: ");
        int id = Integer.parseInt(readLine().trim());

        System.out.print("Enter Name: ");
        String name = readLine().trim();

        System.out.print("Enter Email: ");
        String email = readLine().trim().toLowerCase(java.util.Locale.ROOT);

        // Basic input validation
        if (name.isEmpty() || email.isEmpty()
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            System.out.println(
                "Enter a name and a valid email address."
            );
            return;
        }

        // Check the database, not just the in-memory collection
        if (studentDAO.getStudent(id) != null) {
            System.out.println(
                "Student ID already exists in the database!"
            );
            return;
        }

        // Check whether this ID is already in the running application
        if (system.getStudent(id) != null) {
            System.out.println(
                "Student ID is already registered in the application!"
            );
            return;
        }

        try {
            if (studentDAO.getStudentByEmail(email) != null) {
                System.out.println("Email address is already registered.");
                return;
            }
        } catch (SQLException e) {
            System.out.println(
                "Could not check for duplicate email. Registration was not attempted."
            );
            throw e;
        }

        if (CONSOLE == null) {
            System.out.println(
                "Secure password input is unavailable in this console. "
                + "Run the program from Windows Terminal or PowerShell, "
                + "not an IDE output pane."
            );
            return;
        }

        char[] password = CONSOLE.readPassword("Enter Password: ");
        if (password == null || password.length == 0) {
            clearPassword(password);
            System.out.println("Password cannot be empty.");
            return;
        }

        char[] confirmation = CONSOLE.readPassword("Confirm Password: ");
        if (confirmation == null || !Arrays.equals(password, confirmation)) {
            clearPassword(password);
            clearPassword(confirmation);
            System.out.println("Passwords do not match.");
            return;
        }
        clearPassword(confirmation);

        String passwordHash;
        try {
            passwordHash = PasswordService.hash(password);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }

        Student student = new Student(id, name, email);
        studentDAO.addStudent(student, passwordHash);
        system.registerStudent(student);

        System.out.println("\nStudent registered successfully!");
        System.out.println("Student saved to Supabase PostgreSQL.");
        System.out.println("Student ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
    }

    private static void clearPassword(char[] password) {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }

    private static String readLine() {
        return CONSOLE == null ? sc.nextLine() : CONSOLE.readLine();
    }

    // 2 and 3. Add Offered or Wanted Skill
    public static void addSkill(boolean offered) {

        System.out.print("Enter Student ID: ");
        int id = Integer.parseInt(readLine().trim());

        Student student = system.getStudent(id);

        if (student == null) {
            System.out.println("Student not found!");
            return;
        }

        System.out.print("Enter Skill ID: ");
        int skillId = Integer.parseInt(readLine().trim());

        System.out.print("Enter Skill Name: ");
        String name = readLine().trim();

        System.out.print("Enter Category: ");
        String category = readLine().trim();

        System.out.println("\nAvailable Levels:");
        System.out.println(
            "NONE, BEGINNER, INTERMEDIATE, ADVANCED, EXPERT"
        );

        System.out.print("Enter Level: ");

        Skill.Level level = Skill.Level.valueOf(
            readLine().trim().toUpperCase()
        );

        String goal = "";

        if (!offered) {
            System.out.print("Enter Learning Goal: ");
            goal = readLine().trim();
        }

        Skill skill = new Skill(
            skillId,
            name,
            category,
            level,
            goal
        );

        if (offered) {
            student.addOfferedSkill(skill);
            System.out.println("Offered skill added!");

        } else {
            student.addWantedSkill(skill);
            System.out.println("Wanted skill added!");
        }

        System.out.println(
            "Note: Skills are currently stored in application memory."
        );
    }

    // 4. View Student Profile
    public static void viewProfile() {

        System.out.print("Enter Student ID: ");
        int id = Integer.parseInt(readLine().trim());

        Student student = system.getStudent(id);

        if (student != null) {
            student.displayProfile();

        } else {
            System.out.println("Student not found!");
        }
    }

    // 5. Search for Mentors
    public static void searchMentors() {

        System.out.print("Enter Your Student ID: ");
        int learnerId = Integer.parseInt(readLine().trim());

        if (system.getStudent(learnerId) == null) {
            System.out.println("Student not found!");
            return;
        }

        System.out.print("Enter Skill to Learn: ");
        String skillName = readLine().trim();

        ArrayList<Match> matches =
            system.findMentors(skillName, learnerId);

        if (matches.isEmpty()) {
            System.out.println("No mentors found!");
            return;
        }

        System.out.println("\nAVAILABLE MENTORS:");

        for (Match match : matches) {
            System.out.println(match);
        }
    }

    // 6. Schedule Session
    public static void scheduleSession() {

        System.out.print("Enter Mentor ID: ");
        int mentorId = Integer.parseInt(readLine().trim());

        System.out.print("Enter Learner ID: ");
        int learnerId = Integer.parseInt(readLine().trim());

        System.out.print("Enter Skill Name: ");
        String skill = readLine().trim();

        System.out.print("Enter Date (YYYY-MM-DD): ");
        String date = readLine().trim();

        System.out.print("Enter Time (HH:MM): ");
        String time = readLine().trim();

        Session session = system.scheduleSession(
            mentorId,
            learnerId,
            skill,
            date,
            time
        );

        System.out.println("Session scheduled successfully!");
        System.out.println(session);

        System.out.println(
            "Note: Sessions are currently stored in application memory."
        );
    }

    // 8. Complete Session
    public static void completeSession() {

        System.out.print("Enter Session ID: ");
        int id = Integer.parseInt(readLine().trim());

        system.completeSession(id);

        System.out.println("Session completed successfully!");
    }

    // 9. Rate Mentor
    public static void rateMentor() {

        System.out.print("Enter Completed Session ID: ");
        int id = Integer.parseInt(readLine().trim());

        System.out.print("Enter Rating (1-5): ");
        int score = Integer.parseInt(readLine().trim());

        if (score < 1 || score > 5) {
            System.out.println(
                "Invalid rating. Please enter a score between 1 and 5."
            );
            return;
        }

        System.out.print("Enter Feedback: ");
        String feedback = readLine().trim();

        Rating rating = system.rateMentor(
            id,
            score,
            feedback
        );

        System.out.println("Rating submitted successfully!");
        System.out.println("Score: " + rating.getScore());

        System.out.println(
            "Note: Ratings are currently stored in application memory."
        );
    }

    // 10. Display students directly from PostgreSQL
    public static void displayAllStudents() throws SQLException {
        studentDAO.displayStudents();
    }
}