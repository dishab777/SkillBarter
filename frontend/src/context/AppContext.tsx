import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { ApiError, api } from "../api/client";
import type { Student } from "../types/models";

interface AppContextValue {
  students: Student[];
  activeStudent: Student | null;
  loading: boolean;
  error: string;
  refresh: () => Promise<void>;
  perform: (action: () => Promise<unknown>) => Promise<boolean>;
  setAuthenticatedUser: (student: Student) => void;
  logout: () => Promise<boolean>;
  clearError: () => void;
}

const AppContext = createContext<AppContextValue | undefined>(undefined);

export function AppProvider({ children }: { children: ReactNode }) {
  const [students, setStudents] = useState<Student[]>([]);
  const [activeStudent, setActiveStudent] = useState<Student | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const refresh = useCallback(async () => {
    setLoading(true);
    try {
      const currentUser = await api.getCurrentUser();
      const allStudents = await api.getStudents();
      setActiveStudent(currentUser);
      setStudents(allStudents);
      setError("");
    } catch (cause) {
      setStudents([]);
      if (cause instanceof ApiError && cause.status === 401) {
        setActiveStudent(null);
        setError("");
      } else {
        setActiveStudent(null);
        setError(
          cause instanceof Error
            ? cause.message
            : "Unable to connect to the SkillBarter API.",
        );
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const perform = useCallback(
    async (action: () => Promise<unknown>) => {
      try {
        await action();
        await refresh();
        return true;
      } catch (cause) {
        setError(
          cause instanceof Error
            ? cause.message
            : "That action could not be completed.",
        );
        if (cause instanceof ApiError && cause.status === 401) {
          setActiveStudent(null);
          setStudents([]);
        }
        return false;
      }
    },
    [refresh],
  );

  const setAuthenticatedUser = useCallback((student: Student) => {
    setActiveStudent(student);
    setError("");
  }, []);

  const logout = useCallback(async () => {
    try {
      await api.logout();
      setActiveStudent(null);
      setStudents([]);
      setError("");
      return true;
    } catch (cause) {
      setError(
        cause instanceof Error ? cause.message : "Unable to sign out.",
      );
      return false;
    }
  }, []);

  const value = useMemo(
    () => ({
      students,
      activeStudent,
      loading,
      error,
      refresh,
      perform,
      setAuthenticatedUser,
      logout,
      clearError: () => setError(""),
    }),
    [
      activeStudent,
      error,
      loading,
      logout,
      perform,
      refresh,
      setAuthenticatedUser,
      students,
    ],
  );

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useApp() {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error("useApp must be used inside AppProvider.");
  }
  return context;
}
