import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { activeStudentStorageKey, api } from "../api/client";
import type { Rating, Session, Student } from "../types/models";

interface AppContextValue {
  students: Student[];
  sessions: Session[];
  ratings: Rating[];
  activeStudentId: number;
  activeStudent: Student | undefined;
  loading: boolean;
  error: string;
  setActiveStudentId: (studentId: number) => void;
  refresh: () => Promise<void>;
  perform: (action: () => Promise<unknown>) => Promise<boolean>;
  clearError: () => void;
}

const AppContext = createContext<AppContextValue | undefined>(undefined);

export function AppProvider({ children }: { children: ReactNode }) {
  const [students, setStudents] = useState<Student[]>([]);
  const [sessions, setSessions] = useState<Session[]>([]);
  const [ratings, setRatings] = useState<Rating[]>([]);
  const [activeStudentId, setActiveStudentIdState] = useState(() => {
    const saved = window.localStorage.getItem(activeStudentStorageKey);
    const parsed = saved ? Number(saved) : 1;
    return Number.isInteger(parsed) && parsed > 0 ? parsed : 1;
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const refresh = useCallback(async () => {
    setLoading(true);
    try {
      const [nextStudents, nextSessions, nextRatings] = await Promise.all([
        api.getStudents(),
        api.getSessions(),
        api.getRatings(),
      ]);
      setStudents(nextStudents);
      setSessions(nextSessions);
      setRatings(nextRatings);
      setError("");
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Unable to load SkillBarter data.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const setActiveStudentId = useCallback((studentId: number) => {
    setActiveStudentIdState(studentId);
    window.localStorage.setItem(activeStudentStorageKey, String(studentId));
  }, []);

  const perform = useCallback(
    async (action: () => Promise<unknown>) => {
      try {
        await action();
        await refresh();
        return true;
      } catch (cause) {
        setError(cause instanceof Error ? cause.message : "That action could not be completed.");
        return false;
      }
    },
    [refresh],
  );

  const value = useMemo(
    () => ({
      students,
      sessions,
      ratings,
      activeStudentId,
      activeStudent: students.find((student) => student.userId === activeStudentId),
      loading,
      error,
      setActiveStudentId,
      refresh,
      perform,
      clearError: () => setError(""),
    }),
    [
      activeStudentId,
      error,
      loading,
      perform,
      ratings,
      refresh,
      sessions,
      setActiveStudentId,
      students,
    ],
  );

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useApp() {
  const context = useContext(AppContext);
  if (!context) throw new Error("useApp must be used inside AppProvider.");
  return context;
}
