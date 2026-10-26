package com.skillsexchange.model;

public class Match {
    private Student mentor;
    private Skill skill;
    private double matchScore;

    public Match(Student mentor, Skill skill, double matchScore) {
        this.mentor = mentor;
        this.skill = skill;
        this.matchScore = matchScore;
    }

    public Student getMentor() {
        return mentor;
    }

    public Skill getSkill() {
        return skill;
    }

    public double getMatchScore() {
        return matchScore;
    }

    @Override
    public String toString() {
        return mentor.getName()
                + " | Skill: " + skill.getSkillName()
                + " | Score: " + matchScore
                + " | Rating: " + mentor.getAverageRating();
    }
}