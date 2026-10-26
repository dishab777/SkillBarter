
package com.skillsexchange;

import com.skillsexchange.database.StudentDAO;
import com.skillsexchange.model.*;
import com.skillsexchange.service.SkillExchangeSystem;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    static Scanner sc = new Scanner(System.in);
    static SkillExchangeSystem system = new SkillExchangeSystem();
    static StudentDAO studentDAO = new StudentDAO();

    public static void main(String[] args) {

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
                choice = Integer.parseInt(sc.nextLine());

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
                        system.displayStudents();
                        break;
                    case 0:
                        System.out.println("Thank you for using the platform!");
                        break;
                    default:
                        System.out.println("Invalid choice!");
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

        } while (choice != 0);

        sc.close();
    }

    // 1. Register Student
    public static void registerStudent() {

        System.out.print("Enter Student ID: ");
        int id = Integer.parseInt(sc.nextLine());

        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Email: ");
        String email = sc.nextLine();

        if (system.getStudent(id) != null) {
            System.out.println("Student ID already registered!");
            return;
        }

        Student student = new Student(id, name, email);

        try {
            studentDAO.addStudent(student);
            system.registerStudent(student);

            System.out.println("Student registered successfully!");
            System.out.println("Student saved to MySQL!");

        } catch (java.sql.SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            System.out.println("Student registration failed.");
        }
    }

    // 2 and 3. Add Offered or Wanted Skill
    public static void addSkill(boolean offered) {

        System.out.print("Enter Student ID: ");
        int id = Integer.parseInt(sc.nextLine());

        Student student = system.getStudent(id);

        if (student == null) {
            System.out.println("Student not found!");
            return;
        }

        System.out.print("Enter Skill ID: ");
        int skillId = Integer.parseInt(sc.nextLine());

        System.out.print("Enter Skill Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Category: ");
        String category = sc.nextLine();

        System.out.println("Available Levels:");
        System.out.println("NONE, BEGINNER, INTERMEDIATE, ADVANCED, EXPERT");

        System.out.print("Enter Level: ");
        Skill.Level level = Skill.Level.valueOf(
            sc.nextLine().trim().toUpperCase()
        );

        String goal = "";

        if (!offered) {
            System.out.print("Enter Learning Goal: ");
            goal = sc.nextLine();
        }

        Skill skill = new Skill(skillId, name, category, level, goal);

        if (offered) {
            student.addOfferedSkill(skill);
            System.out.println("Offered skill added!");
        } else {
            student.addWantedSkill(skill);
            System.out.println("Wanted skill added!");
        }
    }

    // 4. View Student Profile
    public static void viewProfile() {

        System.out.print("Enter Student ID: ");
        int id = Integer.parseInt(sc.nextLine());

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
        int learnerId = Integer.parseInt(sc.nextLine());

        if (system.getStudent(learnerId) == null) {
            System.out.println("Student not found!");
            return;
        }

        System.out.print("Enter Skill to Learn: ");
        String skillName = sc.nextLine();

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
        int mentorId = Integer.parseInt(sc.nextLine());

        System.out.print("Enter Learner ID: ");
        int learnerId = Integer.parseInt(sc.nextLine());

        System.out.print("Enter Skill Name: ");
        String skill = sc.nextLine();

        System.out.print("Enter Date (YYYY-MM-DD): ");
        String date = sc.nextLine();

        System.out.print("Enter Time (HH:MM): ");
        String time = sc.nextLine();

        Session session = system.scheduleSession(
            mentorId, learnerId, skill, date, time
        );

        System.out.println("Session scheduled successfully!");
        System.out.println(session);
    }

    // 8. Complete Session
    public static void completeSession() {

        System.out.print("Enter Session ID: ");
        int id = Integer.parseInt(sc.nextLine());

        system.completeSession(id);

        System.out.println("Session completed successfully!");
    }

    // 9. Rate Mentor
    public static void rateMentor() {

        System.out.print("Enter Completed Session ID: ");
        int id = Integer.parseInt(sc.nextLine());

        System.out.print("Enter Rating (1-5): ");
        int score = Integer.parseInt(sc.nextLine());

        System.out.print("Enter Feedback: ");
        String feedback = sc.nextLine();

        Rating rating = system.rateMentor(id, score, feedback);

        System.out.println("Rating submitted successfully!");
        System.out.println("Score: " + rating.getScore());
    }
}
