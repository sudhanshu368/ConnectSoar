-- ==============================================================================
-- ConnectSoar V4 Migration: Meeting Metadata, Lobby Controls & Recording
-- Target Database: PostgreSQL / Supabase
-- ==============================================================================

-- 1. Upgrade meetings table with Project, Agenda, Password, Lobby & Recording fields
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS project VARCHAR(255);
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS agenda TEXT;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS plain_password VARCHAR(255);
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS invited_user_ids JSONB DEFAULT '[]'::jsonb;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS is_recording BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS recording_url TEXT;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS recording_started_at TIMESTAMPTZ;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS lobby_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS pending_knock_user_ids JSONB DEFAULT '[]'::jsonb;
ALTER TABLE meetings ADD COLUMN IF NOT EXISTS admitted_user_ids JSONB DEFAULT '[]'::jsonb;

-- 2. Indexes for efficient lookup
CREATE INDEX IF NOT EXISTS idx_meetings_project ON meetings(project);
CREATE INDEX IF NOT EXISTS idx_meetings_is_recording ON meetings(is_recording);
CREATE INDEX IF NOT EXISTS idx_meetings_lobby_enabled ON meetings(lobby_enabled);
