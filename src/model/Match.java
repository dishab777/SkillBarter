package model;

import java.util.Arrays;
import java.util.List;

/**
 * Model class representing a skill exchange match between two students.
 * Enforces business domain validations and status transitions.
 */
public class Match {
    private int matchID;
    private int studentOneID;
    private int studentTwoID;
    private String skillName;
    private String status; // PENDING, ACCEPTED, COMPLETED, REJECTED
    private int rating;    // 0 = unrated, 1-5 = rated

    private static final List<String> VALID_STATUSES = Arrays.asList("PENDING", "ACCEPTED", "COMPLETED", "REJECTED");

    public Match(int matchID, int studentOneID, int studentTwoID, String skillName, String status, int rating) {
        if (studentOneID == studentTwoID) {
            throw new IllegalArgumentException("A student cannot match with themselves (student IDs must differ).");
        }
        this.matchID = matchID;
        this.studentOneID = studentOneID;
        this.studentTwoID = studentTwoID;
        setSkillName(skillName);
        setStatus(status);
        setRating(rating);
    }

    public int getMatchID() {
        return matchID;
    }

    public void setMatchID(int matchID) {
        this.matchID = matchID;
    }

    public int getStudentOneID() {
        return studentOneID;
    }

    public void setStudentOneID(int studentOneID) {
        if (studentOneID == this.studentTwoID) {
            throw new IllegalArgumentException("Student One cannot have the same ID as Student Two.");
        }
        this.studentOneID = studentOneID;
    }

    public int getStudentTwoID() {
        return studentTwoID;
    }

    public void setStudentTwoID(int studentTwoID) {
        if (studentTwoID == this.studentOneID) {
            throw new IllegalArgumentException("Student Two cannot have the same ID as Student One.");
        }
        this.studentTwoID = studentTwoID;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        if (skillName != null && !skillName.trim().isEmpty()) {
            this.skillName = skillName.trim();
        } else {
            throw new IllegalArgumentException("Skill name cannot be empty in a match.");
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status != null && VALID_STATUSES.contains(status.toUpperCase().trim())) {
            this.status = status.toUpperCase().trim();
        } else {
            throw new IllegalArgumentException("Status must be one of: PENDING, ACCEPTED, COMPLETED, REJECTED.");
        }
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating >= 0 && rating <= 5) {
            this.rating = rating;
        } else {
            throw new IllegalArgumentException("Rating must be between 0 and 5.");
        }
    }
}
