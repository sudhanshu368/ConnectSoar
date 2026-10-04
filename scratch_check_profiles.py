import urllib.request
import json

supabase_url = "https://iqtkkvmphqvzqwkinmfo.supabase.co"
anon_key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlxdGtrdm1waHF2enF3a2lubWZvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc1MTIxNjUsImV4cCI6MjEwMzA4ODE2NX0.fwCfR7uqbo3Xyaljkl3mE2Oo7hufeFmIOnxbzaDW9y4"

# 1. Supabase login
login_body = json.dumps({"email": "codesoartechnologies@gmail.com", "password": "Admin123!"}).encode("utf-8")
req = urllib.request.Request(
    f"{supabase_url}/auth/v1/token?grant_type=password",
    data=login_body,
    headers={"apikey": anon_key, "Authorization": f"Bearer {anon_key}", "Content-Type": "application/json"}
)

try:
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode("utf-8"))
        token = data["access_token"]
        user_meta = data["user"].get("user_metadata", {})
        print("Supabase Login Success:")
        print("User ID:", data["user"]["id"])
        print("User Metadata:", user_meta)

        # Check profiles table with user token
        req_prof = urllib.request.Request(
            f"{supabase_url}/rest/v1/profiles?select=*",
            headers={"apikey": anon_key, "Authorization": f"Bearer {token}"}
        )
        with urllib.request.urlopen(req_prof) as p_resp:
            profiles = json.loads(p_resp.read().decode("utf-8"))
            print("Supabase Profiles count:", len(profiles))
            for p in profiles:
                print("Profile row:", p)
except Exception as e:
    print("Supabase Login Error:", e)

# 2. Backend API login
backend_url = "https://connectsoar-backend.onrender.com"
req_back = urllib.request.Request(
    f"{backend_url}/api/v1/auth/login",
    data=login_body,
    headers={"Content-Type": "application/json"}
)
try:
    with urllib.request.urlopen(req_back) as resp:
        b_data = json.loads(resp.read().decode("utf-8"))
        print("\nBackend /api/v1/auth/login Success:")
        print(json.dumps(b_data, indent=2))

        access_token = b_data.get("data", {}).get("access_token")
        if access_token:
            req_me = urllib.request.Request(
                f"{backend_url}/api/v1/auth/me",
                headers={"Authorization": f"Bearer {access_token}"}
            )
            with urllib.request.urlopen(req_me) as m_resp:
                me_data = json.loads(m_resp.read().decode("utf-8"))
                print("\nBackend /api/v1/auth/me Response:")
                print(json.dumps(me_data, indent=2))
except Exception as e:
    print("Backend Login Error:", e)
