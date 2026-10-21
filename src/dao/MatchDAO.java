package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Match;
import util.DBConnection;

/**
 * Data Access Object for Skill Exchange Matches.
 * Manages matches, status transitions, ratings, and reciprocal partner discovery.
 */
public class MatchDAO {

    /**
     * Creates a new match between two students.
     *
     * @param match Match object
     * @return Generated match_id or -1
     * @throws SQLException On database failure
     */
    public int createMatch(Match match) throws SQLException {
        String sql = "INSERT INTO public.matches (student_one_id, student_two_id, skill_name, status, rating) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING match_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, match.getStudentOneID());
            stmt.setInt(2, match.getStudentTwoID());
            stmt.setString(3, match.getSkillName().trim());
            stmt.setString(4, match.getStatus());
            stmt.setInt(5, match.getRating());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    match.setMatchID(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Retrieves all matches involving a student (as sender or receiver),
     * enriched with the partner student's name, email, department, and semester.
     *
     * @param studentId Authenticated student ID
     * @return List of Maps representing enriched match details
     * @throws SQLException On database failure
     */
    public List<Map<String, Object>> getMatchesForStudent(int studentId) throws SQLException {
        List<Map<String, Object>> matches = new ArrayList<>();
        String sql = "SELECT m.match_id, m.student_one_id, m.student_two_id, m.skill_name, " +
                     "       m.status, m.rating, m.created_at, " +
                     "       u1.name AS student_one_name, u1.department AS student_one_dept, " +
                     "       u2.name AS student_two_name, u2.department AS student_two_dept " +
                     "FROM public.matches m " +
                     "JOIN public.users u1 ON m.student_one_id = u1.user_id " +
                     "JOIN public.users u2 ON m.student_two_id = u2.user_id " +
                     "WHERE m.student_one_id = ? OR m.student_two_id = ? " +
                     "ORDER BY m.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    int s1 = rs.getInt("student_one_id");
                    int s2 = rs.getInt("student_two_id");
                    boolean isSender = (studentId == s1);

                    item.put("matchID", rs.getInt("match_id"));
                    item.put("studentOneID", s1);
                    item.put("studentTwoID", s2);
                    item.put("isInitiator", isSender);
                    item.put("partnerID", isSender ? s2 : s1);
                    item.put("partnerName", isSender ? rs.getString("student_two_name") : rs.getString("student_one_name"));
                    item.put("partnerDept", isSender ? rs.getString("student_two_dept") : rs.getString("student_one_dept"));
                    item.put("skillName", rs.getString("skill_name"));
                    item.put("status", rs.getString("status"));
                    item.put("rating", rs.getInt("rating"));
                    item.put("createdAt", rs.getTimestamp("created_at").toString());
                    matches.add(item);
                }
            }
        }
        return matches;
    }

    /**
     * Checks if an active or pending match already exists between two students for a skill.
     */
    public boolean matchExists(int s1, int s2, String skillName) throws SQLException {
        String sql = "SELECT 1 FROM public.matches " +
                     "WHERE ((student_one_id = ? AND student_two_id = ?) OR (student_one_id = ? AND student_two_id = ?)) " +
                     "  AND LOWER(TRIM(skill_name)) = LOWER(TRIM(?)) " +
                     "  AND status IN ('PENDING', 'ACCEPTED')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, s1);
            stmt.setInt(2, s2);
            stmt.setInt(3, s2);
            stmt.setInt(4, s1);
            stmt.setString(5, skillName);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Retrieves a match record by its ID.
     */
    public Match getMatchById(int matchId) throws SQLException {
        String sql = "SELECT match_id, student_one_id, student_two_id, skill_name, status, rating " +
                     "FROM public.matches WHERE match_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, matchId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Match(
                        rs.getInt("match_id"),
                        rs.getInt("student_one_id"),
                        rs.getInt("student_two_id"),
                        rs.getString("skill_name"),
                        rs.getString("status"),
                        rs.getInt("rating")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Updates the status of a match, enforcing transition rules and student authorization.
     *
     * @param matchId   ID of the match to update
     * @param newStatus Desired status ('ACCEPTED', 'REJECTED', 'COMPLETED')
     * @param studentId The authenticated student requesting the update
     * @return true if updated successfully
     * @throws SQLException If database error or invalid transition
     */
    public boolean updateMatchStatus(int matchId, String newStatus, int studentId) throws SQLException {
        Match match = getMatchById(matchId);
        if (match == null) {
            throw new IllegalArgumentException("Match not found.");
        }

        // Validate that current student is a party in this match
        if (match.getStudentOneID() != studentId && match.getStudentTwoID() != studentId) {
            throw new SecurityException("Unauthorized: You are not a participant in this skill exchange match.");
        }

        String curStatus = match.getStatus();
        String target = newStatus.toUpperCase().trim();

        // Status transition rules:
        // PENDING -> ACCEPTED (only student_two, the recipient, can accept)
        // PENDING -> REJECTED (only student_two, the recipient, can reject)
        // ACCEPTED -> COMPLETED (either participant can mark completed)
        if ("PENDING".equals(curStatus)) {
            if ("ACCEPTED".equals(target) || "REJECTED".equals(target)) {
                if (match.getStudentTwoID() != studentId) {
                    throw new SecurityException("Only the recipient of the match request can accept or reject it.");
                }
            } else {
                throw new IllegalArgumentException("A PENDING match can only transition to ACCEPTED or REJECTED.");
            }
        } else if ("ACCEPTED".equals(curStatus)) {
            if (!"COMPLETED".equals(target)) {
                throw new IllegalArgumentException("An ACCEPTED match can only transition to COMPLETED.");
            }
        } else {
            throw new IllegalArgumentException("Cannot update status of a match that is already " + curStatus + ".");
        }

        String sql = "UPDATE public.matches SET status = ? WHERE match_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, target);
            stmt.setInt(2, matchId);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Submits a rating for a completed match.
     *
     * @param matchId   Match ID
     * @param rating    Rating between 1 and 5
     * @param studentId Authenticated student ID
     * @return true if rating updated
     * @throws SQLException On database failure
     */
    public boolean updateMatchRating(int matchId, int rating, int studentId) throws SQLException {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        Match match = getMatchById(matchId);
        if (match == null) {
            throw new IllegalArgumentException("Match not found.");
        }

        if (match.getStudentOneID() != studentId && match.getStudentTwoID() != studentId) {
            throw new SecurityException("Unauthorized: You cannot rate an exchange you are not a party to.");
        }

        if (!"COMPLETED".equalsIgnoreCase(match.getStatus())) {
            throw new IllegalStateException("Only COMPLETED skill exchanges can be rated.");
        }

        String sql = "UPDATE public.matches SET rating = ? WHERE match_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, rating);
            stmt.setInt(2, matchId);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Finds potential reciprocal skill-exchange partners.
     * A reciprocal match exists when:
     * - Student A TEACHES Skill X and WANTS TO LEARN Skill Y
     * - Student B TEACHES Skill Y and WANTS TO LEARN Skill X
     * - Student A != Student B
     *
     * @param currentStudentId Authenticated student ID
     * @return List of matching suggestions
     * @throws SQLException On database failure
     */
    public List<Map<String, Object>> findPotentialMatches(int currentStudentId) throws SQLException {
        List<Map<String, Object>> suggestions = new ArrayList<>();

        // SQL finding students where:
        // Current student teaches X and wants Y, AND other student teaches Y and wants X.
        String sql = 
            "SELECT DISTINCT " +
            "    u.user_id AS partner_id, " +
            "    u.name AS partner_name, " +
            "    u.department AS partner_dept, " +
            "    u.semester AS partner_semester, " +
            "    my_teach.skill_name AS you_teach_they_learn, " +
            "    partner_teach.skill_name AS they_teach_you_learn " +
            "FROM public.user_skills my_teach " +
            "JOIN public.user_skills partner_learn " +
            "    ON LOWER(TRIM(my_teach.skill_name)) = LOWER(TRIM(partner_learn.skill_name)) " +
            "    AND partner_learn.skill_type = 'LEARN' " +
            "JOIN public.user_skills partner_teach " +
            "    ON partner_teach.user_id = partner_learn.user_id " +
            "    AND partner_teach.skill_type = 'TEACH' " +
            "JOIN public.user_skills my_learn " +
            "    ON LOWER(TRIM(partner_teach.skill_name)) = LOWER(TRIM(my_learn.skill_name)) " +
            "    AND my_learn.skill_type = 'LEARN' " +
            "    AND my_learn.user_id = my_teach.user_id " +
            "JOIN public.users u ON u.user_id = partner_teach.user_id " +
            "WHERE my_teach.user_id = ? " +
            "  AND my_teach.skill_type = 'TEACH' " +
            "  AND partner_teach.user_id <> ? " +
            "ORDER BY u.name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentStudentId);
            stmt.setInt(2, currentStudentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("partnerID", rs.getInt("partner_id"));
                    map.put("partnerName", rs.getString("partner_name"));
                    map.put("partnerDept", rs.getString("partner_dept"));
                    map.put("partnerSemester", rs.getInt("partner_semester"));
                    map.put("youTeach", rs.getString("you_teach_they_learn"));
                    map.put("theyTeach", rs.getString("they_teach_you_learn"));
                    suggestions.add(map);
                }
            }
        }
        return suggestions;
    }
}
