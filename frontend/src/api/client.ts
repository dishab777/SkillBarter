import type {
  MentorMatch,
  RegistrationDetails,
  Skill,
  Student,
} from "../types/models";

const configuredApiBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();
export const apiBaseUrl =
  configuredApiBaseUrl || (import.meta.env.DEV ? "http://localhost:8080" : "");

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

async function requestJson<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  if (!apiBaseUrl) {
    throw new Error(
      "The SkillBarter API URL is not configured. Set VITE_API_BASE_URL in your Vercel project settings and redeploy.",
    );
  }

  let response: Response;
  try {
    response = await fetch(
      `${apiBaseUrl.replace(/\/$/, "")}${path}`,
      {
        ...options,
        credentials: "include",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
          ...options.headers,
        },
      },
    );
  } catch {
    throw new Error(
      `Could not connect to SkillBarter at ${apiBaseUrl}. Check that the Java API is running.`,
    );
  }

  const contentType = response.headers.get("content-type") ?? "";
  const payload = contentType.includes("application/json")
    ? await response.json()
    : null;

  if (!response.ok) {
    const message =
      typeof payload === "object" && payload !== null && "message" in payload
        ? String((payload as { message?: string }).message)
        : "The request could not be completed.";
    throw new ApiError(message, response.status);
  }

  if (payload && typeof payload === "object" && "data" in payload) {
    return (payload as { data: T }).data;
  }
  return payload as T;
}

export const api = {
  getCurrentUser(): Promise<Student> {
    return requestJson<Student>("/api/auth/me");
  },

  login(email: string, password: string): Promise<Student> {
    return requestJson<Student>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
  },

  logout(): Promise<void> {
    return requestJson<void>("/api/auth/logout", { method: "POST" });
  },

  registerStudent(
    details: RegistrationDetails & { password: string },
  ): Promise<Student> {
    return requestJson<Student>("/api/auth/register", {
      method: "POST",
      body: JSON.stringify({
        studentId: details.userId,
        name: details.name,
        email: details.email,
        password: details.password,
      }),
    });
  },

  getStudents(): Promise<Student[]> {
    return requestJson<Student[]>("/api/students");
  },

  updateStudent(
    userId: number,
    details: Pick<Student, "name" | "email">,
  ): Promise<Student> {
    return requestJson<Student>(`/api/students/${userId}`, {
      method: "PUT",
      body: JSON.stringify(details),
    });
  },

  addStudentSkill(
    userId: number,
    details: Omit<Skill, "skillId">,
    offered: boolean,
  ): Promise<Student> {
    return requestJson<Student>(`/api/students/${userId}/skills`, {
      method: "POST",
      body: JSON.stringify({ ...details, offered }),
    });
  },

  findMentors(skillName: string): Promise<MentorMatch[]> {
    return requestJson<MentorMatch[]>(
      `/api/mentors?skillName=${encodeURIComponent(skillName)}`,
    );
  },
};
