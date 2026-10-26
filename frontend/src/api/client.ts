import { exchanges } from "../data/exchanges";
import { reviews } from "../data/reviews";
import { users } from "../data/users";
import type {
  MentorMatch,
  Rating,
  RegistrationDetails,
  Session,
  Skill,
  SkillLevel,
  Student,
} from "../types/models";

const DATA_KEY = "skillbarter-demo-data-v1";
const ACTIVE_STUDENT_KEY = "skillbarter-active-student-v1";

interface DemoState {
  students: Student[];
  sessions: Session[];
  ratings: Rating[];
}

export const useMockData =
  import.meta.env.VITE_USE_MOCK?.trim().toLowerCase() !== "false";

export const apiBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim() ?? "";

function freshDemoState(): DemoState {
  return {
    students: structuredClone(users),
    sessions: structuredClone(exchanges),
    ratings: structuredClone(reviews),
  };
}

function readDemoState(): DemoState {
  const savedState = window.localStorage.getItem(DATA_KEY);
  if (!savedState) return freshDemoState();

  try {
    return JSON.parse(savedState) as DemoState;
  } catch {
    throw new Error(
      "Saved demo data could not be read. Use Reset demo data to restore the sample.",
    );
  }
}

function saveDemoState(state: DemoState): void {
  window.localStorage.setItem(DATA_KEY, JSON.stringify(state));
}

function withRatings(student: Student, allRatings: Rating[]): Student {
  return {
    ...student,
    ratings: allRatings
      .filter((rating) => rating.toStudent.userId === student.userId)
      .map((rating) => ({ ...rating })),
  };
}

function averageRating(student: Student, allRatings: Rating[]): number {
  const ratings = allRatings.filter(
    (rating) => rating.toStudent.userId === student.userId,
  );
  return ratings.length
    ? ratings.reduce((sum, rating) => sum + rating.score, 0) / ratings.length
    : 0;
}

function averageSkillLevel(level: SkillLevel): number {
  return ["NONE", "BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"].indexOf(
    level,
  );
}

function requireMockMode(): void {
  if (!useMockData) {
    const location = apiBaseUrl || "VITE_API_BASE_URL";
    throw new Error(
      `The Java project currently has no HTTP API or controllers. ${location} cannot serve these features until REST endpoints are added.`,
    );
  }
}

function nextSkillId(state: DemoState): number {
  const allSkills = state.students.flatMap((student) => [
    ...student.offeredSkills,
    ...student.wantedSkills,
  ]);
  return Math.max(0, ...allSkills.map((skill) => skill.skillId)) + 1;
}

export const api = {
  async getStudents(): Promise<Student[]> {
    requireMockMode();
    const state = readDemoState();
    return state.students.map((student) => withRatings(student, state.ratings));
  },

  async getStudent(userId: number): Promise<Student | undefined> {
    return (await this.getStudents()).find((student) => student.userId === userId);
  },

  async registerStudent(details: RegistrationDetails): Promise<Student> {
    requireMockMode();
    const state = readDemoState();
    if (state.students.some((student) => student.userId === details.userId)) {
      throw new Error("Student ID already registered.");
    }
    const student: Student = {
      ...details,
      offeredSkills: [],
      wantedSkills: [],
      ratings: [],
    };
    state.students.push(student);
    saveDemoState(state);
    return student;
  },

  async updateStudent(
    userId: number,
    details: Pick<Student, "name" | "email">,
  ): Promise<Student> {
    requireMockMode();
    const state = readDemoState();
    const student = state.students.find((entry) => entry.userId === userId);
    if (!student) throw new Error("Student not found.");
    student.name = details.name;
    student.email = details.email;
    saveDemoState(state);
    return withRatings(student, state.ratings);
  },

  async addStudentSkill(
    userId: number,
    details: Omit<Skill, "skillId">,
    offered: boolean,
  ): Promise<Student> {
    requireMockMode();
    const state = readDemoState();
    const student = state.students.find((entry) => entry.userId === userId);
    if (!student) throw new Error("Student not found.");
    const skill = { ...details, skillId: nextSkillId(state) };
    if (offered) student.offeredSkills.push(skill);
    else student.wantedSkills.push(skill);
    saveDemoState(state);
    return withRatings(student, state.ratings);
  },

  async findMentors(
    skillName: string,
    learnerId: number,
  ): Promise<MentorMatch[]> {
    requireMockMode();
    const state = readDemoState();
    if (!state.students.some((student) => student.userId === learnerId)) {
      throw new Error("Student not found.");
    }

    return state.students
      .filter((student) => student.userId !== learnerId)
      .flatMap((mentor) =>
        mentor.offeredSkills
          .filter(
            (skill) =>
              skill.skillName.toLowerCase() === skillName.toLowerCase() &&
              skill.level !== "NONE",
          )
          .map((skill) => ({
            mentor: withRatings(mentor, state.ratings),
            skill,
            matchScore: Math.min(
              100,
              averageSkillLevel(skill.level) * 15 +
                averageRating(mentor, state.ratings) * 8,
            ),
          })),
      )
      .sort((left, right) => right.matchScore - left.matchScore);
  },

  async getSessions(): Promise<Session[]> {
    requireMockMode();
    return readDemoState().sessions;
  },

  async scheduleSession(
    mentorId: number,
    learnerId: number,
    skillName: string,
    date: string,
    time: string,
  ): Promise<Session> {
    requireMockMode();
    const state = readDemoState();
    const mentor = state.students.find((student) => student.userId === mentorId);
    const learner = state.students.find((student) => student.userId === learnerId);
    if (!mentor || !learner) throw new Error("Student not found.");
    if (mentorId === learnerId) {
      throw new Error("A student cannot mentor themselves.");
    }
    const skill = mentor.offeredSkills.find(
      (entry) =>
        entry.skillName.toLowerCase() === skillName.toLowerCase() &&
        entry.level !== "NONE",
    );
    if (!skill) throw new Error("Mentor does not offer this skill.");

    const session: Session = {
      sessionId:
        Math.max(0, ...state.sessions.map((entry) => entry.sessionId)) + 1,
      mentor: withRatings(mentor, state.ratings),
      learner: withRatings(learner, state.ratings),
      skill,
      date,
      time,
      status: "SCHEDULED",
    };
    state.sessions.push(session);
    saveDemoState(state);
    return session;
  },

  async completeSession(sessionId: number): Promise<void> {
    requireMockMode();
    const state = readDemoState();
    const session = state.sessions.find((entry) => entry.sessionId === sessionId);
    if (!session) throw new Error("Session not found.");
    if (session.status !== "SCHEDULED") {
      throw new Error("Only scheduled sessions can be completed.");
    }
    session.status = "COMPLETED";
    saveDemoState(state);
  },

  async rateMentor(
    sessionId: number,
    score: number,
    feedback: string,
  ): Promise<Rating> {
    requireMockMode();
    const state = readDemoState();
    const session = state.sessions.find((entry) => entry.sessionId === sessionId);
    if (!session) throw new Error("Session not found.");
    if (session.status !== "COMPLETED") {
      throw new Error("Complete the session before rating.");
    }
    if (!Number.isInteger(score) || score < 1 || score > 5) {
      throw new Error("Rating must be between 1 and 5.");
    }
    const rating: Rating = {
      ratingId: Math.max(0, ...state.ratings.map((entry) => entry.ratingId)) + 1,
      fromStudent: session.learner,
      toStudent: session.mentor,
      score,
      feedback,
    };
    state.ratings.push(rating);
    saveDemoState(state);
    return rating;
  },

  async getRatings(): Promise<Rating[]> {
    requireMockMode();
    return readDemoState().ratings;
  },

  async resetDemoData(): Promise<void> {
    requireMockMode();
    window.localStorage.removeItem(DATA_KEY);
    window.localStorage.removeItem(ACTIVE_STUDENT_KEY);
  },
};

export const activeStudentStorageKey = ACTIVE_STUDENT_KEY;
