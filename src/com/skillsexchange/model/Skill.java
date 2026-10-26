package com.skillsexchange.model;

public class Skill {

    public enum Level {
        NONE, BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    private int skillId;
    private String skillName;
    private String category;
    private Level level;
    private String learningGoal;

    public Skill(int skillId, String skillName, String category,
                 Level level, String learningGoal) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = category;
        this.level = level;
        this.learningGoal = learningGoal;
    }

    public int getSkillId() {
        return skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public String getCategory() {
        return category;
    }

    public Level getLevel() {
        return level;
    }

    public String getLearningGoal() {
        return learningGoal;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void setLearningGoal(String learningGoal) {
        this.learningGoal = learningGoal;
    }

    @Override
    public String toString() {
        return skillName + " (" + level + ")";
    }
}