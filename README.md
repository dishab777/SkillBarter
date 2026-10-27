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
database/migrations/          Non-destructive email and password migrations
lib/                           PostgreSQL, Gson, and BCrypt JARs
out/                           Java compilation output
```

## Database schema found in Supabase

The application was checked against the live public schema. It contains:

- `public.users`: `user_id`, `name`, `email`, `password_hash`, `created_at`.
  The password hash is non-null, and user ID/email have unique constraints.
- `public.student`: `student_id`, `name`, `email`, and nullable
  `password_hash`. It has a primary key and unique email constraint. New web
  registrations create both user/profile rows in one transaction using the
  entered student ID; console registrations store a BCrypt hash in the
  student's nullable password column.
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
database level. Apply
[`002_console_student_password.sql`](database/migrations/002_console_student_password.sql)
to add the nullable `student.password_hash` column for console registrations.
Both migrations preserve existing records. Older students keep a `NULL`
password hash; no password is invented for them. Skill IDs are allocated
transactionally by the DAO because the inspected `user_skills` primary key
has no sequence/default. Web authentication continues to use the existing
`users.password_hash` column.
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

javac -cp ".\lib\postgresql-42.7.14.jar;.\lib\gson-2.13.1.jar;.\lib\jbcrypt-0.4.jar" `
  -d .\out `
  (Get-ChildItem -Recurse .\src -Filter *.java | ForEach-Object { $_.FullName })

java -cp ".\out;.\lib\postgresql-42.7.14.jar;.\lib\gson-2.13.1.jar;.\lib\jbcrypt-0.4.jar" `
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

## Console registration passwords

The separate console menu (`com.skillsexchange.Main`, option 1) asks for and
confirms a password using Java's `Console.readPassword`, so typed characters
are not echoed. Run it from a real Windows Terminal/PowerShell window; IDE
output panes often have no `System.console()`, in which case registration
stops with an explanation instead of accepting a visible password. The
password is hashed with the standalone jBCrypt 0.4 library (BCrypt cost 12)
and only the hash is written to `public.student.password_hash`. Password
character arrays are cleared after use. BCrypt's 72-byte input limit is
validated so long passwords are not silently truncated.

The console and web sign-in paths currently use different existing tables:
console registration writes a BCrypt hash to `student.password_hash`, while
web registration/login use PBKDF2 hashes in `users.password_hash`. Do not
copy a hash between those columns or assume the console account can sign into
the web application; unifying those flows would require a separate migration
and authentication design.

### Run the console application

Apply the password-column migration first, then open a PowerShell terminal:

```powershell
Set-Location C:\Users\hp\Documents\SkillBarter

Get-Content .\.env | ForEach-Object {
  if ($_ -match '^(.*?)=(.*)$') {
    [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
  }
}

$sources = Get-ChildItem .\src -Filter *.java -Recurse |
  ForEach-Object { $_.FullName }

javac -cp ".\lib\postgresql-42.7.14.jar;.\lib\gson-2.13.1.jar;.\lib\jbcrypt-0.4.jar" `
  -d .\out $sources

java -cp ".\out;.\lib\postgresql-42.7.14.jar;.\lib\gson-2.13.1.jar;.\lib\jbcrypt-0.4.jar" `
  com.skillsexchange.Main
```

`lib/jbcrypt-0.4.jar` is the standalone `org.mindrot:jbcrypt:0.4` dependency;
no Maven or Gradle is required. If you need to restore it, download the JAR
from Maven Central at
`https://repo.maven.apache.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar`
and place it in `lib`.

The migration SQL is:

```sql
ALTER TABLE public.student
    ADD COLUMN IF NOT EXISTS password_hash TEXT;
```

## Frontend build

```powershell
Set-Location C:\Users\hp\Documents\SkillBarter\frontend
npm run build
```

Vite writes the production bundle to `frontend/dist/`.

## Deploy the frontend to Vercel

Vercel hosts the React/Vite frontend; it does not start this project's Core
Java `HttpServer`. Deploy the Java API separately to a Java-capable host and
configure that service with the `SUPABASE_DB_*` variables. Keep the API
running and reachable over HTTPS.

In the Vercel project settings:

1. Set **Root Directory** to `frontend`.
2. Add `VITE_API_BASE_URL` for the Production environment with the public HTTPS
   origin of the Java API, for example `https://api.example.com`. Do not add
   `/api` to the value; the frontend appends `/api/...` itself.
3. Redeploy after adding or changing the variable. Vite embeds `VITE_*`
   settings during the build, so changing a Vercel variable does not update an
   already-built deployment.

In the Java API host's environment settings:

1. Set `CORS_ALLOWED_ORIGIN` to the exact frontend origin, such as
   `https://skill-barter-pi.vercel.app` (no trailing slash).
2. Set `APP_COOKIE_SECURE=true` and retain the database variables on the
   server only.
3. Restart/redeploy the API after changing environment variables.

With `APP_COOKIE_SECURE=true`, the API marks the session cookie `Secure` and
`SameSite=None` so cross-origin HTTPS fetch requests can send it. Browsers or
privacy settings that block third-party cookies may still prevent sessions
when the frontend and API are on unrelated domains. For the most reliable
authentication, use frontend and API hostnames under the same custom domain
(for example `app.example.com` and `api.example.com`) or proxy `/api` through
the frontend's same site.

If the login page reports that it cannot connect to `http://localhost:8080`,
the deployed bundle is using its local-development default. Configure
`VITE_API_BASE_URL` in Vercel and redeploy; `localhost` on a visitor's device
is not your deployed API.
