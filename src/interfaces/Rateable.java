package interfaces;

/**
 * Interface defining rating behavior for completed skill exchanges.
 * Demonstrates the Interface OOP abstraction principle.
 */
public interface Rateable {

    /**
     * Validates whether a rating value is within the acceptable range (1 to 5).
     *
     * @param rating Numerical rating submitted
     * @return true if valid
     */
    boolean validateRating(int rating);

    /**
     * Submits a rating for an accepted/completed match.
     *
     * @param matchID   The match ID being rated
     * @param studentID The ID of the student submitting the rating
     * @param rating    Rating value (1-5)
     * @return true if the rating was successfully recorded
     */
    boolean submitRating(int matchID, int studentID, int rating);
}
