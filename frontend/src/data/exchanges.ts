// Backend-shaped Session examples; statuses use the Java SessionStatus enum values.
import type { Session, Student } from "../types/models";
import { users } from "./users";
import { findSkill } from "./skills";

function studentReference(userId: number): Student {
  const student = users.find((user) => user.userId === userId);
  if (!student) throw new Error(`Missing sample student ${userId} for a session.`);
  return { ...student, ratings: [] };
}

function sampleSession(
  sessionId: number,
  mentorId: number,
  learnerId: number,
  skillId: number,
  date: string,
  time: string,
  status: Session["status"],
): Session {
  const skill = findSkill(skillId);
  if (!skill) throw new Error(`Missing sample skill ${skillId} for session ${sessionId}.`);
  return {
    sessionId,
    mentor: studentReference(mentorId),
    learner: studentReference(learnerId),
    skill: { ...skill },
    date,
    time,
    status,
  };
}

export const exchanges: Session[] = [
  sampleSession(1, 3, 1, 103, "2026-10-28", "17:30", "SCHEDULED"),
  sampleSession(2, 2, 5, 102, "2026-10-30", "18:00", "SCHEDULED"),
  sampleSession(3, 7, 4, 107, "2026-10-19", "16:00", "COMPLETED"),
  sampleSession(4, 1, 8, 108, "2026-10-17", "11:00", "COMPLETED"),
  sampleSession(5, 4, 3, 104, "2026-10-31", "15:00", "SCHEDULED"),
  sampleSession(6, 6, 2, 116, "2026-10-13", "13:30", "COMPLETED"),
  sampleSession(7, 5, 7, 105, "2026-10-22", "10:00", "CANCELLED"),
  sampleSession(8, 8, 6, 119, "2026-11-02", "14:00", "SCHEDULED"),
];
