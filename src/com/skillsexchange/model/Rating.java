package com.skillsexchange.model;

public class Rating {
    private int ratingId;
    private Student fromStudent;
    private Student toStudent;
    private int score;
    private String feedback;

    public Rating(int ratingId, Student fromStudent,
                  Student toStudent, int score, String feedback) {

        if (score < 1 || score > 5) {
            throw new IllegalArgumentException(
                "Rating must be between 1 and 5"
            );
        }

        this.ratingId = ratingId;
        this.fromStudent = fromStudent;
        this.toStudent = toStudent;
        this.score = score;
        this.feedback = feedback;
    }

    public int getRatingId() {
        return ratingId;
    }

    public Student getFromStudent() {
        return fromStudent;
    }

    public Student getToStudent() {
        return toStudent;
    }

    public int getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }
}