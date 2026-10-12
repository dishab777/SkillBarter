# Peer Skill Exchange Platform

Starter folder structure for a simple academic project using Core Java, JDBC, Java Servlets, HTML/CSS, and Supabase PostgreSQL.

- `src/model`: User, Student, Skill, Match
- `src/interfaces`: Matchable, Rateable
- `src/util`: DBConnection
- `src/dao`: JDBC data-access classes
- `src/service`: business logic
- `src/servlet`: servlet controllers
- `WebContent`: frontend pages, CSS and JavaScript
- `WebContent/WEB-INF`: web deployment configuration
- `lib`: optional location for the PostgreSQL JDBC driver JAR

This ZIP contains a folder structure and starter placeholders, not a complete working application. Replace `YOUR_POOLER_HOST` and `YOUR_POOLER_USERNAME` in DBConnection.java with values copied from Supabase. Set the database password in the `SUPABASE_DB_PASSWORD` environment variable. Do not commit credentials to GitHub.

Use `javax.servlet` with Tomcat 9 and `jakarta.servlet` with Tomcat 10+.
