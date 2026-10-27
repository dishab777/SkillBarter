import { ArrowLeft, ArrowRight, LoaderCircle, Sprout } from "lucide-react";
import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../api/client";

export function RegisterPage() {
  const navigate = useNavigate();
  const [message, setMessage] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function register(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage("");
    const form = event.currentTarget;
    const values = new FormData(form);
    const userId = Number(values.get("userId"));
    const name = String(values.get("name") ?? "").trim();
    const email = String(values.get("email") ?? "").trim();
    const password = String(values.get("password") ?? "");
    const confirmPassword = String(values.get("confirmPassword") ?? "");

    if (!Number.isSafeInteger(userId) || userId < 1 || userId > 2147483647) {
      setMessage("Enter a positive whole number for your student ID.");
      return;
    }
    if (!name || !email) {
      setMessage("Complete all required fields.");
      return;
    }
    if (password.trim().length < 8) {
      setMessage("Password must contain at least 8 non-whitespace characters.");
      return;
    }
    if (password.length > 128) {
      setMessage("Password must be 128 characters or fewer.");
      return;
    }
    if (password !== confirmPassword) {
      setMessage("Passwords do not match.");
      return;
    }

    setSubmitting(true);
    try {
      await api.registerStudent({ userId, name, email, password });
      navigate("/login", {
        replace: true,
        state: { registrationComplete: true },
      });
    } catch (cause) {
      setMessage(
        cause instanceof Error
          ? cause.message
          : "Registration could not be completed.",
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="register-page page-gutter">
      <Link className="back-link" to="/login">
        <ArrowLeft size={16} /> Back to sign in
      </Link>
      <div className="register-card">
        <div className="register-decoration">
          <span><Sprout size={26} /></span>
          <p>A good exchange<br /><em>starts with you.</em></p>
          <small>Share what you love.<br />Stay curious about the rest.</small>
          <div className="register-sun" />
        </div>
        <div className="register-form-wrap">
          <p className="eyebrow">Join SkillBarter</p>
          <h1>Create your account.</h1>
          <p className="section-description">
            Your account is saved to SkillBarter's PostgreSQL database.
          </p>
          <form className="stacked-form" onSubmit={(event) => void register(event)}>
            <label>
              Student ID
              <input name="userId" type="number" min="1" max="2147483647" step="1" required />
            </label>
            <label>
              Full name
              <input name="name" type="text" autoComplete="name" maxLength={120} required />
            </label>
            <label>
              Email address
              <input name="email" type="email" autoComplete="email" required />
            </label>
            <label>
              Password
              <input
                name="password"
                type="password"
                autoComplete="new-password"
                minLength={8}
                maxLength={128}
                required
              />
            </label>
            <label>
              Confirm password
              <input
                name="confirmPassword"
                type="password"
                autoComplete="new-password"
                minLength={8}
                maxLength={128}
                required
              />
            </label>
            <button
              className="button button--primary button--full"
              type="submit"
              disabled={submitting}
            >
              {submitting ? <><LoaderCircle className="spin" size={16} /> Creating account…</> : <>Create account <ArrowRight size={16} /></>}
            </button>
          </form>
          {message && <p className="form-hint form-hint--error" role="alert">{message}</p>}
          <p className="form-footnote">
            Passwords are hashed by the Java backend. Never share your password.
          </p>
          <p className="form-footnote">
            Already registered? <Link to="/login">Sign in</Link>
          </p>
        </div>
      </div>
    </section>
  );
}
