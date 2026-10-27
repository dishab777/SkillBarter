import { BrowserRouter, Navigate, Outlet, Route, Routes } from "react-router-dom";
import { AppLayout } from "./components/AppLayout";
import { AppProvider, useApp } from "./context/AppContext";
import { DashboardPage } from "./pages/DashboardPage";
import { ExchangesPage } from "./pages/ExchangesPage";
import { ExplorePage } from "./pages/ExplorePage";
import { LandingPage } from "./pages/LandingPage";
import { LoginPage } from "./pages/LoginPage";
import { ProfilePage } from "./pages/ProfilePage";
import { RegisterPage } from "./pages/RegisterPage";
import { SkillDetailPage } from "./pages/SkillDetailPage";

function AuthenticationGate({ guestsOnly = false }: { guestsOnly?: boolean }) {
  const { activeStudent, loading } = useApp();

  if (loading) {
    return <main className="page-gutter content-page" aria-live="polite">Checking your SkillBarter session…</main>;
  }
  if (guestsOnly && activeStudent) {
    return <Navigate to="/dashboard" replace />;
  }
  if (!guestsOnly && !activeStudent) {
    return <Navigate to="/login" replace />;
  }
  return <Outlet />;
}

function NotFoundPage() {
  return (
    <section className="page-gutter content-page">
      <div className="empty-state">
        <p className="eyebrow">404 · wrong turn</p>
        <h1>This page isn't on the map.</h1>
        <p>Let's get you back to your SkillBarter dashboard.</p>
        <a className="button button--primary" href="/dashboard">Go to dashboard</a>
      </div>
    </section>
  );
}

export default function App() {
  return (
    <AppProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<AuthenticationGate guestsOnly />}>
            <Route path="/" element={<LandingPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
          </Route>
          <Route element={<AuthenticationGate />}>
            <Route element={<AppLayout />}>
              <Route index element={<Navigate to="/dashboard" replace />} />
              <Route path="dashboard" element={<DashboardPage />} />
              <Route path="skills" element={<ExplorePage />} />
              <Route path="skills/:id" element={<SkillDetailPage />} />
              <Route path="exchanges" element={<ExchangesPage />} />
              <Route path="profile" element={<ProfilePage />} />
              <Route path="*" element={<NotFoundPage />} />
            </Route>
          </Route>
        </Routes>
      </BrowserRouter>
    </AppProvider>
  );
}
