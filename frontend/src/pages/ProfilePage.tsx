import { ArrowRight, Check, Plus, Save, Star } from "lucide-react";
import { useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client";
import { SectionHeading } from "../components/AppLayout";
import { PersonAvatar } from "../components/PersonAvatar";
import { useApp } from "../context/AppContext";
import type { SkillLevel } from "../types/models";
import { averageScore } from "../utils/formatters";

const levels: SkillLevel[] = ["BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"];

export function ProfilePage() {
  const { activeStudent, perform } = useApp();
  const [saved, setSaved] = useState(false);
  const [newSkillSaved, setNewSkillSaved] = useState(false);
  const [skillType, setSkillType] = useState<"offered" | "wanted">("offered");

  if (!activeStudent) {
    return <section className="page-gutter content-page"><SectionHeading eyebrow="Your profile" title="A place to begin." description="Create a local demo profile to share the skills you can teach and the things you'd like to learn." /><Link className="button button--primary" to="/register">Create a profile <ArrowRight size={16} /></Link></section>;
  }

  async function saveProfile(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!activeStudent) return;
    const values = new FormData(event.currentTarget);
    const success = await perform(() => api.updateStudent(activeStudent.userId, { name: String(values.get("name")).trim(), email: String(values.get("email")).trim() }));
    if (success) setSaved(true);
  }

  async function addSkill(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!activeStudent) return;
    const form = event.currentTarget;
    const values = new FormData(form);
    const success = await perform(() => api.addStudentSkill(activeStudent.userId, {
      skillName: String(values.get("skillName")).trim(),
      category: String(values.get("category")).trim(),
      level: String(values.get("level")) as SkillLevel,
      learningGoal: skillType === "wanted" ? String(values.get("learningGoal")).trim() : "",
    }, skillType === "offered"));
    if (success) {
      setNewSkillSaved(true);
      form.reset();
    }
  }

  return (
    <section className="page-gutter content-page profile-page">
      <SectionHeading eyebrow="The person behind the skills" title="Your profile." description="A little about you makes it easier for good exchanges to find their way." />
      <div className="profile-layout">
        <aside className="profile-summary">
          <PersonAvatar name={activeStudent.name} size="large" />
          <h2>{activeStudent.name}</h2><p>{activeStudent.email}</p>
          <div className="profile-rating"><Star size={16} fill="currentColor" /><strong>{averageScore(activeStudent.ratings.map((rating) => rating.score))}</strong><span>{activeStudent.ratings.length} community {activeStudent.ratings.length === 1 ? "review" : "reviews"}</span></div>
          <div className="profile-counts"><div><strong>{activeStudent.offeredSkills.length}</strong><span>teaching</span></div><div><strong>{activeStudent.wantedSkills.length}</strong><span>learning</span></div></div>
        </aside>
        <div className="profile-content">
          <form className="profile-panel stacked-form" key={activeStudent.userId} onSubmit={(event) => void saveProfile(event)}>
            <div className="subsection-heading"><div><p className="eyebrow">The basics</p><h2>Say hello.</h2></div></div>
            <label>Your name<input name="name" defaultValue={activeStudent.name} required /></label>
            <label>Email address<input name="email" type="email" defaultValue={activeStudent.email} required /></label>
            <button className="button button--primary button--small" type="submit"><Save size={15} /> Save profile</button>
            {saved && <span className="success-inline"><Check size={14} /> Profile saved</span>}
          </form>
          <div className="profile-panel">
            <div className="subsection-heading"><div><p className="eyebrow">A two-way street</p><h2>Skills in your orbit.</h2></div></div>
            <div className="profile-skill-groups">
              <div><h3>I can teach</h3><div className="profile-skill-list">{activeStudent.offeredSkills.length ? activeStudent.offeredSkills.map((skill) => <span className="skill-chip skill-chip--green" key={skill.skillId}>{skill.skillName}<small>{skill.level.toLowerCase()}</small></span>) : <p className="muted">Nothing here yet. Add a skill below.</p>}</div></div>
              <div><h3>I&apos;d love to learn</h3><div className="profile-skill-list">{activeStudent.wantedSkills.length ? activeStudent.wantedSkills.map((skill) => <span className="skill-chip skill-chip--peach" key={skill.skillId}>{skill.skillName}{skill.learningGoal && <small>{skill.learningGoal}</small>}</span>) : <p className="muted">A little curiosity goes a long way.</p>}</div></div>
            </div>
          </div>
          <div className="profile-panel add-skill-panel">
            <div className="subsection-heading"><div><p className="eyebrow">Make an exchange possible</p><h2>Add a skill.</h2></div><span className="add-skill-mark"><Plus size={19} /></span></div>
            <div className="skill-type-toggle"><button className={skillType === "offered" ? "selected" : ""} onClick={() => setSkillType("offered")} type="button">I can teach</button><button className={skillType === "wanted" ? "selected" : ""} onClick={() => setSkillType("wanted")} type="button">I want to learn</button></div>
            <form className="skill-entry-form" onSubmit={(event) => void addSkill(event)}>
              <label>Skill name<input name="skillName" placeholder="e.g. French conversation" required /></label>
              <label>Category<input name="category" placeholder="e.g. Languages" required /></label>
              <label>Level<select name="level" defaultValue="BEGINNER">{levels.map((level) => <option key={level} value={level}>{level.charAt(0) + level.slice(1).toLowerCase()}</option>)}</select></label>
              {skillType === "wanted" && <label className="skill-entry-form__goal">Learning goal<input name="learningGoal" placeholder="What would you like to do?" /></label>}
              <button className="button button--primary button--small" type="submit"><Plus size={15} /> Add to my profile</button>
            </form>
            {newSkillSaved && <span className="success-inline"><Check size={14} /> Skill added to your profile</span>}
          </div>
          {activeStudent.ratings.length > 0 && <div className="profile-panel"><div className="subsection-heading"><div><p className="eyebrow">Kind words</p><h2>Notes from your exchanges.</h2></div></div><div className="reviews-list">{activeStudent.ratings.map((rating) => <article key={rating.ratingId}><span className="rating"><Star size={14} fill="currentColor" /> {rating.score}.0</span><p>“{rating.feedback}”</p><small>{rating.fromStudent.name}</small></article>)}</div></div>}
        </div>
      </div>
    </section>
  );
}
