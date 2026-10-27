package com.skillsexchange.database;

import com.skillsexchange.model.Skill;
import com.skillsexchange.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDAO {

    public void register(Student student, String email, String passwordHash)
            throws SQLException {
        String insertUser = "INSERT INTO public.users "
                + "(user_id, name, email, password_hash) VALUES (?, ?, ?, ?)";
        String insertStudent = "INSERT INTO public.student "
                + "(student_id, name, email) VALUES (?, ?, ?)";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement userStatement =
                         connection.prepareStatement(insertUser);
                 PreparedStatement studentStatement =
                         connection.prepareStatement(insertStudent)) {
                userStatement.setInt(1, student.getUserId());
                userStatement.setString(2, student.getName());
                userStatement.setString(3, email);
                userStatement.setString(4, passwordHash);
                userStatement.executeUpdate();

                studentStatement.setInt(1, student.getUserId());
                studentStatement.setString(2, student.getName());
                studentStatement.setString(3, email);
                studentStatement.executeUpdate();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public Account findAccountByEmail(String email) throws SQLException {
        String sql = "SELECT user_id, name, email, password_hash "
                + "FROM public.users WHERE lower(email) = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return null;
                }

                int userId = Math.toIntExact(results.getLong("user_id"));
                Student student = new Student(
                        userId,
                        results.getString("name"),
                        results.getString("email")
                );
                return new Account(student, results.getString("password_hash"));
            } catch (ArithmeticException e) {
                throw new SQLException("Student ID is outside the supported range.", e);
            }
        }
    }

    public Student findStudentById(int userId) throws SQLException {
        String sql = "SELECT user_id, name, email "
                + "FROM public.users WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return null;
                }

                Student student = new Student(
                        userId,
                        results.getString("name"),
                        results.getString("email")
                );
                loadSkills(connection, student);
                return student;
            }
        }
    }

    public List<Student> findAllStudents() throws SQLException {
        String sql = "SELECT user_id, name, email "
                + "FROM public.users ORDER BY user_id";
        List<Student> students = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                try {
                    students.add(new Student(
                            Math.toIntExact(results.getLong("user_id")),
                            results.getString("name"),
                            results.getString("email")
                    ));
                } catch (ArithmeticException e) {
                    throw new SQLException(
                            "A database user ID is outside the supported range.", e
                    );
                }
            }

            Map<Integer, Student> byId = new HashMap<>();
            for (Student student : students) {
                byId.put(student.getUserId(), student);
            }
            loadSkills(connection, byId);
        }

        return students;
    }

    public void updateProfile(int userId, String name, String email)
            throws SQLException {
        String updateUser = "UPDATE public.users SET name = ?, email = ? "
                + "WHERE user_id = ?";
        String updateStudent = "UPDATE public.student SET name = ?, email = ? "
                + "WHERE student_id = ?";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement userStatement =
                         connection.prepareStatement(updateUser);
                 PreparedStatement studentStatement =
                         connection.prepareStatement(updateStudent)) {
                userStatement.setString(1, name);
                userStatement.setString(2, email);
                userStatement.setInt(3, userId);
                if (userStatement.executeUpdate() != 1) {
                    throw new SQLException("Student account was not found.");
                }

                studentStatement.setString(1, name);
                studentStatement.setString(2, email);
                studentStatement.setInt(3, userId);
                studentStatement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public Skill addSkill(
            int userId,
            String skillName,
            boolean offered) throws SQLException {
        String insertSql = "INSERT INTO public.user_skills "
                + "(skill_id, user_id, skill_name, skill_type) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement lock =
                             connection.prepareStatement(
                                     "LOCK TABLE public.user_skills IN EXCLUSIVE MODE"
                             )) {
                    lock.execute();
                }

                String skillType = offered ? "TEACH" : "LEARN";
                try (PreparedStatement existing = connection.prepareStatement(
                        "SELECT 1 FROM public.user_skills "
                                + "WHERE user_id = ? AND lower(skill_name) = lower(?) "
                                + "AND skill_type = ? LIMIT 1")) {
                    existing.setInt(1, userId);
                    existing.setString(2, skillName);
                    existing.setString(3, skillType);
                    try (ResultSet results = existing.executeQuery()) {
                        if (results.next()) {
                            throw new IllegalArgumentException(
                                    "This skill is already on your profile."
                            );
                        }
                    }
                }

                long nextSkillId;
                try (PreparedStatement nextId = connection.prepareStatement(
                        "SELECT COALESCE(MAX(skill_id), 0) + 1 "
                                + "FROM public.user_skills");
                     ResultSet results = nextId.executeQuery()) {
                    if (!results.next()) {
                        throw new SQLException("Unable to allocate a skill ID.");
                    }
                    nextSkillId = results.getLong(1);
                }

                if (nextSkillId > Integer.MAX_VALUE) {
                    throw new SQLException("Skill ID exceeds the supported application range.");
                }

                Skill skill = new Skill(
                        (int) nextSkillId,
                        skillName,
                        "",
                        Skill.Level.NONE,
                        ""
                );
                try (PreparedStatement statement =
                             connection.prepareStatement(insertSql)) {
                    statement.setLong(1, nextSkillId);
                    statement.setInt(2, userId);
                    statement.setString(3, skillName);
                    statement.setString(4, skillType);
                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("Skill was not saved.");
                    }
                }
                connection.commit();
                return skill;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } catch (IllegalArgumentException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public void checkConnection() throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1");
             ResultSet ignored = statement.executeQuery()) {
            if (!ignored.next()) {
                throw new SQLException("Database health check returned no result.");
            }
        }
    }

    private void loadSkills(Connection connection, Student student)
            throws SQLException {
        Map<Integer, Student> students = new HashMap<>();
        students.put(student.getUserId(), student);
        loadSkills(connection, students);
    }

    private void loadSkills(
            Connection connection,
            Map<Integer, Student> students) throws SQLException {
        if (students.isEmpty()) {
            return;
        }

        String sql = "SELECT skill_id, user_id, skill_name, skill_type "
                + "FROM public.user_skills ORDER BY skill_id";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                Student student = students.get(results.getInt("user_id"));
                if (student == null) {
                    continue;
                }

                Skill skill = new Skill(
                        results.getInt("skill_id"),
                        results.getString("skill_name"),
                        "",
                        Skill.Level.NONE,
                        ""
                );
                if ("TEACH".equals(results.getString("skill_type"))) {
                    student.addOfferedSkill(skill);
                } else {
                    student.addWantedSkill(skill);
                }
            }
        }
    }

    public static final class Account {
        private final Student student;
        private final String passwordHash;

        private Account(Student student, String passwordHash) {
            this.student = student;
            this.passwordHash = passwordHash;
        }

        public Student getStudent() {
            return student;
        }

        public String getPasswordHash() {
            return passwordHash;
        }
    }
}
