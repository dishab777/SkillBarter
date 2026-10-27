import { SectionHeading } from "../components/AppLayout";

export function ExchangesPage() {
  return (
    <section className="page-gutter content-page">
      <SectionHeading
        eyebrow="Exchange sessions"
        title="Session scheduling is not connected yet."
        description="The inspected Supabase schema contains accounts, offered/wanted skills, and matches, but no session date/time or rating-feedback tables."
      />
      <div className="empty-state">
        <h3>No fabricated sessions are shown.</h3>
        <p>
          Add session and rating tables before enabling booking, completion, or
          rating workflows. The current page does not create in-memory records.
        </p>
      </div>
    </section>
  );
}
