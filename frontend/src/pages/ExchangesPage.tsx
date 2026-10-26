import { ArrowRight, CalendarDays, Check, Clock3, Star } from "lucide-react";
import { useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client";
import { PersonAvatar } from "../components/PersonAvatar";
import { SectionHeading } from "../components/AppLayout";
import { useApp } from "../context/AppContext";
import { displayDate } from "../utils/formatters";
import type { SessionStatus } from "../types/models";

const tabs: { value: SessionStatus | "ALL"; label: string }[] = [
  { value: "ALL", label: "All exchanges" },
  { value: "SCHEDULED", label: "Scheduled" },
  { value: "COMPLETED", label: "Completed" },
  { value: "CANCELLED", label: "Cancelled" },
];

export function ExchangesPage() {
  const { activeStudent, sessions, perform } = useApp();
  const [selected, setSelected] = useState<SessionStatus | "ALL">("ALL");
  const [ratedSession, setRatedSession] = useState<number | null>(null);
  const mine = sessions
    .filter((session) => activeStudent && (session.mentor.userId === activeStudent.userId || session.learner.userId === activeStudent.userId))
    .filter((session) => selected === "ALL" || session.status === selected)
    .sort((a, b) => b.date.localeCompare(a.date));

  async function complete(sessionId: number) {
    await perform(() => api.completeSession(sessionId));
  }

  async function rate(event: FormEvent<HTMLFormElement>, sessionId: number) {
    event.preventDefault();
    const values = new FormData(event.currentTarget);
    const success = await perform(() => api.rateMentor(sessionId, Number(values.get("score")), String(values.get("feedback") ?? "")));
    if (success) setRatedSession(sessionId);
  }

  return (
    <section className="page-gutter content-page">
      <SectionHeading eyebrow="Learning, in motion" title="Your exchanges." description="Every good skill swap starts with showing up for each other." action={<Link className="button button--primary" to="/skills">Find a new swap <ArrowRight size={16} /></Link>} />
      <div className="tab-row" role="tablist" aria-label="Filter exchanges">{tabs.map((tab) => <button key={tab.value} className={selected === tab.value ? "filter-tab filter-tab--active" : "filter-tab"} role="tab" aria-selected={selected === tab.value} onClick={() => setSelected(tab.value)}>{tab.label}<span>{sessions.filter((session) => (tab.value === "ALL" || session.status === tab.value) && activeStudent && (session.mentor.userId === activeStudent.userId || session.learner.userId === activeStudent.userId)).length}</span></button>)}</div>
      <div className="exchange-list">
        {mine.length ? mine.map((session) => {
          const isMentor = session.mentor.userId === activeStudent?.userId;
          const other = isMentor ? session.learner : session.mentor;
          return <article className="exchange-card" key={session.sessionId}>
            <div className={`exchange-card__status status-${session.status.toLowerCase()}`}><span />{session.status.toLowerCase()}</div>
            <div className="exchange-card__main"><PersonAvatar name={other.name} size="medium" /><div><p className="eyebrow">{isMentor ? "You are teaching" : "You are learning"}</p><h3>{session.skill.skillName}</h3><p className="muted">{isMentor ? "Sharing with" : "Learning with"} <strong>{other.name}</strong></p></div></div>
            <div className="exchange-card__details"><span><CalendarDays size={15} /> {displayDate(session.date)}</span><span><Clock3 size={15} /> {session.time}</span></div>
            {session.status === "SCHEDULED" && <button className="button button--outline button--small" onClick={() => void complete(session.sessionId)}><Check size={15} /> Mark complete</button>}
            {session.status === "COMPLETED" && !isMentor && <form className="rating-form" onSubmit={(event) => void rate(event, session.sessionId)}><label><span className="sr-only">Rating for {other.name}</span><select name="score" defaultValue="5" aria-label="Choose a rating"><option value="5">★★★★★ · Loved it</option><option value="4">★★★★ · Really good</option><option value="3">★★★ · Good</option><option value="2">★★ · Could be better</option><option value="1">★ · Not for me</option></select></label><input name="feedback" placeholder="Add a kind note (optional)" aria-label="Optional feedback" /><button className="button button--outline button--small" type="submit"><Star size={14} /> {ratedSession === session.sessionId ? "Rated" : "Rate mentor"}</button></form>}
          </article>;
        }) : <div className="empty-state"><span>↔</span><h3>Nothing in this corner yet.</h3><p>Find a person with a skill to share and set up your first session.</p><Link className="button button--primary" to="/skills">Explore skills <ArrowRight size={16} /></Link></div>}
      </div>
    </section>
  );
}
