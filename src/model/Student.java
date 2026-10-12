package model;

public class Student extends User {
    private String department;
    private int semester;
    Student(int userID, String name, String email, String password, String department, int semester) {
        super(userID, name, email, password);
        this.department = department;
        this.semester = semester;
    }
    public String getDepartment() {
        return department;
    }
    public int getSemester() {
        return semester;
    }
    public void setDepartment(String department) {
        this.department = department;
    }
    public void setSemester(int semester) {
        this.semester = semester;
    }
    @Override 
    public String getRole() {
        return "Student";
    }
}