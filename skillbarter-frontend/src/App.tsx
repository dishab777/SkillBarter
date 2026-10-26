import { BrowserRouter, Route, Routes } from "react-router-dom";
import { AppLayout } from "./components/AppLayout";
import { AppProvider } from "./context/AppContext";
import { DashboardPage } from "./pages/DashboardPage";
import { ExchangesPage } from "./pages/ExchangesPage";
import { ExplorePage } from "./pages/ExplorePage";
import { LandingPage } from "./pages/LandingPage";
import { ProfilePage } from "./pages/ProfilePage";
import { RegisterPage } from "./pages/RegisterPage";
import { SkillDetailPage } from "./pages/SkillDetailPage";

function NotFoundPage() {
  return <section className="page-gutter content-page"><div className="empty-state"><p className="eyebrow">404 · wrong turn</p><h1>This page isn't on the map.</h1><p>Let's get you back to the good stuff.</p><a className="button button--primary" href="/">Go home</a></div></section>;
}

export default function App() {
  return (
    <AppProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<AppLayout />}>
            <Route index element={<LandingPage />} />
            <Route path="dashboard" element={<DashboardPage />} />
            <Route path="skills" element={<ExplorePage />} />
            <Route path="skills/:id" element={<SkillDetailPage />} />
            <Route path="exchanges" element={<ExchangesPage />} />
            <Route path="profile" element={<ProfilePage />} />
            <Route path="register" element={<RegisterPage />} />
            <Route path="*" element={<NotFoundPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AppProvider>
  );
}
