import urllib.request
import json

supabase_url = "https://iqtkkvmphqvzqwkinmfo.supabase.co"
anon_key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlxdGtrdm1waHF2enF3a2lubWZvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc1MTIxNjUsImV4cCI6MjEwMzA4ODE2NX0.fwCfR7uqbo3Xyaljkl3mE2Oo7hufeFmIOnxbzaDW9y4"

# Login with user credentials to get live authenticated token
login_payload = {
    "email": "sudhanshu@gmail.com",
    "password": "AdminPassword@123"
}

req_login = urllib.request.Request(
    f"{supabase_url}/auth/v1/token?grant_type=password",
    data=json.dumps(login_payload).encode("utf-8"),
    headers={
        "apikey": anon_key,
        "Authorization": f"Bearer {anon_key}",
        "Content-Type": "application/json"
    },
    method="POST"
)

with urllib.request.urlopen(req_login) as resp:
    token_data = json.loads(resp.read().decode("utf-8"))
    user_access_token = token_data["access_token"]
    user_id = token_data["user"]["id"]
    print("LOGGED_IN, user_id:", user_id)

# 1. Insert/Upsert Profile with user_access_token
profile_payload = {
    "id": user_id,
    "email": "sudhanshu@gmail.com",
    "name": "Sudhanshu Shekhar",
    "role": "admin",
    "status": "active",
    "designation": "flutter developer",
    "phone": "8228958397",
    "reset_password": False
}

req_prof = urllib.request.Request(
    f"{supabase_url}/rest/v1/profiles",
    data=json.dumps(profile_payload).encode("utf-8"),
    headers={
        "apikey": anon_key,
        "Authorization": f"Bearer {user_access_token}",
        "Content-Type": "application/json",
        "Prefer": "return=representation,resolution=merge-duplicates"
    },
    method="POST"
)

try:
    with urllib.request.urlopen(req_prof) as resp:
        print("PROFILE_CREATED:", resp.read().decode("utf-8"))
except Exception as e:
    print(f"PROFILE_ERR: {e}")

# 2. Insert Organization
org_payload = {
    "name": "codesoar technology",
    "status": "active"
}

req_org = urllib.request.Request(
    f"{supabase_url}/rest/v1/organizations",
    data=json.dumps(org_payload).encode("utf-8"),
    headers={
        "apikey": anon_key,
        "Authorization": f"Bearer {user_access_token}",
        "Content-Type": "application/json",
        "Prefer": "return=representation"
    },
    method="POST"
)

try:
    with urllib.request.urlopen(req_org) as resp:
        org_data = json.loads(resp.read().decode("utf-8"))
        print("ORG_CREATED:", json.dumps(org_data, indent=2))
        org_id = org_data[0]["id"]
        
        member_payload = {
            "organization_id": org_id,
            "user_id": user_id,
            "role": "admin"
        }
        req_mem = urllib.request.Request(
            f"{supabase_url}/rest/v1/organization_members",
            data=json.dumps(member_payload).encode("utf-8"),
            headers={
                "apikey": anon_key,
                "Authorization": f"Bearer {user_access_token}",
                "Content-Type": "application/json",
                "Prefer": "return=representation"
            },
            method="POST"
        )
        with urllib.request.urlopen(req_mem) as resp_mem:
            print("MEMBER_LINKED:", resp_mem.read().decode("utf-8"))
except Exception as e:
    print(f"ORG_ERR: {e}")
