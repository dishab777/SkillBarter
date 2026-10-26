// Backend-shaped Student examples; edit a student's profile or skill ID lists to customize the demo.
import type { Skill, Student } from "../types/models";
import { skills } from "./skills";

const skillById = new Map<number, Skill>(skills.map((skill) => [skill.skillId, skill]));

function getSkill(skillId: number): Skill {
  const skill = skillById.get(skillId);
  if (!skill) throw new Error(`Missing sample skill ${skillId}.`);
  return { ...skill };
}

function makeStudent(
  userId: number,
  name: string,
  email: string,
  offeredSkillIds: number[],
  wantedSkillIds: number[],
): Student {
  const offeredSkills = offeredSkillIds.map((skillId) => {
    const skill = skillById.get(skillId);
    if (!skill) throw new Error(`Missing sample skill ${skillId} for student ${userId}.`);
    return { ...skill };
  });

  return {
    userId,
    name,
    email,
    offeredSkills,
    wantedSkills: wantedSkillIds.map(getSkill),
    ratings: [],
  };
}

export const users: Student[] = [
  makeStudent(1, "Maya Chen", "maya.chen@example.com", [101, 108, 112, 121], [107, 114]),
  makeStudent(2, "Theo Alvarez", "theo.alvarez@example.com", [102, 117], [103, 110]),
  makeStudent(3, "Nia Okafor", "nia.okafor@example.com", [103, 111], [104, 108]),
  makeStudent(4, "Oliver Park", "oliver.park@example.com", [104, 114], [101, 116]),
  makeStudent(5, "Leila Haddad", "leila.haddad@example.com", [105, 113, 115], [107, 118]),
  makeStudent(6, "Sam Rivera", "sam.rivera@example.com", [106, 116], [105, 119]),
  makeStudent(7, "Arjun Mehta", "arjun.mehta@example.com", [107, 120], [102, 117]),
  makeStudent(8, "Zoe Williams", "zoe.williams@example.com", [109, 118, 119, 122], [113, 120]),
];
