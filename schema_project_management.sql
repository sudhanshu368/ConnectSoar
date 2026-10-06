-- ==============================================================================
-- ConnectSoar Project Management System Database Schema & RLS Policies
-- Target Database: PostgreSQL / Supabase
-- ==============================================================================

-- 1. Ensure UUID Extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. Projects Table
CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('PLANNING', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CANCELLED')),
    start_date DATE DEFAULT CURRENT_DATE,
    deadline DATE NOT NULL,
    created_by_user_id UUID REFERENCES profiles(id) ON DELETE SET NULL,
    last_activity_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. Project Members Table
CREATE TABLE IF NOT EXISTS project_members (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    member_role VARCHAR(50) NOT NULL DEFAULT 'FULL_STACK' CHECK (member_role IN ('MANAGER', 'FRONTEND', 'BACKEND', 'FULL_STACK', 'MEMBER')),
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(project_id, user_id)
);

-- 4. Project Modules Table
CREATE TABLE IF NOT EXISTS project_modules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    module_type VARCHAR(50) NOT NULL DEFAULT 'FULL_STACK' CHECK (module_type IN ('FRONTEND', 'BACKEND', 'FULL_STACK', 'GENERAL')),
    status VARCHAR(50) NOT NULL DEFAULT 'TODO' CHECK (status IN ('TODO', 'IN_PROGRESS', 'REVIEW', 'COMPLETED', 'BLOCKED')),
    progress_percentage INTEGER NOT NULL DEFAULT 0 CHECK (progress_percentage >= 0 AND progress_percentage <= 100),
    assigned_to_user_id UUID REFERENCES profiles(id) ON DELETE SET NULL,
    assigned_to_name VARCHAR(255),
    created_by_user_id UUID REFERENCES profiles(id) ON DELETE SET NULL,
    deadline DATE,
    last_updated_by_user_id UUID REFERENCES profiles(id) ON DELETE SET NULL,
    last_updated_by_name VARCHAR(255),
    last_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 5. Project Activity Logs (Audit Trail)
CREATE TABLE IF NOT EXISTS project_activity_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    module_id UUID REFERENCES project_modules(id) ON DELETE SET NULL,
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    user_name VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    details TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 6. Indexes for High Performance Querying
CREATE INDEX IF NOT EXISTS idx_projects_status ON projects(status);
CREATE INDEX IF NOT EXISTS idx_projects_deadline ON projects(deadline);
CREATE INDEX IF NOT EXISTS idx_projects_last_activity ON projects(last_activity_at);
CREATE INDEX IF NOT EXISTS idx_project_members_proj_user ON project_members(project_id, user_id);
CREATE INDEX IF NOT EXISTS idx_project_members_user ON project_members(user_id);
CREATE INDEX IF NOT EXISTS idx_project_modules_proj_id ON project_modules(project_id);
CREATE INDEX IF NOT EXISTS idx_project_modules_assigned_user ON project_modules(assigned_to_user_id);
CREATE INDEX IF NOT EXISTS idx_project_modules_status ON project_modules(status);
CREATE INDEX IF NOT EXISTS idx_project_activity_logs_proj ON project_activity_logs(project_id);
CREATE INDEX IF NOT EXISTS idx_project_activity_logs_module ON project_activity_logs(module_id);
CREATE INDEX IF NOT EXISTS idx_project_activity_logs_created ON project_activity_logs(created_at DESC);

-- 7. Automated Trigger to update project last_activity_at on module changes
CREATE OR REPLACE FUNCTION trg_update_project_activity()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE projects
    SET last_activity_at = NOW(),
        updated_at = NOW()
    WHERE id = NEW.project_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_module_activity_sync ON project_modules;
CREATE TRIGGER trigger_module_activity_sync
AFTER INSERT OR UPDATE ON project_modules
FOR EACH ROW
EXECUTE FUNCTION trg_update_project_activity();

-- 8. Stored Procedure for the 3-Day Inactivity Auto-Hold Rule
-- If no module status change occurs for 3 consecutive days, project status becomes 'ON_HOLD'
CREATE OR REPLACE FUNCTION auto_hold_inactive_projects()
RETURNS INTEGER AS $$
DECLARE
    v_affected_rows INTEGER := 0;
BEGIN
    WITH updated AS (
        UPDATE projects
        SET status = 'ON_HOLD',
            updated_at = NOW()
        WHERE status = 'IN_PROGRESS'
          AND last_activity_at < (NOW() - INTERVAL '3 days')
        RETURNING id, name
    )
    INSERT INTO project_activity_logs (project_id, user_id, user_name, action, details)
    SELECT id, 
           (SELECT id FROM profiles WHERE role = 'admin' LIMIT 1),
           'System Automation',
           'STATUS_AUTO_HOLD',
           'Project automatically transitioned to ON_HOLD due to 3 consecutive days without any module status updates.'
    FROM updated;

    GET DIAGNOSTICS v_affected_rows = ROW_COUNT;
    RETURN v_affected_rows;
END;
$$ LANGUAGE plpgsql;

-- 9. Row Level Security (RLS) Setup
ALTER TABLE projects ENABLE ROW LEVEL SECURITY;
ALTER TABLE project_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE project_modules ENABLE ROW LEVEL SECURITY;
ALTER TABLE project_activity_logs ENABLE ROW LEVEL SECURITY;

-- Helper Functions for Security Policies
CREATE OR REPLACE FUNCTION is_project_manager(p_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM project_members 
        WHERE project_id = p_id 
          AND user_id = auth.uid() 
          AND member_role = 'MANAGER'
    ) OR EXISTS (
        SELECT 1 FROM projects
        WHERE id = p_id AND created_by_user_id = auth.uid()
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION is_project_member(p_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM project_members 
        WHERE project_id = p_id AND user_id = auth.uid()
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- RLS: projects
CREATE POLICY "Projects select policy" ON projects FOR SELECT USING (
    is_admin() OR is_project_member(id) OR created_by_user_id = auth.uid()
);

CREATE POLICY "Projects insert policy" ON projects FOR INSERT WITH CHECK (
    is_admin() OR EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role IN ('admin', 'manager'))
);

CREATE POLICY "Projects update policy" ON projects FOR UPDATE USING (
    is_admin() OR is_project_manager(id)
);

CREATE POLICY "Projects delete policy" ON projects FOR DELETE USING (
    is_admin()
);

-- RLS: project_members
CREATE POLICY "Members select policy" ON project_members FOR SELECT USING (
    is_admin() OR is_project_member(project_id)
);

CREATE POLICY "Members insert policy" ON project_members FOR INSERT WITH CHECK (
    is_admin() OR is_project_manager(project_id)
);

CREATE POLICY "Members update policy" ON project_members FOR UPDATE USING (
    is_admin() OR is_project_manager(project_id)
);

CREATE POLICY "Members delete policy" ON project_members FOR DELETE USING (
    is_admin() OR is_project_manager(project_id)
);

-- RLS: project_modules
CREATE POLICY "Modules select policy" ON project_modules FOR SELECT USING (
    is_admin() OR is_project_member(project_id)
);

CREATE POLICY "Modules insert policy" ON project_modules FOR INSERT WITH CHECK (
    is_admin() OR is_project_manager(project_id)
);

-- CRITICAL RULE: Employee can only update THEIR OWN assigned module!
-- Manager/Admin can update any module in their project
CREATE POLICY "Modules update policy" ON project_modules FOR UPDATE USING (
    is_admin() 
    OR is_project_manager(project_id) 
    OR (is_project_member(project_id) AND assigned_to_user_id = auth.uid())
);

CREATE POLICY "Modules delete policy" ON project_modules FOR DELETE USING (
    is_admin() OR is_project_manager(project_id)
);

-- RLS: project_activity_logs
CREATE POLICY "Activity logs select policy" ON project_activity_logs FOR SELECT USING (
    is_admin() OR is_project_member(project_id)
);

CREATE POLICY "Activity logs insert policy" ON project_activity_logs FOR INSERT WITH CHECK (
    TRUE
);
