package com.skillsexchange.model;

import java.util.ArrayList;
import com.skillsexchange.interfaces.SkillProvider;
import com.skillsexchange.interfaces.Rateable;

public class Student extends User
        implements SkillProvider, Rateable {

    private ArrayList<Skill> offeredSkills;
    private ArrayList<Skill> wantedSkills;
    private ArrayList<Rating> ratings;

    public Student(int id, String name, String email) {
        super(id, name, email);

        offeredSkills = new ArrayList<>();
        wantedSkills = new ArrayList<>();
        ratings = new ArrayList<>();
    }

    @Override
    public void addOfferedSkill(Skill skill) {
        offeredSkills.add(skill);
    }

    @Override
    public void addWantedSkill(Skill skill) {
        wantedSkills.add(skill);
    }

    @Override
    public void addRating(Rating rating) {
        ratings.add(rating);
    }

    @Override
    public double getAverageRating() {
        if (ratings.isEmpty()) {
            return 0;
        }

        double total = 0;

        for (Rating rating : ratings) {
            total += rating.getScore();
        }

        return total / ratings.size();
    }

    public ArrayList<Rating> getRatings() {
        return new ArrayList<>(ratings);
    }

    public ArrayList<Skill> getOfferedSkills() {
        return new ArrayList<>(offeredSkills);
    }

    public ArrayList<Skill> getWantedSkills() {
        return new ArrayList<>(wantedSkills);
    }

    @Override
    public void displayProfile() {
        System.out.println("Student ID: " + getUserId());
        System.out.println("Name: " + getName());
        System.out.println("Email: " + getEmail());
        System.out.println("Offered Skills: " + offeredSkills);
        System.out.println("Wanted Skills: " + wantedSkills);
        System.out.println("Average Rating: " + getAverageRating());
    }
}