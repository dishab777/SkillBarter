package com.skillsexchange.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator;

import com.skillsexchange.model.Student;
import com.skillsexchange.model.Skill;
import com.skillsexchange.model.Match;
import com.skillsexchange.model.Session;
import com.skillsexchange.model.Rating;

public class SkillExchangeSystem {

    private final HashMap<Integer, Student> students = new HashMap<>();
    private final ArrayList<Session> sessions = new ArrayList<>();
    private final ArrayList<Rating> ratings = new ArrayList<>();

    private int nextSessionId = 1;
    private int nextRatingId = 1;

    // Register a student
    public void registerStudent(Student student) {
        if (students.containsKey(student.getUserId())) {
            throw new IllegalArgumentException("Student ID already exists");
        }

        students.put(student.getUserId(), student);
    }

    // Find student by ID
    public Student getStudent(int id) {
        return students.get(id);
    }

    public ArrayList<Student> getAllStudents() {
        return new ArrayList<>(students.values());
    }

    public void updateStudent(int studentId, String name, String email) {
        Student student = students.get(studentId);

        if (student == null) {
            throw new IllegalArgumentException("Student not found");
        }

        student.setName(name);
        student.setEmail(email);
    }

    public void addSkillToStudent(int studentId, Skill skill, boolean offered) {
        Student student = students.get(studentId);

        if (student == null) {
            throw new IllegalArgumentException("Student not found");
        }

        if (offered) {
            student.addOfferedSkill(skill);
        } else {
            student.addWantedSkill(skill);
        }
    }

    // Find mentors for a skill
    public ArrayList<Match> findMentors(String skillName, int learnerId) {

        ArrayList<Match> matches = new ArrayList<>();

        for (Student student : students.values()) {

            if (student.getUserId() == learnerId) {
                continue;
            }

            for (Skill skill : student.getOfferedSkills()) {

                if (skill.getSkillName().equalsIgnoreCase(skillName)
                        && skill.getLevel() != Skill.Level.NONE) {

                    double score = calculateMatchScore(student, skill);

                    matches.add(new Match(student, skill, score));
                }
            }
        }

        // Sort by highest matching score
        matches.sort(
            Comparator.comparingDouble(Match::getMatchScore).reversed()
        );

        return matches;
    }

    // Calculate simple matching score out of 100
    private double calculateMatchScore(Student mentor, Skill skill) {

        double levelScore = skill.getLevel().ordinal() * 15;
        double ratingScore = mentor.getAverageRating() * 8;

        return Math.min(100, levelScore + ratingScore);
    }

    // Schedule a session
    public Session scheduleSession(int mentorId, int learnerId,
                                   String skillName, String date, String time) {

        Student mentor = students.get(mentorId);
        Student learner = students.get(learnerId);

        if (mentor == null || learner == null) {
            throw new IllegalArgumentException("Student not found");
        }

        if (mentorId == learnerId) {
            throw new IllegalArgumentException(
                "A student cannot mentor themselves"
            );
        }

        Skill selectedSkill = null;

        for (Skill skill : mentor.getOfferedSkills()) {
            if (skill.getSkillName().equalsIgnoreCase(skillName)
                    && skill.getLevel() != Skill.Level.NONE) {
                selectedSkill = skill;
                break;
            }
        }

        if (selectedSkill == null) {
            throw new IllegalArgumentException(
                "Mentor does not offer this skill"
            );
        }

        Session session = new Session(
            nextSessionId++, mentor, learner,
            selectedSkill, date, time
        );

        sessions.add(session);

        return session;
    }

    // Complete a session
    public void completeSession(int sessionId) {
        Session session = getSession(sessionId);

        if (session == null) {
            throw new IllegalArgumentException("Session not found");
        }

        session.completeSession();
    }

    // Find a session
    public Session getSession(int sessionId) {

        for (Session session : sessions) {
            if (session.getSessionId() == sessionId) {
                return session;
            }
        }

        return null;
    }

    // Rate a mentor after completing a session
    public Rating rateMentor(int sessionId, int score, String feedback) {

        Session session = getSession(sessionId);

        if (session == null) {
            throw new IllegalArgumentException("Session not found");
        }

        if (session.getStatus() != Session.SessionStatus.COMPLETED) {
            throw new IllegalStateException(
                "Complete the session before rating"
            );
        }

        Rating rating = new Rating(
            nextRatingId++,
            session.getLearner(),
            session.getMentor(),
            score,
            feedback
        );

        ratings.add(rating);
        session.getMentor().addRating(rating);

        return rating;
    }

    public ArrayList<Session> getAllSessions() {
        return new ArrayList<>(sessions);
    }

    public ArrayList<Rating> getAllRatings() {
        return new ArrayList<>(ratings);
    }

    // Display all registered students
    public void displayStudents() {
        for (Student student : students.values()) {
            student.displayProfile();
            System.out.println("-------------------");
        }
    }

    // Display all sessions
    public void displaySessions() {
        for (Session session : sessions) {
            System.out.println(session);
        }
    }
}