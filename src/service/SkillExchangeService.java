package service;

import dao.MatchDAO;
import dao.SkillDAO;
import dao.UserDAO;
import interfaces.Matchable;
import interfaces.Rateable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Match;
import model.Skill;
import model.Student;

/**
 * Service Layer for SkillBarter.
 * Implements Matchable and Rateable interfaces.
 * Coordinates business logic between Servlets and DAOs.
 * Demonstrates OOP Interface Implementation, Polymorphism, and Collections (ArrayList, HashMap).
 */
public class SkillExchangeService implements Matchable, Rateable {

    private final UserDAO userDAO;
    private final SkillDAO skillDAO;
    private final MatchDAO matchDAO;

    public SkillExchangeService() {
        this.userDAO = new UserDAO();
        this.skillDAO = new SkillDAO();
        this.matchDAO = new MatchDAO();
    }

    // Constructor for dependency injection / testing
    public SkillExchangeService(UserDAO userDAO, SkillDAO skillDAO, MatchDAO matchDAO) {
        this.userDAO = userDAO;
        this.skillDAO = skillDAO;
        this.matchDAO = matchDAO;
    }

    // =========================================================================
    // Matchable Interface Implementation
    // =========================================================================

    @Override
    public boolean isCompatible(Skill teachSkill, Skill learnSkill) {
        if (teachSkill == null || learnSkill == null) {
            return false;
        }
        if (teachSkill.getUserID() == learnSkill.getUserID()) {
            return false;
        }
        if (!"TEACH".equalsIgnoreCase(teachSkill.getSkillType()) || !"LEARN".equalsIgnoreCase(learnSkill.getSkillType())) {
            return false;
        }
        return teachSkill.getSkillName().trim().equalsIgnoreCase(learnSkill.getSkillName().trim());
    }

    @Override
    public List<Map<String, Object>> findMatchSuggestions(int studentID) {
        try {
            return matchDAO.findPotentialMatches(studentID);
        } catch (SQLException e) {
            System.err.println("Error finding match suggestions: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // =========================================================================
    // Rateable Interface Implementation
    // =========================================================================

    @Override
    public boolean validateRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    @Override
    public boolean submitRating(int matchID, int studentID, int rating) {
        if (!validateRating(rating)) {
            throw new IllegalArgumentException("Rating must be an integer between 1 and 5.");
        }
        try {
            return matchDAO.updateMatchRating(matchID, rating, studentID);
        } catch (SQLException e) {
            throw new RuntimeException("Database error recording rating: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // Skills Management Logic
    // =========================================================================

    public int addSkill(int userID, String skillName, String skillType) throws SQLException {
        if (skillName == null || skillName.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill name is required.");
        }
        Skill skill = new Skill(0, userID, skillName.trim(), skillType);
        return skillDAO.addSkill(skill);
    }

    public List<Skill> getStudentSkills(int userID) throws SQLException {
        return skillDAO.getSkillsByUserId(userID);
    }

    /**
     * Categorizes a student's skills into TEACH and LEARN lists using HashMap.
     * Demonstrates Collections usage.
     */
    public Map<String, List<Skill>> getCategorizedSkills(int userID) throws SQLException {
        List<Skill> allSkills = skillDAO.getSkillsByUserId(userID);
        Map<String, List<Skill>> categorized = new HashMap<>();
        categorized.put("TEACH", new ArrayList<>());
        categorized.put("LEARN", new ArrayList<>());

        for (Skill s : allSkills) {
            if ("TEACH".equalsIgnoreCase(s.getSkillType())) {
                categorized.get("TEACH").add(s);
            } else if ("LEARN".equalsIgnoreCase(s.getSkillType())) {
                categorized.get("LEARN").add(s);
            }
        }
        return categorized;
    }

    public boolean removeSkill(int skillID, int userID) throws SQLException {
        return skillDAO.deleteSkill(skillID, userID);
    }

    public List<Map<String, Object>> getDiscoverableSkills(int currentUserID) throws SQLException {
        return skillDAO.getDiscoverableTeachingSkills(currentUserID);
    }

    // =========================================================================
    // Match Management Logic
    // =========================================================================

    public int requestMatch(int initiatorID, int partnerID, String skillName) throws SQLException {
        if (initiatorID == partnerID) {
            throw new IllegalArgumentException("You cannot request a skill exchange with yourself.");
        }
        if (skillName == null || skillName.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill name must be specified for the exchange.");
        }

        // Prevent duplicate pending/accepted matches for the same pair and skill
        if (matchDAO.matchExists(initiatorID, partnerID, skillName)) {
            throw new IllegalArgumentException("An active or pending match request already exists for this skill with this partner.");
        }

        Match match = new Match(0, initiatorID, partnerID, skillName, "PENDING", 0);
        return matchDAO.createMatch(match);
    }

    public List<Map<String, Object>> getStudentMatches(int studentID) throws SQLException {
        return matchDAO.getMatchesForStudent(studentID);
    }

    public boolean updateMatchStatus(int matchID, String newStatus, int studentID) throws SQLException {
        return matchDAO.updateMatchStatus(matchID, newStatus, studentID);
    }

    // =========================================================================
    // Profile & Student Logic
    // =========================================================================

    public Student getStudentProfile(int studentID) throws SQLException {
        return userDAO.findById(studentID);
    }

    public boolean updateStudentProfile(int studentID, String name, String department, int semester) throws SQLException {
        return userDAO.updateProfile(studentID, name, department, semester);
    }
}
