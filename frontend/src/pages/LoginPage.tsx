import { ArrowRight, LoaderCircle, Sprout } from "lucide-react";
import { useState, type FormEvent } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { api } from "../api/client";
import { useApp } from "../context/AppContext";

interface LoginLocationState {
  registrationComplete?: boolean;
}

export function LoginPage() {
  const { setAuthenticatedUser, refresh, error: connectionError } = useApp();
  const navigate = useNavigate();
  const location = useLocation();
  const state = location.state as LoginLocationState | null;
  const [message, setMessage] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function login(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage("");
    const values = new FormData(event.currentTarget);
    const email = String(values.get("email") ?? "").trim();
    const password = String(values.get("password") ?? "");
    if (!email || !password) {
      setMessage("Enter your email address and password.");
      return;
    }

    setSubmitting(true);
    try {
      const student = await api.login(email, password);
      setAuthenticatedUser(student);
      await refresh();
      navigate("/dashboard", { replace: true });
    } catch (cause) {
      setMessage(
        cause instanceof Error ? cause.message : "Sign-in could not be completed.",
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="register-page page-gutter">
      <div className="register-card">
        <div className="register-decoration">
          <span><Sprout size={26} /></span>
          <p>Share what you know.<br /><em>Find what you need.</em></p>
          <small>Welcome back to your<br />SkillBarter community.</small>
          <div className="register-sun" />
        </div>
        <div className="register-form-wrap">
          <p className="eyebrow">Welcome back</p>
          <h1>Sign in.</h1>
          <p className="section-description">
            Use the account you registered with.
          </p>
          {state?.registrationComplete && (
            <p className="success-message" role="status">
              Account created. Sign in with your new credentials.
            </p>
          )}
          {connectionError && (
            <p className="form-hint form-hint--error" role="alert">
              {connectionError}
            </p>
          )}
          <form className="stacked-form" onSubmit={(event) => void login(event)}>
            <label>
              Email address
              <input name="email" type="email" autoComplete="email" required />
            </label>
            <label>
              Password
              <input
                name="password"
                type="password"
                autoComplete="current-password"
                required
              />
            </label>
            <button
              className="button button--primary button--full"
              type="submit"
              disabled={submitting}
            >
              {submitting ? <><LoaderCircle className="spin" size={16} /> Signing in…</> : <>Sign in <ArrowRight size={16} /></>}
            </button>
          </form>
          {message && <p className="form-hint form-hint--error" role="alert">{message}</p>}
          <p className="form-footnote">
            New to SkillBarter? <Link to="/register">Create an account</Link>
          </p>
        </div>
      </div>
    </section>
  );
}
