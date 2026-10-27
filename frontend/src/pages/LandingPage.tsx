import { ArrowRight, ArrowUpRight, Sparkles, Sprout, UsersRound } from "lucide-react";
import { Link } from "react-router-dom";
import { SectionHeading } from "../components/AppLayout";

export function LandingPage() {
  return (
    <>
      <section className="hero-section page-gutter">
        <div className="hero-copy">
          <p className="eyebrow eyebrow--pill"><Sparkles size={14} /> A little give. A lot of grow.</p>
          <h1>Your next skill is<br /><span>closer than you think.</span></h1>
          <p className="hero-description">
            Teach what you know and learn something new through real student
            profiles stored in SkillBarter.
          </p>
          <div className="hero-actions">
            <Link className="button button--primary" to="/login">
              Sign in <ArrowRight size={17} />
            </Link>
            <Link className="button button--text" to="/register">
              Create an account <ArrowUpRight size={16} />
            </Link>
          </div>
        </div>
        <div className="hero-art">
          <div className="hero-art__orb hero-art__orb--one" />
          <div className="hero-art__orb hero-art__orb--two" />
          <div className="hero-art__caption">different strengths.<br />one good connection.</div>
          <span className="floating-note floating-note--top"><Sprout size={16} /> grow together</span>
          <span className="floating-note floating-note--bottom">✳ &nbsp; exchange, not expense</span>
        </div>
      </section>
      <section className="value-strip page-gutter">
        <p className="eyebrow">How it works</p>
        <div className="value-grid">
          <article><span className="value-icon value-icon--green"><Sprout size={19} /></span><p className="value-number">01</p><h3>Register an account</h3><p>Your profile is saved by the Java API to Supabase PostgreSQL.</p></article>
          <article><span className="value-icon value-icon--peach"><UsersRound size={19} /></span><p className="value-number">02</p><h3>Share your skills</h3><p>Add skills you can teach or skills you would like to learn.</p></article>
          <article><span className="value-icon value-icon--blue"><ArrowRight size={19} /></span><p className="value-number">03</p><h3>Explore real profiles</h3><p>Browse skills entered by registered SkillBarter members.</p></article>
        </div>
      </section>
      <section className="featured-section page-gutter">
        <SectionHeading
          eyebrow="Built for learning together"
          title="Curiosity meets community."
          description="Sign in to browse the profiles and skills currently stored in the database."
          action={<Link className="text-link" to="/login">Sign in <ArrowUpRight size={16} /></Link>}
        />
      </section>
    </>
  );
}
