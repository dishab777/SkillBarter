import { ArrowLeft, ArrowRight } from "lucide-react";
import { Link, useParams } from "react-router-dom";
import { PersonAvatar } from "../components/PersonAvatar";
import { SectionHeading } from "../components/AppLayout";
import { useApp } from "../context/AppContext";

export function SkillDetailPage() {
  const { id = "" } = useParams();
  const [studentId, skillId] = id.split("-").map(Number);
  const { students } = useApp();
  const mentor = students.find((student) => student.userId === studentId);
  const skill = mentor?.offeredSkills.find((entry) => entry.skillId === skillId);

  if (!mentor || !skill) {
    return (
      <section className="page-gutter content-page">
        <div className="empty-state">
          <h2>This skill is not in the database.</h2>
          <p>Browse skills that registered students have actually added.</p>
          <Link className="button button--primary" to="/skills">Browse skills</Link>
        </div>
      </section>
    );
  }

  return (
    <section className="page-gutter content-page detail-page">
      <Link className="back-link" to="/skills">
        <ArrowLeft size={16} /> Back to all skills
      </Link>
      <div className="detail-layout">
        <div className="detail-main">
          <div className="detail-art">
            <span>Shared skill</span>
            <strong>{skill.skillName.split(" ").map((word) => word[0]).slice(0, 2).join("")}</strong>
            <small>from a registered profile</small>
          </div>
          <p className="eyebrow">Offered by a registered member</p>
          <h1>{skill.skillName}</h1>
          <div className="detail-mentor">
            <PersonAvatar name={mentor.name} size="large" />
            <div><strong>{mentor.name}</strong><p>{mentor.email}</p></div>
          </div>
          <div className="detail-note">
            <span>✳</span>
            <p>This skill was retrieved from the database. Session booking is not available because the current schema has no session date or time records.</p>
          </div>
        </div>
        <aside className="request-panel">
          <p className="eyebrow">Session scheduling</p>
          <h2>Not connected yet.</h2>
          <p>The existing Supabase schema stores accounts, skills, and matches, but not scheduled sessions. No session is created or implied by this screen.</p>
          <Link className="button button--outline button--small" to="/skills">
            Explore another skill <ArrowRight size={15} />
          </Link>
        </aside>
      </div>
      <SectionHeading eyebrow="A little more to explore" title="Other skills from this profile." />
      <div className="detail-tags">
        {mentor.offeredSkills
          .filter((entry) => entry.skillId !== skill.skillId)
          .map((entry) => (
            <Link key={entry.skillId} to={`/skills/${mentor.userId}-${entry.skillId}`}>
              {entry.skillName} <ArrowRight size={14} />
            </Link>
          ))}
      </div>
    </section>
  );
}
