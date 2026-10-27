export interface Skill {
  skillId: number;
  skillName: string;
}

export interface Student {
  userId: number;
  name: string;
  email: string;
  offeredSkills: Skill[];
  wantedSkills: Skill[];
}

export interface MentorMatch {
  mentor: Student;
  skill: Skill;
}

export interface RegistrationDetails {
  userId: number;
  name: string;
  email: string;
}
