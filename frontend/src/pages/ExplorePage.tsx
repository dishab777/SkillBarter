import { Search } from "lucide-react";
import { useMemo, useState } from "react";
import { useApp } from "../context/AppContext";
import { SkillCard } from "../components/SkillCard";
import { SectionHeading } from "../components/AppLayout";

export function ExplorePage() {
  const { students } = useApp();
  const [query, setQuery] = useState("");
  const offerings = useMemo(
    () => students.flatMap((mentor) =>
      mentor.offeredSkills.map((skill) => ({ mentor, skill })),
    ),
    [students],
  );
  const filtered = offerings.filter(({ mentor, skill }) =>
    `${skill.skillName} ${mentor.name}`.toLowerCase().includes(query.toLowerCase()),
  );

  return (
    <section className="page-gutter content-page">
      <SectionHeading
        eyebrow="The community classroom"
        title="Find your next thing."
        description="Browse skills actually offered by registered students."
      />
      <div className="explore-tools">
        <label className="search-field">
          <Search size={18} />
          <span className="sr-only">Search skills and teachers</span>
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Search skill names or students"
          />
        </label>
      </div>
      <p className="results-count">
        {filtered.length} {filtered.length === 1 ? "skill" : "skills"} in the database
      </p>
      {filtered.length ? (
        <div className="skill-grid">
          {filtered.map(({ mentor, skill }) => (
            <SkillCard
              key={`${mentor.userId}-${skill.skillId}`}
              mentor={mentor}
              skill={skill}
            />
          ))}
        </div>
      ) : (
        <div className="empty-state">
          <span>◌</span>
          <h3>No skills are currently listed.</h3>
          <p>Skills added by registered students will appear here.</p>
        </div>
      )}
    </section>
  );
}
