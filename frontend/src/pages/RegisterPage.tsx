import { ArrowLeft, ArrowRight, Sprout } from "lucide-react";
import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../api/client";
import { useApp } from "../context/AppContext";

export function RegisterPage() {
  const { perform, setActiveStudentId } = useApp();
  const navigate = useNavigate();
  const [message, setMessage] = useState("");

  async function register(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const values = new FormData(event.currentTarget);
    const userId = Number(values.get("userId"));
    const name = String(values.get("name")).trim();
    const email = String(values.get("email")).trim();
    if (!Number.isInteger(userId) || userId < 1) {
      setMessage("Choose a positive whole number for your student ID.");
      return;
    }
    const created = await perform(() => api.registerStudent({ userId, name, email }));
    if (created) {
      setActiveStudentId(userId);
      navigate("/profile");
    }
  }

  return (
    <section className="register-page page-gutter">
      <Link className="back-link" to="/"><ArrowLeft size={16} /> Back home</Link>
      <div className="register-card">
        <div className="register-decoration"><span><Sprout size={26} /></span><p>A good exchange<br /><em>starts with you.</em></p><small>Share what you love.<br />Stay curious about the rest.</small><div className="register-sun" /></div>
        <div className="register-form-wrap">
          <p className="eyebrow">Pull up a chair</p><h1>Let&apos;s get to know you.</h1><p className="section-description">Create a local demo profile. Add a skill you can teach from your profile page.</p>
          <form className="stacked-form" onSubmit={(event) => void register(event)}>
            <label>Your student ID<input name="userId" type="number" min="1" placeholder="e.g. 42" required /></label>
            <label>Your name<input name="name" type="text" placeholder="How should we call you?" required /></label>
            <label>Email address<input name="email" type="email" placeholder="you@example.com" required /></label>
            <button className="button button--primary button--full" type="submit">Create my profile <ArrowRight size={16} /></button>
          </form>
          {message && <p className="form-hint form-hint--error" role="alert">{message}</p>}
          <p className="form-footnote">This demo has no password or sign-in system; your profile stays in this browser.</p>
        </div>
      </div>
    </section>
  );
}
