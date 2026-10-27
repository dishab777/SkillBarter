import { ArrowRight, Compass, Plus, Sparkles } from "lucide-react";
import { Link } from "react-router-dom";
import { PersonAvatar } from "../components/PersonAvatar";
import { SectionHeading } from "../components/AppLayout";
import { useApp } from "../context/AppContext";

export function DashboardPage() {
  const { activeStudent, students } = useApp();
  if (!activeStudent) {
    return null;
  }

  const suggestions = students
    .filter((student) => student.userId !== activeStudent.userId)
    .flatMap((mentor) =>
      mentor.offeredSkills
        .filter((skill) =>
          activeStudent.wantedSkills.some(
            (wanted) => wanted.skillName.toLowerCase() === skill.skillName.toLowerCase(),
          ),
        )
        .map((skill) => ({ mentor, skill })),
    )
    .slice(0, 3);

  return (
    <section className="page-gutter content-page dashboard-page">
      <div className="dashboard-welcome">
        <div>
          <p className="eyebrow">Your SkillBarter space</p>
          <h1>Welcome, {activeStudent.name.split(" ")[0]}<span className="welcome-period">.</span></h1>
          <p>Your profile and skills are loaded from the SkillBarter database.</p>
        </div>
        <Link className="button button--primary" to="/skills">
          <Compass size={17} /> Explore skills
        </Link>
      </div>
      <div className="dashboard-stats">
        <article>
          <span className="stat-icon stat-icon--green"><Sparkles size={18} /></span>
          <div><strong>{activeStudent.offeredSkills.length}</strong><span>skills you teach</span></div>
          <Link to="/profile" aria-label="Manage skills you teach"><ArrowRight size={16} /></Link>
        </article>
        <article>
          <span className="stat-icon stat-icon--peach"><Compass size={18} /></span>
          <div><strong>{activeStudent.wantedSkills.length}</strong><span>skills you want to learn</span></div>
          <Link to="/profile" aria-label="Manage skills you want to learn"><ArrowRight size={16} /></Link>
        </article>
        <article>
          <span className="stat-icon stat-icon--blue"><PersonAvatar name={activeStudent.name} size="small" /></span>
          <div><strong>{students.length}</strong><span>registered accounts</span></div>
          <Link to="/skills" aria-label="Explore registered accounts"><ArrowRight size={16} /></Link>
        </article>
      </div>
      <div className="dashboard-columns">
        <div className="dashboard-column">
          <SectionHeading
            eyebrow="A two-way street"
            title="Your skill shelf"
            description="These are the skills saved to your profile."
          />
          <div className="skill-shelf">
            <div>
              <span className="shelf-label">I can teach</span>
              {activeStudent.offeredSkills.length
                ? activeStudent.offeredSkills.map((skill) => (
                    <span className="skill-chip skill-chip--green" key={skill.skillId}>
                      {skill.skillName}
                    </span>
                  ))
                : <Link className="skill-chip skill-chip--add" to="/profile">Add a skill to teach <Plus size={13} /></Link>}
            </div>
            <div>
              <span className="shelf-label">I want to learn</span>
              {activeStudent.wantedSkills.length
                ? activeStudent.wantedSkills.map((skill) => (
                    <span className="skill-chip skill-chip--peach" key={skill.skillId}>
                      {skill.skillName}
                    </span>
                  ))
                : <Link className="skill-chip skill-chip--add" to="/profile">Add a learning goal <Plus size={13} /></Link>}
            </div>
          </div>
          <div className="aside-card session-support-note">
            <p className="eyebrow">Sessions</p>
            <h3>Scheduling is not connected yet.</h3>
            <p>The current Supabase schema has no session date/time records. No sample sessions are shown.</p>
          </div>
        </div>
        <aside className="dashboard-aside">
          <div className="aside-card">
            <div className="subsection-heading">
              <div><p className="eyebrow">From the database</p><h3>Possible skill matches</h3></div>
            </div>
            {suggestions.length
              ? suggestions.map(({ mentor, skill }) => (
                  <div className="suggestion-row" key={`${mentor.userId}-${skill.skillId}`}>
                    <PersonAvatar name={mentor.name} size="small" />
                    <span><strong>{skill.skillName}</strong><small>offered by {mentor.name}</small></span>
                  </div>
                ))
              : <p className="muted">No matching skills are currently recorded. Add a learning goal or explore all skills.</p>}
            <Link className="text-link" to="/skills">Browse skills <ArrowRight size={15} /></Link>
          </div>
        </aside>
      </div>
    </section>
  );
}
