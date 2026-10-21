package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Skill;
import util.DBConnection;

/**
 * Data Access Object for User Skills.
 * Implements CRUD and discovery queries using PreparedStatement and try-with-resources.
 */
public class SkillDAO {

    /**
     * Adds a new skill (TEACH or LEARN) for a student.
     *
     * @param skill The Skill object to insert
     * @return Generated skill_id, or -1 on failure
     * @throws SQLException On database failure
     */
    public int addSkill(Skill skill) throws SQLException {
        String sql = "INSERT INTO public.user_skills (user_id, skill_name, skill_type) " +
                     "VALUES (?, ?, ?) RETURNING skill_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, skill.getUserID());
            stmt.setString(2, skill.getSkillName().trim());
            stmt.setString(3, skill.getSkillType().toUpperCase().trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    skill.setSkillID(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Lists all skills (both TEACH and LEARN) for a given student.
     *
     * @param userId The student's user ID
     * @return ArrayList of Skill objects
     * @throws SQLException On database failure
     */
    public List<Skill> getSkillsByUserId(int userId) throws SQLException {
        List<Skill> skills = new ArrayList<>();
        String sql = "SELECT skill_id, user_id, skill_name, skill_type " +
                     "FROM public.user_skills WHERE user_id = ? ORDER BY skill_type DESC, skill_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    skills.add(mapRowToSkill(rs));
                }
            }
        }
        return skills;
    }

    /**
     * Lists skills of a specific type (TEACH or LEARN) for a student.
     *
     * @param userId    Student's user ID
     * @param skillType "TEACH" or "LEARN"
     * @return List of Skill objects
     * @throws SQLException On database failure
     */
    public List<Skill> getSkillsByType(int userId, String skillType) throws SQLException {
        List<Skill> skills = new ArrayList<>();
        String sql = "SELECT skill_id, user_id, skill_name, skill_type " +
                     "FROM public.user_skills WHERE user_id = ? AND skill_type = ? ORDER BY skill_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, skillType.toUpperCase().trim());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    skills.add(mapRowToSkill(rs));
                }
            }
        }
        return skills;
    }

    /**
     * Lists discoverable teaching skills offered by other students.
     * Excludes the current student's own skills.
     *
     * @param excludeUserId The current student's user ID
     * @return List of Maps containing skill info and the teacher's profile
     * @throws SQLException On database failure
     */
    public List<Map<String, Object>> getDiscoverableTeachingSkills(int excludeUserId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT s.skill_id, s.skill_name, s.skill_type, " +
                     "       u.user_id, u.name AS user_name, u.department, u.semester " +
                     "FROM public.user_skills s " +
                     "JOIN public.users u ON s.user_id = u.user_id " +
                     "WHERE s.skill_type = 'TEACH' AND s.user_id <> ? " +
                     "ORDER BY s.skill_name ASC, u.name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, excludeUserId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("skillID", rs.getInt("skill_id"));
                    item.put("skillName", rs.getString("skill_name"));
                    item.put("skillType", rs.getString("skill_type"));
                    item.put("teacherID", rs.getInt("user_id"));
                    item.put("teacherName", rs.getString("user_name"));
                    item.put("department", rs.getString("department"));
                    item.put("semester", rs.getInt("semester"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    /**
     * Deletes a skill record, strictly enforcing that it belongs to the authenticated user.
     *
     * @param skillId The skill ID to remove
     * @param userId  The ID of the authenticated user
     * @return true if deleted, false if not found or unauthorized
     * @throws SQLException On database failure
     */
    public boolean deleteSkill(int skillId, int userId) throws SQLException {
        String sql = "DELETE FROM public.user_skills WHERE skill_id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, skillId);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    private Skill mapRowToSkill(ResultSet rs) throws SQLException {
        return new Skill(
            rs.getInt("skill_id"),
            rs.getInt("user_id"),
            rs.getString("skill_name"),
            rs.getString("skill_type")
        );
    }
}
