-- Existing student rows remain valid with a NULL password_hash.
-- New console registrations always provide a BCrypt hash.

ALTER TABLE public.student
    ADD COLUMN IF NOT EXISTS password_hash TEXT;
