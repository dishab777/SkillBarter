package com.skillsexchange.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBTest {

    public static void main(String[] args) {

        System.out.println("Testing Supabase PostgreSQL connection...");

        try (Connection con = DBConnection.getConnection()) {

            System.out.println("Connection successful!");
            System.out.println("Database: " + con.getCatalog());

            String sql = "SELECT version()";

            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                if (rs.next()) {
                    System.out.println(
                            "PostgreSQL version: " + rs.getString(1)
                    );
                }
            }

            System.out.println("JDBC test completed successfully.");

        } catch (Exception e) {

            System.out.println("Database connection failed.");
            System.out.println("Error: " + e.getMessage());

            e.printStackTrace();
        }
    }
}