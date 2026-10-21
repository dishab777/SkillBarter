-- ============================================================================
-- SkillBarter — Peer Skill Exchange Platform Database Schema
-- Target: PostgreSQL / Supabase
-- ============================================================================
-- This script creates the 3 core tables for SkillBarter:
--   1. public.users
--   2. public.user_skills
--   3. public.matches
--
-- Safe for fresh databases. Uses IF NOT EXISTS to prevent destructive overwrites.
-- ============================================================================

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS public.users (
    user_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    department VARCHAR(100) NOT NULL,
    semester INTEGER NOT NULL CHECK (semester >= 1 AND semester <= 12),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. USER_SKILLS TABLE
CREATE TABLE IF NOT EXISTS public.user_skills (
    skill_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES public.users(user_id) ON DELETE CASCADE,
    skill_name VARCHAR(100) NOT NULL,
    skill_type VARCHAR(10) NOT NULL CHECK (skill_type IN ('TEACH', 'LEARN')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. MATCHES TABLE
CREATE TABLE IF NOT EXISTS public.matches (
    match_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_one_id INTEGER NOT NULL REFERENCES public.users(user_id) ON DELETE CASCADE,
    student_two_id INTEGER NOT NULL REFERENCES public.users(user_id) ON DELETE CASCADE,
    skill_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'COMPLETED', 'REJECTED')),
    rating INTEGER NOT NULL DEFAULT 0 CHECK (rating >= 0 AND rating <= 5),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_different_students CHECK (student_one_id <> student_two_id)
);

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================
CREATE INDEX IF NOT EXISTS idx_user_skills_user_id ON public.user_skills(user_id);
CREATE INDEX IF NOT EXISTS idx_user_skills_search ON public.user_skills(LOWER(skill_name), skill_type);
CREATE INDEX IF NOT EXISTS idx_matches_student_one ON public.matches(student_one_id);
CREATE INDEX IF NOT EXISTS idx_matches_student_two ON public.matches(student_two_id);
CREATE INDEX IF NOT EXISTS idx_matches_status ON public.matches(status);

-- ============================================================================
-- VERIFICATION QUERIES
-- Run these queries after executing the schema above to verify table creation.
-- ============================================================================
-- Check table existence:
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
  AND table_name IN ('users', 'user_skills', 'matches')
ORDER BY table_name;

-- Check constraints:
SELECT conname, contype, conrelid::regclass AS table_from
FROM pg_constraint
WHERE conrelid::regclass::text IN ('public.users', 'public.user_skills', 'public.matches')
ORDER BY table_from, conname;
