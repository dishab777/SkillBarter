# SkillBarter

SkillBarter is a peer-to-peer skill exchange project. Students share skills they
can teach, find mentors, and arrange sessions to learn from one another.

The repository contains a Java backend built with the JDK `HttpServer`, a React
frontend, and a Supabase PostgreSQL database connection. The frontend can run
in demo mode or connect to the Java API.

## Project structure

```text
src/com/skillsexchange/
  database/                   PostgreSQL JDBC connection and DAO logic
  model/                      User, student, skill, session, rating, match
  server/                     HttpServer-based REST API
  service/                    In-memory system logic for mentor matches and sessions
  Main.java                   Console demo entry point
frontend/                     React, Vite, and TypeScript web app
  src/api/                    Frontend API client and mock fallback logic
  src/data/                   Sample students, skills, sessions, and ratings
  src/pages/                  Landing, dashboard, skills, exchanges, and profile pages
  .env.example                Frontend environment sample
lib/                           PostgreSQL and Gson JARs
out/                           Compiled Java classes, if built locally
```

## Backend setup

The backend uses Java's built-in `com.sun.net.httpserver.HttpServer` and JDBC
to connect to Supabase PostgreSQL. Configure the database credentials in the
repository root `.env` file:

```properties
SUPABASE_DB_HOST=aws-0-ap-northeast-2.pooler.supabase.com
SUPABASE_DB_PORT=5432
SUPABASE_DB_NAME=postgres
SUPABASE_DB_USER=postgres.<project-user>
SUPABASE_DB_PASSWORD=<your-password>
```

Compile the Java source from the repository root:

```powershell
javac -cp ".\lib\postgresql-42.7.14.jar;.\lib\gson-2.13.1.jar" `
  -d .\out `
  (Get-ChildItem -Recurse .\src -Filter *.java | ForEach-Object { $_.FullName })
```

Start the API server:

```powershell
java -cp ".\out;.\lib\postgresql-42.7.14.jar;.\lib\gson-2.13.1.jar" com.skillsexchange.server.ApiServer
```

The server exposes endpoints such as:

- `GET /api/health`
- `GET /api/students`
- `POST /api/students`
- `PUT /api/students/{id}`
- `GET /api/mentors?skillName=...&learnerId=...`
- `GET /api/sessions`
- `POST /api/sessions`
- `PUT /api/sessions/{id}/complete`
- `POST /api/sessions/{id}/ratings`
- `GET /api/ratings`

## Frontend setup

Requirements: Node.js and npm.

From the repository root:

```powershell
cd .\frontend
npm install
npm run dev
```

Open the local URL printed by Vite, usually `http://localhost:5173`.

Set these frontend variables in `frontend/.env.local` when you want to use the
Java backend instead of demo data:

```properties
VITE_USE_MOCK=false
VITE_API_BASE_URL=http://localhost:8080
```

If `VITE_USE_MOCK` is omitted, the app defaults to demo mode for a quick
exploration experience. The API client will make HTTP calls to the backend only
when the mock flag is disabled.

## Database model

The Java DAO expects a PostgreSQL table named `public.student` with at least the
following columns:

```sql
CREATE TABLE public.student (
    student_id integer PRIMARY KEY,
    name text NOT NULL,
    email text NOT NULL UNIQUE
);
```

The project also includes Java model classes for skills, sessions, ratings, and
mentor matching. These are connected to the backend API through the DAO and
service classes.

## Demo data and local persistence

The bundled sample data under `frontend/src/data/` remains available for a
self-contained demo. Interactions are saved in the browser's localStorage when
mock mode is enabled.

The app is intentionally simple and maintainable for an academic OOP mini-
project, with a clear separation between the React UI, Java HTTP server, and the
PostgreSQL data layer.
