import { Check, Plus, Save } from "lucide-react";
import { useState, type FormEvent } from "react";
import { api } from "../api/client";
import { SectionHeading } from "../components/AppLayout";
import { PersonAvatar } from "../components/PersonAvatar";
import { useApp } from "../context/AppContext";

export function ProfilePage() {
  const { activeStudent, perform } = useApp();
  const [saved, setSaved] = useState(false);
  const [newSkillSaved, setNewSkillSaved] = useState(false);
  const [skillType, setSkillType] = useState<"offered" | "wanted">("offered");

  if (!activeStudent) return null;

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
              <div><h3>I can teach</h3><div className="profile-skill-list">{activeStudent.offeredSkills.length ? activeStudent.offeredSkills.map((skill) => <span className="skill-chip skill-chip--green" key={skill.skillId}>{skill.skillName}</span>) : <p className="muted">Nothing here yet. Add a skill below.</p>}</div></div>
              <div><h3>I&apos;d love to learn</h3><div className="profile-skill-list">{activeStudent.wantedSkills.length ? activeStudent.wantedSkills.map((skill) => <span className="skill-chip skill-chip--peach" key={skill.skillId}>{skill.skillName}</span>) : <p className="muted">A little curiosity goes a long way.</p>}</div></div>
            </div>
          </div>
          <div className="profile-panel add-skill-panel">
            <div className="subsection-heading"><div><p className="eyebrow">Make an exchange possible</p><h2>Add a skill.</h2></div><span className="add-skill-mark"><Plus size={19} /></span></div>
            <div className="skill-type-toggle"><button className={skillType === "offered" ? "selected" : ""} onClick={() => setSkillType("offered")} type="button">I can teach</button><button className={skillType === "wanted" ? "selected" : ""} onClick={() => setSkillType("wanted")} type="button">I want to learn</button></div>
            <form className="skill-entry-form" onSubmit={(event) => void addSkill(event)}>
              <label>Skill name<input name="skillName" maxLength={100} required /></label>
              <button className="button button--primary button--small" type="submit"><Plus size={15} /> Add to my profile</button>
            </form>
            {newSkillSaved && <span className="success-inline"><Check size={14} /> Skill added to your profile</span>}
          </div>
          <div className="profile-panel">
            <p className="eyebrow">Ratings and exchanges</p>
            <p className="muted">These features are not stored in the current database schema, so no ratings or sessions are fabricated here.</p>
          </div>
        </div>
      </div>
    </section>
  );
}
