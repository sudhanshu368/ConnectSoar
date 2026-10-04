-- ==============================================================================
-- ConnectSoar V5 Migration: Remove Meeting Recording Feature
-- Target Database: PostgreSQL / Supabase
-- ==============================================================================

-- 1. Drop recording-related columns from meetings table
ALTER TABLE meetings DROP COLUMN IF EXISTS is_recording CASCADE;
ALTER TABLE meetings DROP COLUMN IF EXISTS recording_url CASCADE;
ALTER TABLE meetings DROP COLUMN IF EXISTS recording_started_at CASCADE;

-- 2. Drop index if exists
DROP INDEX IF EXISTS idx_meetings_is_recording;
