package com.skillsexchange.model;

public class Session {

    public enum SessionStatus {
        SCHEDULED, COMPLETED, CANCELLED
    }

    private int sessionId;
    private Student mentor;
    private Student learner;
    private Skill skill;
    private String date;
    private String time;
    private SessionStatus status;

    public Session(int sessionId, Student mentor, Student learner,
                   Skill skill, String date, String time) {

        this.sessionId = sessionId;
        this.mentor = mentor;
        this.learner = learner;
        this.skill = skill;
        this.date = date;
        this.time = time;
        this.status = SessionStatus.SCHEDULED;
    }

    public int getSessionId() {
        return sessionId;
    }

    public Student getMentor() {
        return mentor;
    }

    public Student getLearner() {
        return learner;
    }

    public Skill getSkill() {
        return skill;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void completeSession() {
        if (status != SessionStatus.SCHEDULED) {
            throw new IllegalStateException(
                "Only scheduled sessions can be completed"
            );
        }

        status = SessionStatus.COMPLETED;
    }

    public void cancelSession() {
        if (status != SessionStatus.SCHEDULED) {
            throw new IllegalStateException(
                "Only scheduled sessions can be cancelled"
            );
        }

        status = SessionStatus.CANCELLED;
    }

    @Override
    public String toString() {
        return "Session " + sessionId
                + " | Mentor: " + mentor.getName()
                + " | Learner: " + learner.getName()
                + " | Skill: " + skill.getSkillName()
                + " | " + date + " " + time
                + " | Status: " + status;
    }
}