import urllib.request
import json

supabase_url = "https://iqtkkvmphqvzqwkinmfo.supabase.co"
anon_key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlxdGtrdm1waHF2enF3a2lubWZvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc1MTIxNjUsImV4cCI6MjEwMzA4ODE2NX0.fwCfR7uqbo3Xyaljkl3mE2Oo7hufeFmIOnxbzaDW9y4"

login_payload = {
    "email": "sudhanshu@gmail.com",
    "password": "AdminPassword@123"
}

req = urllib.request.Request(
    f"{supabase_url}/auth/v1/token?grant_type=password",
    data=json.dumps(login_payload).encode("utf-8"),
    headers={
        "apikey": anon_key,
        "Authorization": f"Bearer {anon_key}",
        "Content-Type": "application/json"
    },
    method="POST"
)

with urllib.request.urlopen(req) as resp:
    res_data = json.loads(resp.read().decode("utf-8"))
    print("ADMIN_LOGIN_SUCCESS:")
    print("User ID:", res_data["user"]["id"])
    print("Email:", res_data["user"]["email"])
    print("Role:", res_data["user"]["user_metadata"]["role"])
    print("Designation:", res_data["user"]["user_metadata"]["designation"])
    print("Access Token (truncated):", res_data["access_token"][:50] + "...")
