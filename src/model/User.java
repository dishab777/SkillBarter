package model;

/**
 * Abstract base class representing a registered user in SkillBarter.
 * Demonstrates Abstraction and Encapsulation OOP principles.
 */
public abstract class User {
    private int userID;
    private String name;
    private String email;
    private String passwordHash;

    /**
     * Parameterized constructor for User.
     *
     * @param userID       Unique ID of the user
     * @param name         Full name of the user
     * @param email        Unique email address
     * @param passwordHash Salted PBKDF2 hash of the user's password
     */
    public User(int userID, String name, String email, String passwordHash) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        } else {
            throw new IllegalArgumentException("User name cannot be empty.");
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email != null && email.contains("@")) {
            this.email = email.trim().toLowerCase();
        } else {
            throw new IllegalArgumentException("Invalid email address format.");
        }
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Abstract method demonstrating Polymorphism in subclasses.
     *
     * @return The specific role of this user.
     */
    public abstract String getRole();
}