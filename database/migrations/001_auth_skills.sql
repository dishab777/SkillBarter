-- Inspected schema already includes public.users.password_hash, unique
-- emails, public.student, and public.user_skills. This migration adds
-- case-insensitive email uniqueness without dropping tables or changing
-- existing records.

BEGIN;

CREATE UNIQUE INDEX IF NOT EXISTS users_email_lower_uq
    ON public.users (lower(email));

CREATE UNIQUE INDEX IF NOT EXISTS student_email_lower_uq
    ON public.student (lower(email));

COMMIT;
