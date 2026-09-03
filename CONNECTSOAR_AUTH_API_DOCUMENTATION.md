# ConnectSoar — Authentication API Documentation

**Base URL**: `http://localhost:8080/api/v1/auth`  
**Standard Response Format**: Consistent JSON envelope across all endpoints.

---

## 1. Architecture & Security Overview

ConnectSoar uses **Supabase Auth + PostgreSQL** with a zero-trust backend layer:

1. **Password Safety**: Supabase Auth handles identity, hashing, and password management. The application `profiles` table never stores passwords.
2. **First-Time Login Gatekeeping**: When an admin provisions an employee (`reset_password = true`), login returns HTTP `403 Forbidden` with error code `PASSWORD_CHANGE_REQUIRED`. **NO `access_token` or `refresh_token` is issued**.
3. **Clean Token Response Contract**: `token_type` and `expires_in` are handled internally and strictly hidden (`@JsonIgnore`) from the HTTP JSON response. Only `access_token` and `refresh_token` are sent.
4. **Live Status Enforcement**: Inactive or suspended accounts (`status = inactive / suspended`) are rejected immediately, even if a valid JWT is presented.
5. **Anti-Enumeration**: Login and Forgot-Password endpoints return generic responses that never reveal if an email exists.
6. **Rate Limiting**: Throttling protects `/login`, `/refresh`, `/forgot-password`, and `/change-password` from brute-force attacks.

---

## 2. API Endpoints Reference

### 1. User Login
- **Endpoint**: `POST /api/v1/auth/login`
- **Headers**: `Content-Type: application/json`

#### Request:
```json
{
  "email": "employee@example.com",
  "password": "UserPassword123!"
}
```

#### Response (Case A: Normal Login `reset_password = false`):
**Status**: `200 OK`
```json
{
  "success": true,
  "message": "Login successful.",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIs...",
    "refresh_token": "0b4c8038-d635-4309-a7b6-c677610368a5",
    "user": {
      "id": "c8d62635-4309-450f-a7b6-c677610368a5",
      "email": "employee@example.com",
      "name": "Rahul Kumar",
      "role": "employee",
      "status": "active",
      "department": "Engineering",
      "designation": "Flutter Developer",
      "phone": "+919876543210",
      "image_url": null,
      "reset_password": false,
      "created_at": "2026-08-31T13:30:00Z",
      "updated_at": "2026-08-31T13:30:00Z"
    }
  }
}
```

#### Response (Case B: First-Time Login `reset_password = true`):
*No session tokens issued! Client is navigated to the Password Change screen.*  
**Status**: `403 Forbidden`
```json
{
  "success": false,
  "error": {
    "code": "PASSWORD_CHANGE_REQUIRED",
    "message": "Password change is required before accessing the application."
  },
  "data": {
    "reset_password": true,
    "password_reset_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "c8d62635-4309-450f-a7b6-c677610368a5",
      "email": "new.employee@example.com",
      "name": "Jane New",
      "role": "employee"
    }
  }
}
```

#### Response (Case C: Invalid Credentials):
**Status**: `401 Unauthorized`
```json
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "Invalid email or password."
  }
}
```

#### Response (Case D: Inactive / Suspended Account):
**Status**: `403 Forbidden`
```json
{
  "success": false,
  "error": {
    "code": "USER_INACTIVE",
    "message": "User account is inactive."
  }
}
```

---

### 2. Refresh Token
- **Endpoint**: `POST /api/v1/auth/refresh`
- **Headers**: `Content-Type: application/json`

#### Request:
```json
{
  "refresh_token": "0b4c8038-d635-4309-a7b6-c677610368a5"
}
```

#### Response (Success):
**Status**: `200 OK`
```json
{
  "success": true,
  "message": "Token refreshed successfully.",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.NEW_ACCESS_TOKEN...",
    "refresh_token": "NEW_ROTATED_REFRESH_TOKEN..."
  }
}
```

#### Response (Invalid Token):
**Status**: `401 Unauthorized`
```json
{
  "success": false,
  "error": {
    "code": "TOKEN_INVALID",
    "message": "Invalid or expired refresh token."
  }
}
```

---

### 3. Get Current User Profile
- **Endpoint**: `GET /api/v1/auth/me`
- **Headers**: `Authorization: Bearer <access_token>`

#### Response (Success):
**Status**: `200 OK`
```json
{
  "success": true,
  "data": {
    "id": "c8d62635-4309-450f-a7b6-c677610368a5",
    "email": "employee@example.com",
    "name": "Rahul Kumar",
    "role": "employee",
    "status": "active",
    "department": "Engineering",
    "designation": "Flutter Developer",
    "phone": "+919876543210",
    "image_url": null,
    "reset_password": false,
    "created_at": "2026-08-31T13:30:00Z",
    "updated_at": "2026-08-31T13:30:00Z"
  }
}
```

---

### 4. Change Password
- **Endpoint**: `POST /api/v1/auth/change-password`
- **Headers**: `Content-Type: application/json`

#### Request (Option A: First-time setup using `reset_token`):
```json
{
  "new_password": "NewSecurePassword456!",
  "confirm_password": "NewSecurePassword456!",
  "reset_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Request (Option B: Normal authenticated session):
```json
{
  "current_password": "OldPassword123!",
  "new_password": "NewSecurePassword456!",
  "confirm_password": "NewSecurePassword456!"
}
```

#### Response (Success):
**Status**: `200 OK`
```json
{
  "success": true,
  "message": "Password changed successfully.",
  "data": {
    "reset_password": false
  }
}
```

---

### 5. Forgot Password
- **Endpoint**: `POST /api/v1/auth/forgot-password`
- **Headers**: `Content-Type: application/json`

#### Request:
```json
{
  "email": "employee@example.com"
}
```

#### Response (Generic Anti-Enumeration):
**Status**: `200 OK`
```json
{
  "success": true,
  "message": "If an account exists with this email, password reset instructions have been sent."
}
```

---

### 6. Logout
- **Endpoint**: `POST /api/v1/auth/logout`
- **Headers**: `Authorization: Bearer <access_token>`

#### Response (Success):
**Status**: `200 OK`
```json
{
  "success": true,
  "message": "Logged out successfully."
}
```

---

## 3. Standard Error Codes Reference

| Error Code | HTTP Status | Meaning |
|---|---|---|
| `INVALID_CREDENTIALS` | 401 | Invalid email or password. |
| `UNAUTHORIZED` | 401 | Missing or invalid Authorization Bearer header. |
| `TOKEN_EXPIRED` | 401 | Access token expired. Refresh required. |
| `TOKEN_INVALID` | 401 | Token signature or format invalid. |
| `PASSWORD_CHANGE_REQUIRED` | 403 | First login password update mandatory. Tokens blocked. |
| `USER_INACTIVE` | 403 | User account is inactive/disabled. |
| `USER_SUSPENDED` | 403 | User account is suspended. |
| `FORBIDDEN` | 403 | Insufficient role permissions. |
| `RATE_LIMITED` | 429 | Too many requests in short duration. |
| `VALIDATION_ERROR` | 400 | Request body failed input validation rules. |
