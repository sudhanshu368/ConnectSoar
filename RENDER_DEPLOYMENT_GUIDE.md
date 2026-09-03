# ConnectSoar Backend - Render Deployment Guide 🚀

ConnectSoar Spring Boot Backend ko **Render** par deploy karne ke liye pura setup complete kar diya gaya hai.

---

## 🛠️ Kya-Kya Setup Kiya Gaya Hai:

1. **`Dockerfile` (Multi-stage Build)**:
   - Java 17 + Maven build stage (`maven:3.9.6-eclipse-temurin-17-alpine`)
   - Lightweight production runtime (`eclipse-temurin:17-jre-alpine`)
   - Optimized JVM container memory flags (`-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`)
   - Non-root user security

2. **`render.yaml` (Render Blueprint)**:
   - Automatic configuration for Render Web Service
   - Pre-configured health check path (`/health`)
   - Environment variables mapping

3. **`application.yml`**:
   - Dynamic port binding: `${PORT:${SERVER_PORT:8080}}` (Render automatically provides `PORT`)
   - Configurable CORS origins via `CORS_ALLOWED_ORIGINS`

4. **`HealthController.java`**:
   - `/health`, `/` and `/api/v1/health` endpoints added for Render uptime checks.

---

## 📋 Step-by-Step Render Deployment Process:

### Step 1: Code ko GitHub par Push Karein

Terminal / Command Prompt me ye commands run karein:

```bash
git add .
git commit -m "feat: setup Render deployment with Docker and health checks"
git push origin main
```

---

### Step 2: Render par Web Service Banayein

1. [dashboard.render.com](https://dashboard.render.com/) par login karein.
2. **"New +"** button par click karein aur **"Web Service"** select karein.
3. Apna GitHub repository (`ConnectSoar`) select/connect karein.
4. Settings fill karein:
   - **Name**: `connectsoar-backend` (ya aapki marzi ka naam)
   - **Region**: `Oregon (US West)` ya `Singapore` (nearest to users)
   - **Branch**: `main`
   - **Runtime / Environment**: `Docker`
   - **Dockerfile Path**: `./Dockerfile`
   - **Instance Type / Plan**: `Free`

---

### Step 3: Environment Variables Add Karein

Render Dashboard me **"Environment"** tab me jaakar ye variables add karein:

| Key | Value (Example / Description) |
|---|---|
| `SUPABASE_URL` | `https://your-project.supabase.co` |
| `SUPABASE_ANON_KEY` | Aapki Supabase project ki `anon` public key |
| `SUPABASE_SERVICE_ROLE_KEY` | Aapki Supabase project ki `service_role` secret key |
| `JWT_SECRET` | 256-bit secret string (e.g. `ConnectSoarProductionGradeSuperSecretJwtKey2026!...`) |
| `CORS_ALLOWED_ORIGINS` | `*` ya aapke frontend URLs (e.g. `https://your-frontend.vercel.app,http://localhost:3000`) |
| `LOG_LEVEL_CONNECTSOAR` | `INFO` |
| `LOG_LEVEL_SPRING` | `INFO` |

---

### Step 4: Deploy & Verify

1. **"Create Web Service"** par click karein.
2. Render automatically Docker build start karega aur deploy kar dega.
3. Jab deployment complete ho jaye ("Live"), tab verify karein:

```bash
curl https://connectsoar-backend.onrender.com/health
```

Expected Output:
```json
{
  "status": "UP",
  "service": "ConnectSoar Backend API",
  "version": "0.0.1-SNAPSHOT",
  "timestamp": "2026-09-03T..."
}
```
