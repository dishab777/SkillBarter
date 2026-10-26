import { ArrowUpRight, Star } from "lucide-react";
import { Link } from "react-router-dom";
import type { Skill, Student } from "../types/models";
import { averageScore } from "../utils/formatters";
import { PersonAvatar } from "./PersonAvatar";
import { useTilt } from "../hooks/useTilt";

const accents = ["mint", "peach", "blue", "lilac", "gold"];

export function SkillCard({
  skill,
  mentor,
}: {
  skill: Skill;
  mentor: Student;
}) {
  const tilt = useTilt();

  return (
    <Link
      className="skill-card tilt-card"
      to={`/skills/${mentor.userId}-${skill.skillId}`}
      onPointerMove={tilt.onPointerMove}
      onPointerLeave={tilt.onPointerLeave}
      aria-label={`${skill.skillName} with ${mentor.name}`}
    >
      <div className={`skill-card__art art-${accents[skill.skillId % accents.length]}`}>
        <span className="skill-card__category">{skill.category}</span>
        <span className="skill-card__art-mark" aria-hidden="true">
          {skill.skillName.split(" ").slice(0, 2).map((word) => word[0]).join("")}
        </span>
        <span className="skill-card__arrow"><ArrowUpRight size={17} /></span>
      </div>
      <div className="skill-card__body">
        <div className="skill-card__title-row">
          <div>
            <h3>{skill.skillName}</h3>
            <p className="muted">{skill.level.toLowerCase()} · offered by</p>
          </div>
          <span className="rating"><Star size={14} fill="currentColor" /> {averageScore(mentor.ratings.map((rating) => rating.score))}</span>
        </div>
        <div className="skill-card__person">
          <PersonAvatar name={mentor.name} size="small" />
          <span>{mentor.name}</span>
          <span className="skill-card__exchange">Skill for skill</span>
        </div>
      </div>
    </Link>
  );
}
