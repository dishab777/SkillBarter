SkillBarter - External Dependencies / JARs Directory
=====================================================

Because Maven and Gradle are not used in this project, required standalone JARs
are maintained directly.

Required JARs:
1. PostgreSQL JDBC Driver (e.g., postgresql-42.7.3.jar)
   - Official Download: https://jdbc.postgresql.org/download/
   - Enables Java JDBC connectivity to Supabase PostgreSQL over SSL.

2. Jakarta Servlet API (e.g., jakarta.servlet-api-6.0.0.jar)
   - Needed for compiling Servlets locally outside an IDE.
   - For Apache Tomcat 10.1+ / 11, this is provided at runtime in Tomcat's lib/ directory
     (CATALINA_HOME/lib/servlet-api.jar).
   - For standalone compilation via javac, reference this JAR in the -cp classpath.

Classpath usage:
- Compile time:
  javac -cp "lib/*" -d WebContent/WEB-INF/classes src/model/*.java src/util/*.java src/interfaces/*.java src/dao/*.java src/service/*.java src/servlet/*.java

- Deployment to Tomcat:
  Ensure postgresql-42.7.3.jar is copied into WebContent/WEB-INF/lib/ or Tomcat's lib/
  so the runtime classloader can load the PostgreSQL JDBC driver.
