-- ==============================================================================
-- Migration V3: Add Employee Address and Aadhar Number
-- Target Database: PostgreSQL / Supabase
-- ==============================================================================

-- 1. Add columns to profiles table if they don't already exist
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS address TEXT;
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS adhar_number VARCHAR(20);

-- 2. Create index on adhar_number for quick lookup
CREATE INDEX IF NOT EXISTS idx_profiles_adhar_number ON profiles(adhar_number);
