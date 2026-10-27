import { AnimatePresence, motion } from "framer-motion";
import { ArrowUpRight, LogOut, Moon, Sun } from "lucide-react";
import { useEffect, useState, type ReactNode } from "react";
import { Link, NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { useApp } from "../context/AppContext";

const navigation = [
  { to: "/dashboard", label: "My space" },
  { to: "/skills", label: "Explore skills" },
];

export function AppLayout() {
  const { activeStudent, error, clearError, logout } = useApp();
  const location = useLocation();
  const navigate = useNavigate();
  const [dark, setDark] = useState(() => window.localStorage.getItem("skillbarter-theme") === "dark");
  const [mobileOpen, setMobileOpen] = useState(false);

  useEffect(() => {
    document.documentElement.dataset.theme = dark ? "dark" : "light";
    window.localStorage.setItem("skillbarter-theme", dark ? "dark" : "light");
  }, [dark]);

  async function signOut() {
    if (await logout()) {
      navigate("/login", { replace: true });
    }
  }

  return (
    <div className="app-shell">
      <header className={mobileOpen ? "site-header mobile-menu-open" : "site-header"}>
        <Link className="brand" to="/" aria-label="SkillBarter home">
          <span className="brand-mark">s<span>b</span></span>
          <span>skillbarter<span className="brand-period">.</span></span>
        </Link>
        <nav className="main-nav" aria-label="Main navigation">
          {navigation.map((item) => (
            <NavLink key={item.to} to={item.to} className={({ isActive }) => isActive ? "nav-link nav-link--active" : "nav-link"}>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="header-actions">
          {activeStudent && <span className="header-user">{activeStudent.name}</span>}
          <button className="icon-button theme-toggle" onClick={() => setDark((value) => !value)} aria-label={`Switch to ${dark ? "light" : "dark"} mode`} title="Toggle theme">
            {dark ? <Sun size={18} /> : <Moon size={18} />}
          </button>
          <Link className="header-profile" to="/profile">
            <span>Profile</span><ArrowUpRight size={15} />
          </Link>
          <button className="header-profile sign-out-button" onClick={() => void signOut()}>
            <span>Sign out</span><LogOut size={15} />
          </button>
          <button className="mobile-nav-toggle" aria-label="Toggle navigation menu" aria-controls="mobile-navigation" aria-expanded={mobileOpen} onClick={() => setMobileOpen((open) => !open)}>☰</button>
        </div>
        <div className="mobile-nav" id="mobile-navigation">
          {navigation.map((item) => <NavLink key={item.to} to={item.to} onClick={() => setMobileOpen(false)}>{item.label}</NavLink>)}
          <Link to="/profile" onClick={() => setMobileOpen(false)}>Profile</Link>
          <button className="mobile-reset" onClick={() => { setMobileOpen(false); void signOut(); }}><LogOut size={15} /> Sign out</button>
        </div>
      </header>
      {error && (
        <div className="error-banner" role="alert">
          <span>{error}</span><button onClick={clearError} aria-label="Dismiss message">×</button>
        </div>
      )}
      <AnimatePresence mode="wait">
        <motion.main key={location.pathname} className="page-main" initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -6 }} transition={{ duration: 0.24 }}>
          <Outlet />
        </motion.main>
      </AnimatePresence>
      <Footer />
    </div>
  );
}

export function SectionHeading({
  eyebrow,
  title,
  description,
  action,
}: {
  eyebrow: string;
  title: string;
  description?: string;
  action?: ReactNode;
}) {
  return (
    <div className="section-heading">
      <div><p className="eyebrow">{eyebrow}</p><h1>{title}</h1>{description && <p className="section-description">{description}</p>}</div>
      {action && <div className="section-heading__action">{action}</div>}
    </div>
  );
}

function Footer() {
  return <footer className="site-footer"><Link className="brand brand--footer" to="/"><span className="brand-mark">s<span>b</span></span><span>skillbarter<span className="brand-period">.</span></span></Link><span>Good things happen when we teach each other.</span><a href="#top">Back to top ↑</a></footer>;
}
