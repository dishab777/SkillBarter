export type SkillLevel =
  | "NONE"
  | "BEGINNER"
  | "INTERMEDIATE"
  | "ADVANCED"
  | "EXPERT";

export type SessionStatus = "SCHEDULED" | "COMPLETED" | "CANCELLED";

export interface Skill {
  skillId: number;
  skillName: string;
  category: string;
  level: SkillLevel;
  learningGoal: string;
}

export interface Student {
  userId: number;
  name: string;
  email: string;
  offeredSkills: Skill[];
  wantedSkills: Skill[];
  ratings: Rating[];
}

export interface Rating {
  ratingId: number;
  fromStudent: Student;
  toStudent: Student;
  score: number;
  feedback: string;
}

export interface Session {
  sessionId: number;
  mentor: Student;
  learner: Student;
  skill: Skill;
  date: string;
  time: string;
  status: SessionStatus;
}

export interface MentorMatch {
  mentor: Student;
  skill: Skill;
  matchScore: number;
}

export interface RegistrationDetails {
  userId: number;
  name: string;
  email: string;
}
