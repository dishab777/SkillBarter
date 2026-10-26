import { ArrowRight, ArrowUpRight, CalendarDays, Compass, Plus, Sparkles, Star } from "lucide-react";
import { Link } from "react-router-dom";
import { PersonAvatar } from "../components/PersonAvatar";
import { SectionHeading } from "../components/AppLayout";
import { useApp } from "../context/AppContext";
import { displayDate } from "../utils/formatters";

export function DashboardPage() {
  const { activeStudent, sessions, students } = useApp();
  if (!activeStudent) {
    return <section className="page-gutter content-page"><SectionHeading eyebrow="Your corner of the community" title="Make this space yours." description="Choose a demo profile or create one to start sharing skills." /><Link className="button button--primary" to="/register">Create a profile <ArrowRight size={16} /></Link></section>;
  }
  const mySessions = sessions.filter((session) => session.learner.userId === activeStudent.userId || session.mentor.userId === activeStudent.userId);
  const upcoming = mySessions.filter((session) => session.status === "SCHEDULED").slice(0, 3);
  const suggestions = students.filter((student) => student.userId !== activeStudent.userId).flatMap((mentor) => mentor.offeredSkills.map((skill) => ({ mentor, skill }))).filter(({ skill }) => activeStudent.wantedSkills.some((wanted) => wanted.skillName.toLowerCase() === skill.skillName.toLowerCase())).slice(0, 2);
  const average = activeStudent.ratings.length ? (activeStudent.ratings.reduce((sum, rating) => sum + rating.score, 0) / activeStudent.ratings.length).toFixed(1) : "—";

  return (
    <section className="page-gutter content-page dashboard-page">
      <div className="dashboard-welcome">
        <div><p className="eyebrow">Your little corner</p><h1>Good morning, {activeStudent.name.split(" ")[0]}<span className="welcome-period">.</span></h1><p>There&apos;s always something new to learn from someone nearby.</p></div>
        <Link className="button button--primary" to="/skills"><Compass size={17} /> Find a skill to learn</Link>
      </div>
      <div className="dashboard-stats">
        <article><span className="stat-icon stat-icon--green"><Sparkles size={18} /></span><div><strong>{activeStudent.offeredSkills.length}</strong><span>skills to share</span></div><Link to="/profile" aria-label="Edit skills you offer"><ArrowUpRight size={16} /></Link></article>
        <article><span className="stat-icon stat-icon--peach"><Compass size={18} /></span><div><strong>{activeStudent.wantedSkills.length}</strong><span>things to learn</span></div><Link to="/profile" aria-label="Edit skills you want to learn"><ArrowUpRight size={16} /></Link></article>
        <article><span className="stat-icon stat-icon--blue"><CalendarDays size={18} /></span><div><strong>{mySessions.filter((session) => session.status === "SCHEDULED").length}</strong><span>upcoming sessions</span></div><Link to="/exchanges" aria-label="View your sessions"><ArrowUpRight size={16} /></Link></article>
        <article><span className="stat-icon stat-icon--gold"><Star size={18} /></span><div><strong>{average}</strong><span>community rating</span></div><Link to="/profile" aria-label="View your profile"><ArrowUpRight size={16} /></Link></article>
      </div>
      <div className="dashboard-columns">
        <div className="dashboard-column">
          <div className="subsection-heading"><div><p className="eyebrow">Coming up</p><h2>Your next exchanges</h2></div><Link className="text-link" to="/exchanges">All exchanges <ArrowRight size={15} /></Link></div>
          <div className="session-list session-list--compact">
            {upcoming.length ? upcoming.map((session) => {
              const other = session.mentor.userId === activeStudent.userId ? session.learner : session.mentor;
              return <article className="session-row" key={session.sessionId}><div className="session-date"><strong>{displayDate(session.date).split(" ")[1]?.replace(",", "") ?? session.date}</strong><span>{displayDate(session.date).split(" ")[0]}</span></div><PersonAvatar name={other.name} size="small" /><div className="session-row__text"><strong>{session.skill.skillName}</strong><span>{session.mentor.userId === activeStudent.userId ? "Teaching" : "Learning"} with {other.name}</span></div><span className="session-time">{session.time}</span></article>;
            }) : <div className="mini-empty"><p>No sessions booked yet.</p><Link to="/skills">Find a mentor <ArrowRight size={14} /></Link></div>}
          </div>
          <div className="subsection-heading subsection-heading--spaced"><div><p className="eyebrow">A two-way street</p><h2>Your skill shelf</h2></div><Link className="icon-add" to="/profile" aria-label="Add a skill"><Plus size={18} /></Link></div>
          <div className="skill-shelf">
            <div><span className="shelf-label">I can teach</span>{activeStudent.offeredSkills.length ? activeStudent.offeredSkills.slice(0, 4).map((skill) => <span className="skill-chip skill-chip--green" key={skill.skillId}>{skill.skillName}</span>) : <Link className="skill-chip skill-chip--add" to="/profile">Add a skill to share <Plus size={13} /></Link>}</div>
            <div><span className="shelf-label">I want to learn</span>{activeStudent.wantedSkills.length ? activeStudent.wantedSkills.slice(0, 4).map((skill) => <span className="skill-chip skill-chip--peach" key={skill.skillId}>{skill.skillName}</span>) : <Link className="skill-chip skill-chip--add" to="/profile">Add a learning goal <Plus size={13} /></Link>}</div>
          </div>
        </div>
        <aside className="dashboard-aside">
          <div className="aside-card aside-card--note"><p className="eyebrow">A good match</p><h3>People learn best from people.</h3><p>When you share what you know, you make space for something new to find you.</p><span className="aside-doodle">✳</span></div>
          <div className="aside-card"><div className="subsection-heading"><div><p className="eyebrow">Your wish list</p><h3>Skills worth a look</h3></div></div>{suggestions.length ? suggestions.map(({ mentor, skill }) => <Link className="suggestion-row" key={`${mentor.userId}-${skill.skillId}`} to={`/skills/${mentor.userId}-${skill.skillId}`}><PersonAvatar name={mentor.name} size="small" /><span><strong>{skill.skillName}</strong><small>with {mentor.name}</small></span><ArrowUpRight size={15} /></Link>) : <p className="muted">Add a learning goal to see matching mentors here.</p>}</div>
        </aside>
      </div>
    </section>
  );
}
