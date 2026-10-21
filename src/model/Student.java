package model;

/**
 * Concrete Student class extending the abstract User class.
 * Demonstrates Inheritance and Polymorphism (@Override getRole).
 */
public class Student extends User {
    private String department;
    private int semester;

    /**
     * Parameterized constructor calling super(...)
     *
     * @param userID       Unique ID
     * @param name         Student's full name
     * @param email        Student's email
     * @param passwordHash PBKDF2 hashed password
     * @param department   Academic department (e.g., Computer Science)
     * @param semester     Current academic semester (1-12)
     */
    public Student(int userID, String name, String email, String passwordHash, String department, int semester) {
        super(userID, name, email, passwordHash);
        this.department = department != null ? department.trim() : "";
        setSemester(semester);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        if (department != null && !department.trim().isEmpty()) {
            this.department = department.trim();
        } else {
            throw new IllegalArgumentException("Department cannot be empty.");
        }
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        if (semester >= 1 && semester <= 12) {
            this.semester = semester;
        } else {
            throw new IllegalArgumentException("Semester must be between 1 and 12.");
        }
    }

    @Override
    public String getRole() {
        return "Student";
    }
}