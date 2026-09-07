-- ==============================================================================
-- ConnectSoar V2 Migration: Enhanced Meetings Module
-- Target Database: PostgreSQL / Supabase
-- ==============================================================================

-- 1. Create Enums for Meeting Module
DO $$ BEGIN
    CREATE TYPE meeting_type_enum AS ENUM ('INSTANT_ROOM', 'SCHEDULED_MEETING');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE meeting_status_enum AS ENUM ('SCHEDULED', 'LIVE', 'COMPLETED', 'CANCELLED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE participant_role_enum AS ENUM ('HOST', 'PARTICIPANT');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE participant_status_enum AS ENUM ('INVITED', 'ACCEPTED', 'DECLINED', 'JOINED', 'LEFT', 'REMOVED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE recurrence_type_enum AS ENUM ('NONE', 'DAILY', 'WEEKLY', 'MONTHLY');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE invitation_status_enum AS ENUM ('PENDING', 'ACCEPTED', 'DECLINED', 'CANCELLED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 2. Alter or Create Meetings Table
CREATE TABLE IF NOT EXISTS meetings_new (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    meeting_code VARCHAR(50) UNIQUE NOT NULL,
    meeting_url VARCHAR(500) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    meeting_type VARCHAR(50) NOT NULL DEFAULT 'INSTANT_ROOM' CHECK (meeting_type IN ('INSTANT_ROOM', 'SCHEDULED_MEETING')),
    host_user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    password_hash VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'LIVE', 'COMPLETED', 'CANCELLED')),
    scheduled_start_time TIMESTAMPTZ,
    scheduled_end_time TIMESTAMPTZ,
    duration_minutes INTEGER,
    timezone VARCHAR(100) DEFAULT 'UTC',
    reminder_minutes INTEGER DEFAULT 15,
    recurrence_type VARCHAR(50) NOT NULL DEFAULT 'NONE' CHECK (recurrence_type IN ('NONE', 'DAILY', 'WEEKLY', 'MONTHLY')),
    allow_participant_chat BOOLEAN NOT NULL DEFAULT TRUE,
    allow_screen_sharing BOOLEAN NOT NULL DEFAULT TRUE,
    mute_participants_on_entry BOOLEAN NOT NULL DEFAULT FALSE,
    allow_participant_video BOOLEAN NOT NULL DEFAULT TRUE,
    allow_participant_audio BOOLEAN NOT NULL DEFAULT TRUE,
    is_open_room BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    started_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ
);

-- Check if meetings table needs column upgrades or migration from legacy table
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'meetings') THEN
        -- Add missing columns if legacy meetings table exists
        ALTER TABLE meetings ADD COLUMN IF NOT EXISTS meeting_code VARCHAR(50);
        ALTER TABLE meetings ADD COLUMN IF NOT EXISTS meeting_url VARCHAR(500);
        ALTER TABLE meetings ADD COLUMN IF NOT EXISTS meeting_type VARCHAR(50) DEFAULT 'INSTANT_ROOM';
        ALTER TABLE meetings ADD COLUMN IF NOT EXISTS host_user_id UUID;
        ALTER TABLE meetings ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);
        ALTER TABLE meetings ADD COLUMN IF NOT EXISTS scheduled_start_time TIMESTAMPTZ;
        ALTER TABLE meetings ADD COLUMN IF NOT EXISTS scheduled_end_time TIMESTAMPTZ;
        ALTER TABLE meetings ADD COLUMN IF NOT EXISTS duration_minutes INTEGER;
        ALTER TABLE meetings ADD COLUMN IF NOT EXISTS timezone VARCHAR(100) DEFAULT 'UTC';
        ALTER TABLE meetings ADD COLUMN IF NOT EXISTS reminder_minutes INTEGER DEFAULT 15;
        ALTER TABLE meetings ADD COLUMN IF NOT EXISTS recurrence_type VARCHAR(50) DEFAULT 'NONE';
        ALTER TABLE meetings ADD COLUMN IF NOT EXISTS allow_participant_chat BOOLEAN DEFAULT TRUE;
        ALTER TABLE meetings ADD COLUMN IF NOT EXISTS allow_screen_sharing BOOLEAN DEFAULT TRUE;
        ALTER TABLE meetings ADD COLUMN IF NOT EXISTS mute_participants_on_entry BOOLEAN DEFAULT FALSE;
        ALTER TABLE meetings ADD COLUMN IF NOT EXISTS allow_participant_video BOOLEAN DEFAULT TRUE;
        ALTER TABLE meetings ADD COLUMN IF NOT EXISTS allow_participant_audio BOOLEAN DEFAULT TRUE;
        ALTER TABLE meetings ADD COLUMN IF NOT EXISTS is_open_room BOOLEAN DEFAULT FALSE;

        -- Update host_user_id from host_id if exists
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'meetings' AND column_name = 'host_id') THEN
            UPDATE meetings SET host_user_id = host_id WHERE host_user_id IS NULL;
        END IF;
    ELSE
        ALTER TABLE meetings_new RENAME TO meetings;
    END IF;
END $$;

DROP TABLE IF EXISTS meetings_new;

-- 3. Meeting Participants Table
CREATE TABLE IF NOT EXISTS meeting_participants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    meeting_id UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    participant_role VARCHAR(50) NOT NULL DEFAULT 'PARTICIPANT' CHECK (participant_role IN ('HOST', 'PARTICIPANT')),
    status VARCHAR(50) NOT NULL DEFAULT 'INVITED' CHECK (status IN ('INVITED', 'ACCEPTED', 'DECLINED', 'JOINED', 'LEFT', 'REMOVED')),
    invited_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    joined_at TIMESTAMPTZ,
    left_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(meeting_id, user_id)
);

-- In case meeting_participants already had older structure, add new columns
DO $$
BEGIN
    ALTER TABLE meeting_participants ADD COLUMN IF NOT EXISTS participant_role VARCHAR(50) DEFAULT 'PARTICIPANT';
    ALTER TABLE meeting_participants ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'INVITED';
    ALTER TABLE meeting_participants ADD COLUMN IF NOT EXISTS invited_at TIMESTAMPTZ DEFAULT NOW();
    ALTER TABLE meeting_participants ADD COLUMN IF NOT EXISTS joined_at TIMESTAMPTZ;
    ALTER TABLE meeting_participants ADD COLUMN IF NOT EXISTS left_at TIMESTAMPTZ;
END $$;

-- 4. Meeting Invitations Table
CREATE TABLE IF NOT EXISTS meeting_invitations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    meeting_id UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    user_id UUID REFERENCES profiles(id) ON DELETE SET NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'CANCELLED')),
    token VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(meeting_id, email)
);

-- 5. Meeting Sessions Table (Runtime state / presence)
CREATE TABLE IF NOT EXISTS meeting_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    meeting_id UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    session_id VARCHAR(255) NOT NULL,
    connection_status VARCHAR(50) NOT NULL DEFAULT 'CONNECTED',
    microphone_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    camera_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    screen_sharing BOOLEAN NOT NULL DEFAULT FALSE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    left_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 6. Meeting Messages Table (Chat persistence)
CREATE TABLE IF NOT EXISTS meeting_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    meeting_id UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    sender_user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 7. Indexes for High Performance
CREATE INDEX IF NOT EXISTS idx_meetings_code ON meetings(meeting_code);
CREATE INDEX IF NOT EXISTS idx_meetings_host_user_id ON meetings(host_user_id);
CREATE INDEX IF NOT EXISTS idx_meetings_status ON meetings(status);
CREATE INDEX IF NOT EXISTS idx_meetings_scheduled_start ON meetings(scheduled_start_time);
CREATE INDEX IF NOT EXISTS idx_meetings_started_at ON meetings(started_at DESC);
CREATE INDEX IF NOT EXISTS idx_meetings_ended_at ON meetings(ended_at DESC);

CREATE INDEX IF NOT EXISTS idx_participants_meeting_id ON meeting_participants(meeting_id);
CREATE INDEX IF NOT EXISTS idx_participants_user_id ON meeting_participants(user_id);
CREATE INDEX IF NOT EXISTS idx_participants_status ON meeting_participants(status);

CREATE INDEX IF NOT EXISTS idx_invitations_meeting_id ON meeting_invitations(meeting_id);
CREATE INDEX IF NOT EXISTS idx_invitations_user_id ON meeting_invitations(user_id);
CREATE INDEX IF NOT EXISTS idx_invitations_email ON meeting_invitations(email);

CREATE INDEX IF NOT EXISTS idx_sessions_meeting_id ON meeting_sessions(meeting_id);
CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON meeting_sessions(user_id);

CREATE INDEX IF NOT EXISTS idx_messages_meeting_id ON meeting_messages(meeting_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON meeting_messages(created_at ASC);

-- 8. Row Level Security Policies & Helper Functions
ALTER TABLE meetings ENABLE ROW LEVEL SECURITY;
ALTER TABLE meeting_participants ENABLE ROW LEVEL SECURITY;
ALTER TABLE meeting_invitations ENABLE ROW LEVEL SECURITY;
ALTER TABLE meeting_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE meeting_messages ENABLE ROW LEVEL SECURITY;

-- Helper function to check if current user is host of a meeting (SECURITY DEFINER prevents RLS recursion)
CREATE OR REPLACE FUNCTION is_meeting_host(m_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM meetings WHERE id = m_id AND host_user_id = auth.uid()
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Helper function to check if current user is participant of a meeting (SECURITY DEFINER prevents RLS recursion)
CREATE OR REPLACE FUNCTION is_meeting_participant(m_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM meeting_participants WHERE meeting_id = m_id AND user_id = auth.uid()
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Meetings RLS
DROP POLICY IF EXISTS "Meetings view policy" ON meetings;
CREATE POLICY "Meetings view policy" ON meetings FOR SELECT USING (
    host_user_id = auth.uid() 
    OR is_admin() 
    OR is_open_room = TRUE
    OR is_meeting_participant(id)
);

DROP POLICY IF EXISTS "Meetings create policy" ON meetings;
CREATE POLICY "Meetings create policy" ON meetings FOR INSERT WITH CHECK (
    auth.uid() = host_user_id OR is_admin()
);

DROP POLICY IF EXISTS "Meetings update policy" ON meetings;
CREATE POLICY "Meetings update policy" ON meetings FOR UPDATE USING (
    host_user_id = auth.uid() OR is_admin()
);

DROP POLICY IF EXISTS "Meetings delete policy" ON meetings;
CREATE POLICY "Meetings delete policy" ON meetings FOR DELETE USING (
    host_user_id = auth.uid() OR is_admin()
);

-- Participants RLS
DROP POLICY IF EXISTS "Participants view policy" ON meeting_participants;
CREATE POLICY "Participants view policy" ON meeting_participants FOR SELECT USING (
    user_id = auth.uid() 
    OR is_admin() 
    OR is_meeting_host(meeting_id)
);

DROP POLICY IF EXISTS "Participants insert policy" ON meeting_participants;
CREATE POLICY "Participants insert policy" ON meeting_participants FOR INSERT WITH CHECK (
    is_meeting_host(meeting_id) OR is_admin() OR user_id = auth.uid()
);

DROP POLICY IF EXISTS "Participants update policy" ON meeting_participants;
CREATE POLICY "Participants update policy" ON meeting_participants FOR UPDATE USING (
    is_meeting_host(meeting_id) OR is_admin() OR user_id = auth.uid()
);

DROP POLICY IF EXISTS "Participants delete policy" ON meeting_participants;
CREATE POLICY "Participants delete policy" ON meeting_participants FOR DELETE USING (
    is_meeting_host(meeting_id) OR is_admin()
);

-- Invitations RLS
DROP POLICY IF EXISTS "Invitations view policy" ON meeting_invitations;
CREATE POLICY "Invitations view policy" ON meeting_invitations FOR SELECT USING (
    user_id = auth.uid() 
    OR is_admin() 
    OR is_meeting_host(meeting_id)
);

-- Messages RLS
DROP POLICY IF EXISTS "Messages view policy" ON meeting_messages;
CREATE POLICY "Messages view policy" ON meeting_messages FOR SELECT USING (
    is_admin() 
    OR is_meeting_host(meeting_id)
    OR is_meeting_participant(meeting_id)
);
