import { Search, SlidersHorizontal } from "lucide-react";
import { useMemo, useState } from "react";
import { useApp } from "../context/AppContext";
import { SkillCard } from "../components/SkillCard";
import { SectionHeading } from "../components/AppLayout";

export function ExplorePage() {
  const { students } = useApp();
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("All skills");
  const offerings = useMemo(
    () => students.flatMap((mentor) => mentor.offeredSkills.map((skill) => ({ mentor, skill }))),
    [students],
  );
  const categories = ["All skills", ...new Set(offerings.map(({ skill }) => skill.category))];
  const filtered = offerings.filter(({ mentor, skill }) => {
    const text = `${skill.skillName} ${skill.category} ${mentor.name}`.toLowerCase();
    return text.includes(query.toLowerCase()) && (category === "All skills" || skill.category === category);
  });

  return (
    <section className="page-gutter content-page">
      <SectionHeading eyebrow="The community classroom" title="Find your next thing." description="Browse what people are happy to teach. Every skill starts a conversation." />
      <div className="explore-tools">
        <label className="search-field"><Search size={18} /><span className="sr-only">Search skills and teachers</span><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Try watercolor, Spanish, or Maya…" /></label>
        <label className="filter-field"><SlidersHorizontal size={16} /><span className="sr-only">Filter by category</span><select value={category} onChange={(event) => setCategory(event.target.value)}>{categories.map((item) => <option key={item}>{item}</option>)}</select></label>
      </div>
      <p className="results-count">{filtered.length} {filtered.length === 1 ? "skill" : "skills"} to get curious about</p>
      {filtered.length ? <div className="skill-grid">{filtered.map(({ mentor, skill }) => <SkillCard key={`${mentor.userId}-${skill.skillId}`} mentor={mentor} skill={skill} />)}</div> : <div className="empty-state"><span>◌</span><h3>No matches just yet</h3><p>Try a different search or open up the category filter.</p></div>}
    </section>
  );
}
