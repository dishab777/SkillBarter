# SkillBarter

SkillBarter is a peer-to-peer skill exchange project. Students can create an
account, list skills they can teach or want to learn, and discover skills
entered by other registered students.

The web application uses React, Vite, and TypeScript. Its backend is Core Java
using `com.sun.net.httpserver.HttpServer`, JDBC, and Supabase PostgreSQL. It
does not use Servlets, Spring, Maven, Gradle, or Tomcat.

For the step-by-step frontend run/demo, architecture walkthrough, and viva
preparation, see [`PROJECT_DEMO_WORKFLOW.md`](PROJECT_DEMO_WORKFLOW.md).

## Project structure

```text
src/com/skillsexchange/
  database/                   JDBC connection, student DAO, and user DAO
  interfaces/                 SkillProvider and Rateable contracts
  model/                      User, Student, Skill, Session, Rating, Match
  server/                     HttpServer-based JSON API
  service/                    Authentication and skill-exchange business logic
  Main.java                   Console OOP demonstration
frontend/                     React, Vite, and TypeScript web application
  src/api/                    HTTP API client (no runtime mock-data fallback)
  src/context/                Authenticated user and fetched API state
  src/pages/                  Login, registration, profile, skills, dashboard
database/migrations/          Non-destructive case-insensitive email indexes
lib/                           PostgreSQL and Gson JARs
out/                           Java compilation output
```

## Database schema found in Supabase

The application was checked against the live public schema. It contains:

- `public.users`: `user_id`, `name`, `email`, `password_hash`, `created_at`.
  The password hash is non-null, and user ID/email have unique constraints.
- `public.student`: `student_id`, `name`, `email`. It has a primary key and
  unique email constraint. New web registrations create both rows in one
  transaction using the entered student ID.
- `public.user_skills`: `skill_id`, `user_id`, `skill_name`, `skill_type`,
  `created_at`. Skill type is constrained to `TEACH` or `LEARN`; the user ID
  references `users`.
- `public.matches`: student pairs, skill name, status, and an optional numeric
  rating. It does not contain session date/time or written rating feedback.

The inspected database contained no `users`, `user_skills`, or `matches`
records at inspection time, and had one legacy `student` record. That legacy
row is not considered an authenticated account because it has no corresponding
`users` row and password hash.

Apply [`001_auth_skills.sql`](database/migrations/001_auth_skills.sql) using the
Supabase SQL Editor to enforce case-insensitive email uniqueness at the
database level. It does not delete or rewrite existing records. Skill IDs are
allocated transactionally by the DAO because the inspected `user_skills`
primary key has no sequence/default. Authentication uses the existing
`users.password_hash` column and does not require adding a password column.
If applying the migration to a database with existing users, resolve any
emails that differ only by letter case first; PostgreSQL cannot create the
unique indexes while such duplicates exist.

## Configure the database

Keep credentials in the ignored repository-root `.env` file. Do not copy
database credentials into frontend environment files:

```properties
SUPABASE_DB_HOST=<your-supabase-pooler-host>
SUPABASE_DB_PORT=5432
SUPABASE_DB_NAME=postgres
SUPABASE_DB_USER=<your-database-user>
SUPABASE_DB_PASSWORD=<your-database-password>
```

The Java process does not load `.env` automatically. The PowerShell commands
below load its values into the current terminal process before starting Java.

## Run locally

Use two PowerShell terminals.

### Terminal 1: compile and start the Java API

```powershell
Set-Location C:\Users\hp\Documents\SkillBarter

Get-Content .\.env | ForEach-Object {
  if ($_ -match '^(.*?)=(.*)$') {
    [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
  }
}

Get-ChildItem .\lib

javac -cp ".\lib\postgresql-42.7.14.jar;.\lib\gson-2.13.1.jar" `
  -d .\out `
  (Get-ChildItem -Recurse .\src -Filter *.java | ForEach-Object { $_.FullName })

java -cp ".\out;.\lib\postgresql-42.7.14.jar;.\lib\gson-2.13.1.jar" `
  com.skillsexchange.server.ApiServer
```

The default API port is `8080`. If it is occupied, set a different port before
starting Java and use the same value in `frontend/.env.local`:

```powershell
$env:API_PORT = "8081"
```

Check the API and database connection from another PowerShell terminal:

```powershell
curl.exe http://localhost:8080/api/health
```

A healthy response includes `"database":"connected"`. A database connection
failure returns HTTP 503; the API does not switch to fabricated data.

### Terminal 2: start React/Vite

Create `frontend/.env.local` with the correct API URL:

```properties
VITE_API_BASE_URL=http://localhost:8080
```

Then run:

```powershell
Set-Location C:\Users\hp\Documents\SkillBarter\frontend
npm install
npm run dev
```

Open the Vite URL, normally `http://localhost:5173`. The Java server allows
that origin by default. For another frontend origin, set
`CORS_ALLOWED_ORIGIN` before starting Java.

## Authentication and API

The frontend sends JSON to the Java API. The backend validates values,
hashes passwords with salted PBKDF2-HMAC-SHA-256, and writes the account into
the actual Supabase tables with JDBC. Login returns an `HttpOnly`, `SameSite=Lax`
cookie; the browser JavaScript cannot read the token. Sessions expire after 12
hours and are held in server memory, so restarting Java invalidates them.

Available routes:

- `GET /api/health` — API and database health check.
- `POST /api/auth/register` — create a database-backed account.
- `POST /api/auth/login` — verify the password hash and set the session cookie.
- `GET /api/auth/me` — validate the session and return the current profile.
- `POST /api/auth/logout` — invalidate the server-side session.
- `GET /api/students` — authenticated list of actual account profiles/skills.
- `PUT /api/students/{id}` — update the authenticated user's own profile.
- `POST /api/students/{id}/skills` — add a real skill row to `user_skills`.
- `GET /api/mentors?skillName=...` — find matching real offered skills.

Requests to protected routes without a valid cookie return HTTP 401. The
frontend uses the cookie for session validation and does not store a password,
hash, or auth token in localStorage. Only the visual theme preference uses
localStorage.

The existing schema does not support session dates/times or written rating
feedback. Those flows are explicitly unavailable in the UI and API instead of
showing fabricated sessions or ratings. The `user_skills` schema stores skill
names and whether they are for teaching (`TEACH`) or learning (`LEARN`); the
frontend only requests and displays those stored fields.

## Frontend build

```powershell
Set-Location C:\Users\hp\Documents\SkillBarter\frontend
npm run build
```

Vite writes the production bundle to `frontend/dist/`.
