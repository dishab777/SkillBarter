# SkillBarter

SkillBarter is a peer-to-peer skill exchange project. People share skills they
can teach, find mentors, and arrange sessions to learn from one another.

The repository contains a Java console application and a React web frontend.
The frontend starts in demo mode with realistic sample data, so it can be
explored without running MySQL or the Java application.

## Project structure

```text
src/com/skillsexchange/       Java console application
  Main.java                   Interactive command-line menu
  database/                   MySQL connection and student DAO
  model/                      User, student, skill, session, rating, and match
  service/                    In-memory skill exchange operations
frontend/                     React, Vite, and TypeScript web application
  src/api/                    Frontend data access and demo persistence
  src/data/                   Sample students, skills, sessions, and ratings
  src/pages/                  Landing, dashboard, skills, exchanges, and profile
  vercel.json                 SPA route rewrites for Vercel
lib/                           MySQL Connector/J driver
out/                           Compiled Java classes, if built locally
```

## Web frontend

### Run locally

Requirements: Node.js and npm.

In PowerShell, from the repository root:

```powershell
cd .\frontend
npm install
npm run dev
```

Open the local URL printed by Vite, usually `http://localhost:5173`. Stop the
development server with **Ctrl+C**.

To build and locally preview the production frontend:

```powershell
npm run build
npm run preview
```

Vite writes the production build to `frontend/dist/`. The Vercel configuration
rewrites application routes to the SPA entry point, allowing pages to load on
direct navigation and refresh.

### Demo data and settings

The default configuration uses mock data; no environment file is needed. To
create a local environment file, copy the example:

```powershell
Copy-Item .env.example .env.local
```

| Variable | Default | Purpose |
| --- | --- | --- |
| `VITE_USE_MOCK` | `true` | Uses bundled sample data and browser-local persistence. |
| `VITE_API_BASE_URL` | `http://localhost:8080` in `.env.example` | Reserved base URL for a future backend HTTP API. |

Edit the example data in `frontend/src/data/skills.ts`,
`frontend/src/data/users.ts`, `frontend/src/data/exchanges.ts`, and
`frontend/src/data/reviews.ts`. The files include comments describing their
backend-shaped data. After editing the bundled sample data, use **Reset demo
data** in the app, or clear the browser's `localStorage`, to discard an older
saved demo and load the edited examples.

Interactions such as new profiles, skills, sessions, and ratings are saved in
the current browser's `localStorage`. They are local to that browser and are
not shared between visitors or persisted to MySQL.

### Deploy to Vercel

Configure the Vercel project with:

- **Root Directory:** `frontend`
- **Framework Preset:** Vite
- **Build Command:** `npm run build`
- **Output Directory:** `dist`

Keep `VITE_USE_MOCK=true` for a self-contained deployed demo. Changing
`VITE_API_BASE_URL` alone does not connect the frontend to the Java application.

## Java console application

Requirements: a JDK and a running MySQL server. The application connects to
the `peer_skill_exchange` database. Review
[`DBConnection.java`](src/com/skillsexchange/database/DBConnection.java) and
configure its connection URL and credentials for your local MySQL instance.
The DAO expects a `Student` table with `student_id`, `name`, and `email`
columns; the repository currently does not include a SQL schema.

From the repository root in PowerShell, compile the Java source files:

```powershell
javac -cp ".\lib\mysql-connector-j-26.10.0.jar" `
  -d .\out `
  (Get-ChildItem -Recurse .\src -Filter *.java | ForEach-Object { $_.FullName })
```

Run the interactive application:

```powershell
java -cp ".\out;.\lib\mysql-connector-j-26.10.0.jar" com.skillsexchange.Main
```

The Java menu supports student registration, adding offered or wanted skills,
viewing profiles, searching for mentors, scheduling and viewing sessions,
completing sessions, rating mentors, and listing students. Student records are
written through the MySQL DAO. Skills and sessions are managed in memory by
the application and are not exposed through a web API.

## Backend integration status

The Java application is a console program, not an HTTP server. It currently
provides no REST controllers, HTTP endpoints, authentication, or API error
responses. Consequently, the frontend's functional workflows currently use
sample data and browser-local storage. The frontend reports an error if mock
mode is turned off rather than sending requests to nonexistent endpoints.

To connect the frontend to a hosted backend, the Java project first needs an
HTTP API for the frontend operations. Configure that API to allow requests
from the deployed frontend's origin (CORS), then set `VITE_USE_MOCK=false` and
`VITE_API_BASE_URL` to the hosted API URL when the API integration is
implemented.
