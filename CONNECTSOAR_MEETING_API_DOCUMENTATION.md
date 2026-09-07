# ConnectSoar - Meeting Module Backend API & Integration Documentation

## Overview

The ConnectSoar Meeting Module provides complete backend services for instant rooms, scheduled meetings, participant management, room permissions, access control, and WebRTC signaling with persistent chat history.

All endpoints support both `/api/meetings` and `/api/v1/meetings` paths to ensure seamless compatibility with the Flutter/mobile frontend and web clients.

---

## 1. Database Schema

### Target Database: PostgreSQL / Supabase
The migration script is located at: `src/main/resources/db/migration/V2__meeting_module_enhancements.sql`

```sql
-- Enums
CREATE TYPE meeting_type_enum AS ENUM ('INSTANT_ROOM', 'SCHEDULED_MEETING');
CREATE TYPE meeting_status_enum AS ENUM ('SCHEDULED', 'LIVE', 'COMPLETED', 'CANCELLED');
CREATE TYPE participant_role_enum AS ENUM ('HOST', 'PARTICIPANT');
CREATE TYPE participant_status_enum AS ENUM ('INVITED', 'ACCEPTED', 'DECLINED', 'JOINED', 'LEFT', 'REMOVED');
CREATE TYPE recurrence_type_enum AS ENUM ('NONE', 'DAILY', 'WEEKLY', 'MONTHLY');
CREATE TYPE invitation_status_enum AS ENUM ('PENDING', 'ACCEPTED', 'DECLINED', 'CANCELLED');

-- 1. Meetings Table
CREATE TABLE meetings (
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

-- 2. Meeting Participants Table
CREATE TABLE meeting_participants (
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

-- 3. Meeting Invitations Table
CREATE TABLE meeting_invitations (
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

-- 4. Meeting Sessions Table (Runtime presence & media state)
CREATE TABLE meeting_sessions (
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

-- 5. Meeting Messages Table (Chat persistence)
CREATE TABLE meeting_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    meeting_id UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    sender_user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

---

## 2. API Endpoints List

| Method | Endpoint | Description | Auth Required | Permissions |
|---|---|---|---|---|
| `POST` | `/api/meetings` | Create meeting (Instant Room or Scheduled) | Yes | Authenticated user (becomes Host) |
| `GET` | `/api/meetings` | List meetings with filters (`tab`, `search`, `status`, `page`, `size`) | Yes | Host, Participant, Admin (sees all) |
| `GET` | `/api/meetings/{id}` | Get meeting details | Yes | Host, Participant, Admin, Open Room |
| `PUT` | `/api/meetings/{id}` | Update meeting details/permissions | Yes | Host, Admin |
| `DELETE` | `/api/meetings/{id}` | Cancel meeting (soft delete) | Yes | Host, Admin |
| `POST` | `/api/meetings/{id}/start` | Start scheduled meeting (transition to LIVE) | Yes | Host, Admin |
| `POST` | `/api/meetings/{id}/end` | End meeting (transition to COMPLETED) | Yes | Host, Admin |
| `POST` | `/api/meetings/{id}/join` | Join meeting by meeting ID | Yes | Host, Invited Participant, Open Room |
| `POST` | `/api/meetings/join-code` | Join meeting by 9-character code | Yes | Host, Invited Participant, Open Room |
| `POST` | `/api/meetings/{id}/leave` | Leave meeting | Yes | Any joined participant |
| `GET` | `/api/meetings/{id}/participants` | List meeting participants | Yes | Host, Participant, Admin |
| `POST` | `/api/meetings/{id}/participants` | Add participant by userId or email | Yes | Host, Admin |
| `DELETE` | `/api/meetings/{id}/participants/{userId}` | Remove participant | Yes | Host, Admin |
| `GET` | `/api/meetings/{id}/messages` | Get meeting chat history | Yes | Host, Participant, Admin |
| `POST` | `/api/meetings/{id}/messages` | Send chat message via REST fallback | Yes | Host, Participant, Admin |
| `WS` | `/ws/meetings/{meetingId}` | WebRTC signaling & real-time chat WebSocket | Yes (JWT) | Host, Participant, Admin |

---

## 3. Role & Permission Matrix

| Feature / Action | HOST | PARTICIPANT | ADMIN | UNAUTHORIZED USER |
|---|---|---|---|---|
| Create Meeting | ✅ | ✅ | ✅ | ❌ (401) |
| Join Own Meeting | ✅ (Immediate) | N/A | ✅ (Immediate) | ❌ (401) |
| Join Private Meeting | ✅ | ✅ (If invited) | ✅ | ❌ (403) |
| Join Open Room | ✅ | ✅ | ✅ | ❌ (401 without JWT) |
| Password Bypass | ✅ | ❌ (Requires password) | ✅ | ❌ |
| Start / End Meeting | ✅ | ❌ (403) | ✅ | ❌ |
| Update Settings | ✅ | ❌ (403) | ✅ | ❌ |
| Cancel / Delete | ✅ | ❌ (403) | ✅ | ❌ |
| Add Participants | ✅ | ❌ (403) | ✅ | ❌ |
| Remove Participants | ✅ | ❌ (403) | ✅ | ❌ |
| Screen Share | ✅ | ✅ (If enabled) | ✅ | ❌ |
| Camera / Mic | ✅ | ✅ (If enabled) | ✅ | ❌ |
| Send Chat | ✅ | ✅ (If enabled) | ✅ | ❌ |
| View All Meetings | ❌ (Hosted + Invited) | ❌ (Hosted + Invited) | ✅ (All in system) | ❌ |

---

## 4. Request & Response Examples

### A. Create Instant Meeting
**POST** `/api/meetings`
```json
{
  "title": "Ad-hoc Architecture Sync",
  "description": "Discuss architecture and implementation",
  "meetingType": "INSTANT_ROOM",
  "password": "secret123",
  "participantUserIds": ["d9b1c784-38c2-4632-8431-7e8c3b5d2719"],
  "participantEmails": ["colleague@connectsoar.com"],
  "allowParticipantChat": true,
  "allowScreenSharing": true,
  "muteParticipantsOnEntry": false,
  "allowParticipantVideo": true,
  "allowParticipantAudio": true,
  "isOpenRoom": false
}
```

**Response (HTTP 201):**
```json
{
  "success": true,
  "message": "Meeting created successfully",
  "data": {
    "id": "5fa2dc09-4bf9-4595-bdcf-ae8d00921a22",
    "meetingCode": "ABC-DEF-GHI",
    "meetingUrl": "https://connectsoar.com/meeting/ABC-DEF-GHI",
    "title": "Ad-hoc Architecture Sync",
    "description": "Discuss architecture and implementation",
    "meetingType": "INSTANT_ROOM",
    "status": "LIVE",
    "host": {
      "id": "e0d376d2-f04b-4b18-b2e4-e0b6dfa994ef",
      "name": "John Doe",
      "email": "john@connectsoar.com"
    },
    "participants": [
      {
        "id": "563065b2-70b0-4cbb-9271-897db6746ef7",
        "meetingId": "5fa2dc09-4bf9-4595-bdcf-ae8d00921a22",
        "userId": "e0d376d2-f04b-4b18-b2e4-e0b6dfa994ef",
        "name": "John Doe",
        "email": "john@connectsoar.com",
        "participantRole": "HOST",
        "status": "JOINED"
      }
    ],
    "participantCount": 2,
    "permissions": {
      "allowParticipantChat": true,
      "allowScreenSharing": true,
      "muteParticipantsOnEntry": false,
      "allowParticipantVideo": true,
      "allowParticipantAudio": true,
      "isOpenRoom": false
    },
    "passwordProtected": true,
    "password": "secret123",
    "createdAt": "2026-09-08T00:45:00",
    "startedAt": "2026-09-08T00:45:00"
  }
}
```

---

### B. List Meetings with Tabs & Search
**GET** `/api/meetings?tab=upcoming&search=Architecture&page=0&size=20`

**Response (HTTP 200):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "5fa2dc09-4bf9-4595-bdcf-ae8d00921a22",
        "meetingCode": "ABC-DEF-GHI",
        "meetingUrl": "https://connectsoar.com/meeting/ABC-DEF-GHI",
        "title": "Architecture Deep Dive",
        "meetingType": "SCHEDULED_MEETING",
        "status": "SCHEDULED",
        "host": {
          "id": "e0d376d2-f04b-4b18-b2e4-e0b6dfa994ef",
          "name": "John Doe",
          "email": "john@connectsoar.com"
        },
        "scheduledStartTime": "2026-09-08T15:00:00",
        "scheduledEndTime": "2026-09-08T15:45:00",
        "durationMinutes": 45,
        "timezone": "Asia/Kolkata",
        "participantCount": 3,
        "permissions": {
          "allowParticipantChat": true,
          "allowScreenSharing": true,
          "muteParticipantsOnEntry": false,
          "allowParticipantVideo": true,
          "allowParticipantAudio": true
        }
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

### C. Join Meeting by Code
**POST** `/api/meetings/join-code`
```json
{
  "meetingCode": "ABC-DEF-GHI",
  "password": "secret123"
}
```

**Response (HTTP 200):**
```json
{
  "success": true,
  "message": "Meeting joined successfully",
  "data": {
    "meetingId": "5fa2dc09-4bf9-4595-bdcf-ae8d00921a22",
    "meetingCode": "ABC-DEF-GHI",
    "meetingUrl": "https://connectsoar.com/meeting/ABC-DEF-GHI",
    "roomId": "room-5fa2dc09-4bf9-4595-bdcf-ae8d00921a22",
    "role": "HOST",
    "status": "LIVE",
    "permissions": {
      "canChat": true,
      "canShareScreen": true,
      "canUseCamera": true,
      "canUseMicrophone": true,
      "mutedOnEntry": false
    },
    "sessionId": "a2444db8-8d2a-4dfa-bb6f-47401d4ca860",
    "meetingToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 900
  }
}
```

---

## 5. WebSocket Signaling & Event Format

### Endpoint: `ws://<host>/ws/meetings/{meetingId}?token=<JWT>`

### Client -> Server Messages:
1. **Send WebRTC Offer:**
```json
{
  "type": "OFFER",
  "targetUserId": "d9b1c784-38c2-4632-8431-7e8c3b5d2719",
  "sdp": "v=0\r\no=- 42... IN IP4 127.0.0.1..."
}
```

2. **Send WebRTC Answer:**
```json
{
  "type": "ANSWER",
  "targetUserId": "e0d376d2-f04b-4b18-b2e4-e0b6dfa994ef",
  "sdp": "v=0\r\no=- 43... IN IP4 127.0.0.1..."
}
```

3. **Send ICE Candidate:**
```json
{
  "type": "ICE_CANDIDATE",
  "targetUserId": "d9b1c784-38c2-4632-8431-7e8c3b5d2719",
  "candidate": "candidate:842163049 1 udp 1677729535 192.168.1.100 54321 typ host..."
}
```

4. **Update Media State:**
```json
{
  "type": "MEDIA_STATE_CHANGED",
  "microphoneEnabled": false,
  "cameraEnabled": true,
  "screenSharing": false
}
```

5. **Send In-Meeting Chat:**
```json
{
  "type": "CHAT_MESSAGE",
  "message": "Hello everyone!"
}
```

### Server -> Client Broadcasts:
- `USER_JOINED` - Notifies room when a peer joins.
- `USER_LEFT` - Notifies room when a peer leaves or disconnects.
- `CHAT_MESSAGE` - Delivers real-time chat messages to all peers in the room.
- `MEDIA_STATE_CHANGED` - Broadcasts peer microphone/camera/screen state changes.
- `ERROR` - Sent when an action violates room permissions (e.g. `SCREEN_SHARE_DISABLED`, `CHAT_DISABLED`).

---

## 6. Environment Variables

| Variable | Default Value | Description |
|---|---|---|
| `SERVER_PORT` / `PORT` | `8080` | Backend HTTP listening port |
| `JWT_SECRET` | `ConnectSoarProductionGradeSuperSecretJwtKey2026!...` | Secret for verifying and signing JWT tokens |
| `SUPABASE_URL` | `https://iqtkkvmphqvzqwkinmfo.supabase.co` | Supabase auth/database URL |
| `SUPABASE_ANON_KEY` | `eyJhbGci...` | Supabase anon key |
| `SUPABASE_SERVICE_ROLE_KEY` | `""` | Supabase service role key |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173,...` | Allowed CORS origins |
| `APP_MEETING_BASE_URL` | `https://connectsoar.com/meeting` | Base URL used when generating meeting links |

---

## 7. Example cURL Commands

### 1. Create Instant Meeting
```bash
curl -X POST http://localhost:8080/api/meetings \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Quick Standup",
    "meetingType": "INSTANT_ROOM",
    "allowParticipantChat": true,
    "allowScreenSharing": true
  }'
```

### 2. List Upcoming Meetings
```bash
curl -X GET "http://localhost:8080/api/meetings?tab=upcoming" \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

### 3. Join by Meeting Code
```bash
curl -X POST http://localhost:8080/api/meetings/join-code \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "meetingCode": "ABC-DEF-GHI"
  }'
```

### 4. Fetch Chat History
```bash
curl -X GET http://localhost:8080/api/meetings/<MEETING_ID>/messages \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```
