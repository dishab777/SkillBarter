package model;
public class Skill{
    private int skillID;
    private int userID;
    private String skillName;
    private String skillType;
    public Skill(int skillID, int userID, String skillName, String skillType){
        this.skillID = skillID;
        this.userID = userID;
        this.skillName = skillName;
        setSkillType(skillType);
    }
    public int getSkillID() {
        return skillID;
    }
    public int getUserID() {
        return userID;
    }
    public String getSkillName() {
        return skillName;
    }
    public String getSkillType() {
        return skillType;
    }
    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }
    public void setSkillType(String skillType) {
        if (skillType != null && (skillType.equalsIgnoreCase("TEACH") || skillType.equalsIgnoreCase("LEARN"))) {
            this.skillType = skillType.toUpperCase();
        }
        else {
            throw new IllegalArgumentException("Skill type must be TEACH or LEARN.");
        }
    }
}