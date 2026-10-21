package test;

import interfaces.Matchable;
import interfaces.Rateable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import model.Match;
import model.Skill;
import model.Student;
import model.User;
import service.SkillExchangeService;
import util.DBConnection;
import util.JsonUtil;
import util.PasswordUtil;

/**
 * Standalone Verification Suite for SkillBarter.
 * Tests OOP principles, Model validations, Security hashing, JSON utils,
 * and optional live Supabase database connectivity.
 */
public class TestRunner {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" SkillBarter Automated Verification Test Suite");
        System.out.println("=================================================");

        int passed = 0;
        int failed = 0;

        // 1. OOP Principles Test: Student extends User (Inheritance & Polymorphism)
        try {
            User student = new Student(1, "Alice Smith", "alice@college.edu", "dummy_hash", "CSE", 3);
            if (!"Student".equals(student.getRole())) {
                throw new AssertionError("Polymorphism failure: getRole() must return 'Student'");
            }
            if (!"Alice Smith".equals(student.getName())) {
                throw new AssertionError("Encapsulation failure: getName() incorrect");
            }
            System.out.println("[PASS] 1. OOP Inheritance & Polymorphism (Student extends User)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 1. Student inheritance test: " + t.getMessage());
            failed++;
        }

        // 2. Skill Validation Test (Encapsulation & Domain Rules)
        try {
            Skill teach = new Skill(10, 1, "Java Programming", "teach");
            if (!"TEACH".equals(teach.getSkillType())) {
                throw new AssertionError("Skill type should be converted to uppercase 'TEACH'");
            }

            boolean threw = false;
            try {
                new Skill(11, 1, "Invalid", "PLAY");
            } catch (IllegalArgumentException e) {
                threw = true;
            }
            if (!threw) {
                throw new AssertionError("Expected IllegalArgumentException for invalid skill type");
            }
            System.out.println("[PASS] 2. Skill Model validation (TEACH/LEARN enforcement)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 2. Skill model test: " + t.getMessage());
            failed++;
        }

        // 3. Match Model Domain Validations
        try {
            Match m = new Match(1, 101, 102, "Data Structures", "PENDING", 0);
            if (m.getStudentOneID() != 101 || m.getStudentTwoID() != 102) {
                throw new AssertionError("Match student IDs mismatch");
            }

            // Self-matching must be rejected
            boolean threwSelfMatch = false;
            try {
                new Match(2, 101, 101, "Python", "PENDING", 0);
            } catch (IllegalArgumentException e) {
                threwSelfMatch = true;
            }
            if (!threwSelfMatch) {
                throw new AssertionError("Match between identical student IDs should be rejected");
            }

            // Invalid rating (>5) must be rejected
            boolean threwRating = false;
            try {
                new Match(3, 101, 102, "Python", "COMPLETED", 6);
            } catch (IllegalArgumentException e) {
                threwRating = true;
            }
            if (!threwRating) {
                throw new AssertionError("Rating > 5 should be rejected");
            }

            System.out.println("[PASS] 3. Match Model validations (Self-match prevention, Status & Rating bounds)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 3. Match model test: " + t.getMessage());
            failed++;
        }

        // 4. Security & Cryptography: PBKDF2 Hashing and Verification
        try {
            String rawPassword = "SecurePassword@123";
            String hash1 = PasswordUtil.hashPassword(rawPassword);
            String hash2 = PasswordUtil.hashPassword(rawPassword);

            // Salt uniqueness test
            if (hash1.equals(hash2)) {
                throw new AssertionError("Two hashes of the same password should use distinct random salts!");
            }

            // Verification tests
            if (!PasswordUtil.verifyPassword(rawPassword, hash1)) {
                throw new AssertionError("Valid password failed verification against hash");
            }
            if (PasswordUtil.verifyPassword("WrongPassword", hash1)) {
                throw new AssertionError("Incorrect password should not verify!");
            }
            System.out.println("[PASS] 4. Security & PasswordUtil (PBKDF2-HMAC-SHA256 with salted hashes)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 4. Password security test: " + t.getMessage());
            failed++;
        }

        // 5. Interface implementation & Matching Logic
        try {
            SkillExchangeService service = new SkillExchangeService();
            Matchable matchable = service;
            Rateable rateable = service;

            Skill s1TeachJava = new Skill(1, 10, "Java", "TEACH");
            Skill s2LearnJava = new Skill(2, 20, "java", "LEARN");
            Skill s1LearnJava = new Skill(3, 10, "Java", "LEARN"); // Same user

            if (!matchable.isCompatible(s1TeachJava, s2LearnJava)) {
                throw new AssertionError("Complementary skills from distinct users should be compatible");
            }
            if (matchable.isCompatible(s1TeachJava, s1LearnJava)) {
                throw new AssertionError("Skills from same user cannot be compatible");
            }

            if (!rateable.validateRating(5) || !rateable.validateRating(1)) {
                throw new AssertionError("Ratings 1 and 5 should be valid");
            }
            if (rateable.validateRating(0) || rateable.validateRating(6)) {
                throw new AssertionError("Ratings 0 and 6 should be invalid for submission");
            }
            System.out.println("[PASS] 5. Interface implementation (Matchable & Rateable compatibility checks)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 5. Service & Interface test: " + t.getMessage());
            failed++;
        }

        // 6. Lightweight JSON Utilities
        try {
            String jsonSuccess = JsonUtil.success("Operation ok", null);
            if (!jsonSuccess.contains("\"success\":true")) {
                throw new AssertionError("JsonUtil.success output malformed");
            }
            String parsedName = JsonUtil.parseSimpleJson("{\"name\":\"Dishab\",\"age\":21}").get("name");
            if (!"Dishab".equals(parsedName)) {
                throw new AssertionError("JsonUtil parser failed to extract key");
            }
            System.out.println("[PASS] 6. Lightweight JsonUtil (Serialization and single-level parsing)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 6. JsonUtil test: " + t.getMessage());
            failed++;
        }

        // 7. Supabase Database Connection Test (Live Check if env vars present)
        System.out.println("\nChecking Supabase Database Connection...");
        String host = System.getenv("SUPABASE_DB_HOST");
        if (host == null || host.trim().isEmpty()) {
            System.out.println("[INFO] 7. Database test skipped: SUPABASE_DB_HOST environment variable not set.");
            System.out.println("       To run live database tests, set the Supabase environment variables.");
        } else {
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement()) {

                System.out.println("[PASS] 7. Connected to Supabase PostgreSQL successfully!");

                // Check tables
                String checkSql = "SELECT table_name FROM information_schema.tables " +
                                  "WHERE table_schema = 'public' AND table_name IN ('users', 'user_skills', 'matches')";
                try (ResultSet rs = stmt.executeQuery(checkSql)) {
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        System.out.println("       Found table: public." + rs.getString("table_name"));
                    }
                    if (count == 3) {
                        System.out.println("[PASS] All 3 core tables verified in Supabase PostgreSQL!");
                    } else {
                        System.out.println("[WARN] " + count + "/3 tables found. Please run sql/schema.sql in Supabase SQL editor.");
                    }
                }
                passed++;
            } catch (Exception e) {
                System.err.println("[WARN] Database connection attempt failed: " + e.getMessage());
            }
        }

        System.out.println("\n=================================================");
        System.out.printf(" Results: %d Passed, %d Failed%n", passed, failed);
        System.out.println("=================================================");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
