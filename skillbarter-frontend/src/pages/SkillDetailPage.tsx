import { ArrowLeft, ArrowRight, CalendarDays, Clock3, Star } from "lucide-react";
import { useState, type FormEvent } from "react";
import { Link, useParams } from "react-router-dom";
import { api } from "../api/client";
import { PersonAvatar } from "../components/PersonAvatar";
import { SectionHeading } from "../components/AppLayout";
import { useApp } from "../context/AppContext";
import { averageScore } from "../utils/formatters";

export function SkillDetailPage() {
  const { id = "" } = useParams();
  const [studentId, skillId] = id.split("-").map(Number);
  const { students, activeStudent, perform } = useApp();
  const [message, setMessage] = useState("");
  const mentor = students.find((student) => student.userId === studentId);
  const skill = mentor?.offeredSkills.find((entry) => entry.skillId === skillId);

  if (!mentor || !skill) {
    return <section className="page-gutter content-page"><div className="empty-state"><h2>That skill moved on.</h2><p>Have a look through the skills currently being shared.</p><Link className="button button--primary" to="/skills">Browse skills</Link></div></section>;
  }
  const selectedMentor = mentor;
  const selectedSkill = skill;

  async function requestSession(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!activeStudent) return;
    const form = event.currentTarget;
    const values = new FormData(form);
    const saved = await perform(() =>
      api.scheduleSession(
        selectedMentor.userId,
        activeStudent.userId,
        selectedSkill.skillName,
        String(values.get("date")),
        String(values.get("time")),
      ),
    );
    if (saved) {
      setMessage("Your session is on the calendar. Happy learning!");
      form.reset();
    }
  }

  return (
    <section className="page-gutter content-page detail-page">
      <Link className="back-link" to="/skills"><ArrowLeft size={16} /> Back to all skills</Link>
      <div className="detail-layout">
        <div className="detail-main">
          <div className="detail-art"><span>{selectedSkill.category}</span><strong>{selectedSkill.skillName.split(" ").map((word) => word[0]).slice(0, 2).join("")}</strong><small>shared with care</small></div>
          <p className="eyebrow">{selectedSkill.category} · {selectedSkill.level.toLowerCase()}</p>
          <h1>{selectedSkill.skillName}</h1>
          <p className="detail-intro">Learn directly from someone who&apos;s been there. Start with a session that fits your schedule and see where the exchange takes you.</p>
          <div className="detail-mentor"><PersonAvatar name={selectedMentor.name} size="large" /><div><strong>{selectedMentor.name}</strong><p>{selectedMentor.email}</p></div><span className="rating"><Star size={15} fill="currentColor" /> {averageScore(selectedMentor.ratings.map((rating) => rating.score))}</span></div>
          <div className="detail-note"><span>✳</span><p>Every session is a chance to learn from a real person. Bring questions, curiosity, and anything you&apos;d like to share in return.</p></div>
        </div>
        <aside className="request-panel">
          <p className="eyebrow">Start learning</p><h2>Request a session</h2><p>Choose a time that works for you and {selectedMentor.name.split(" ")[0]}.</p>
          <form className="request-form" onSubmit={(event) => void requestSession(event)}>
            <label><span><CalendarDays size={15} /> Date</span><input name="date" type="date" min={new Date().toISOString().slice(0, 10)} required /></label>
            <label><span><Clock3 size={15} /> Time</span><input name="time" type="time" required /></label>
            <button className="button button--primary button--full" type="submit" disabled={!activeStudent || activeStudent.userId === selectedMentor.userId}>Send session request <ArrowRight size={16} /></button>
          </form>
          {activeStudent?.userId === selectedMentor.userId && <p className="form-hint">Choose another demo profile to request a session with yourself.</p>}
          {message && <p className="success-message" role="status">{message}</p>}
          <Link className="text-link request-link" to="/exchanges">See your exchanges <ArrowRight size={15} /></Link>
        </aside>
      </div>
      <SectionHeading eyebrow="A little more to explore" title="Other ways to grow." />
      <div className="detail-tags">{selectedMentor.offeredSkills.filter((entry) => entry.skillId !== selectedSkill.skillId).map((entry) => <Link key={entry.skillId} to={`/skills/${selectedMentor.userId}-${entry.skillId}`}>{entry.skillName} <ArrowRight size={14} /></Link>)}</div>
    </section>
  );
}
