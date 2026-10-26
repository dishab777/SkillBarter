import { motion } from "framer-motion";
import { ArrowDown, ArrowRight, ArrowUpRight, Sparkles, Sprout, UsersRound } from "lucide-react";
import { lazy, Suspense } from "react";
import { Link } from "react-router-dom";
import { useApp } from "../context/AppContext";
import { SkillCard } from "../components/SkillCard";
import { useParallax } from "../hooks/useParallax";

const HeroScene = lazy(() => import("../components/HeroScene"));

export function LandingPage() {
  const { students } = useApp();
  const featured = students
    .flatMap((mentor) => mentor.offeredSkills.map((skill) => ({ mentor, skill })))
    .slice(0, 3);
  const parallaxOffset = useParallax();

  return (
    <>
      <section className="hero-section page-gutter">
        <div className="hero-copy">
          <motion.p className="eyebrow eyebrow--pill" initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.05 }}>
            <Sparkles size={14} /> A little give. A lot of grow.
          </motion.p>
          <motion.h1 initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1, duration: 0.55 }}>
            Your next skill is<br /><span>closer than you think.</span>
          </motion.h1>
          <motion.p className="hero-description" initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.3 }}>
            Teach the thing you love. Learn the thing you don&apos;t know yet. Good swaps make everyone a little more capable.
          </motion.p>
          <motion.div className="hero-actions" initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.42 }}>
            <Link className="button button--primary" to="/skills">Find your next skill <ArrowRight size={17} /></Link>
            <Link className="button button--text" to="/register">Join the community <ArrowUpRight size={16} /></Link>
          </motion.div>
          <div className="hero-proof"><div className="avatar-stack">{students.slice(0, 4).map((student) => <span key={student.userId} title={student.name}>{student.name.split(" ").map((word) => word[0]).join("")}</span>)}</div><p><strong>{students.length || 8} curious people</strong><br />already swapping skills</p></div>
        </div>
        <div className="hero-art" style={{ transform: `translateY(${parallaxOffset}px)` }}>
          <div className="hero-art__orb hero-art__orb--one" />
          <div className="hero-art__orb hero-art__orb--two" />
          <Suspense fallback={<span className="scene-loading" aria-hidden="true" />}>
            <HeroScene />
          </Suspense>
          <span className="floating-note floating-note--top"><Sprout size={16} /> grow together</span>
          <span className="floating-note floating-note--bottom">✳ &nbsp; exchange, not expense</span>
          <span className="hero-art__caption">different strengths.<br />one good connection.</span>
        </div>
        <a className="hero-scroll" href="#how-it-works"><ArrowDown size={15} /> Scroll to explore</a>
      </section>

      <section className="value-strip page-gutter" id="how-it-works">
        <p className="eyebrow">A better kind of trade</p>
        <div className="value-grid">
          <article><span className="value-icon value-icon--green"><Sprout size={19} /></span><p className="value-number">01</p><h3>Bring what you know</h3><p>List a skill you can share, from the first brushstroke to the final line of code.</p></article>
          <article><span className="value-icon value-icon--peach"><UsersRound size={19} /></span><p className="value-number">02</p><h3>Meet your match</h3><p>Find people whose know-how fits what you&apos;re curious to learn next.</p></article>
          <article><span className="value-icon value-icon--blue"><ArrowRight size={19} /></span><p className="value-number">03</p><h3>Make a good swap</h3><p>Set up a session and trade time, ideas, and a little bit of momentum.</p></article>
        </div>
      </section>

      <section className="featured-section page-gutter">
        <div className="section-heading"><div><p className="eyebrow">Good things to learn</p><h2>Curiosity, meet community.</h2><p className="section-description">A few lovely places to start. Your next favourite teacher is in here somewhere.</p></div><Link className="text-link" to="/skills">Explore all skills <ArrowUpRight size={16} /></Link></div>
        <div className="skill-grid">{featured.map(({ mentor, skill }) => <SkillCard key={`${mentor.userId}-${skill.skillId}`} mentor={mentor} skill={skill} />)}</div>
      </section>

      <section className="closing-cta page-gutter">
        <div><p className="eyebrow eyebrow--light">It starts with a hello</p><h2>What could you learn<br /><em>from each other?</em></h2><p>No fees, no big commitments. Just people sharing what they know.</p><Link className="button button--light" to="/register">Find your first swap <ArrowRight size={16} /></Link></div>
        <div className="closing-cta__mark" aria-hidden="true">s<span>b</span></div>
      </section>
    </>
  );
}
