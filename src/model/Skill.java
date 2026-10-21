package model;

/**
 * Model class representing a skill offered or desired by a student.
 * Demonstrates Encapsulation and Data Validation.
 */
public class Skill {
    private int skillID;
    private int userID;
    private String skillName;
    private String skillType; // "TEACH" or "LEARN"

    public Skill(int skillID, int userID, String skillName, String skillType) {
        this.skillID = skillID;
        this.userID = userID;
        setSkillName(skillName);
        setSkillType(skillType);
    }

    public int getSkillID() {
        return skillID;
    }

    public void setSkillID(int skillID) {
        this.skillID = skillID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        if (skillName != null && !skillName.trim().isEmpty()) {
            this.skillName = skillName.trim();
        } else {
            throw new IllegalArgumentException("Skill name cannot be empty.");
        }
    }

    public String getSkillType() {
        return skillType;
    }

    public void setSkillType(String skillType) {
        if (skillType != null && (skillType.equalsIgnoreCase("TEACH") || skillType.equalsIgnoreCase("LEARN"))) {
            this.skillType = skillType.toUpperCase().trim();
        } else {
            throw new IllegalArgumentException("Skill type must be either 'TEACH' or 'LEARN'.");
        }
    }
}