package com.skillsexchange.interfaces;

import com.skillsexchange.model.Rating;

public interface Rateable {
    void addRating(Rating rating);
    double getAverageRating();
}