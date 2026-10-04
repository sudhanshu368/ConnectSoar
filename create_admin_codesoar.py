import urllib.request
import urllib.error
import json
import sys

supabase_url = "https://iqtkkvmphqvzqwkinmfo.supabase.co"
anon_key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlxdGtrdm1waHF2enF3a2lubWZvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc1MTIxNjUsImV4cCI6MjEwMzA4ODE2NX0.fwCfR7uqbo3Xyaljkl3mE2Oo7hufeFmIOnxbzaDW9y4"

email = "codesoartechnologies@gmail.com"
password = "Admin123!"
name = "CodeSoar Technologies"
role = "admin"
designation = "Administrator"
department = "Management"
phone = "9876543210"

print(f"=== Creating Admin Account: {email} ===")

# 1. Supabase Auth Signup
signup_payload = {
    "email": email,
    "password": password,
    "data": {
        "name": name,
        "role": role,
        "designation": designation,
        "department": department,
        "phone": phone
    }
}

req_signup = urllib.request.Request(
    f"{supabase_url}/auth/v1/signup",
    data=json.dumps(signup_payload).encode("utf-8"),
    headers={
        "apikey": anon_key,
        "Authorization": f"Bearer {anon_key}",
        "Content-Type": "application/json"
    },
    method="POST"
)

user_id = None
try:
    with urllib.request.urlopen(req_signup) as resp:
        res_data = json.loads(resp.read().decode("utf-8"))
        print("1. SIGNUP SUCCESSFUL!")
        user_node = res_data.get("user") or res_data
        user_id = user_node.get("id")
        print(f"   Created User ID: {user_id}")
except urllib.error.HTTPError as e:
    err_body = e.read().decode('utf-8')
    print(f"   Signup Note ({e.code}): {err_body}")
    # If user already exists, we will log in
except Exception as e:
    print(f"   Signup error: {e}")

# 2. Authenticate / Login to get Access Token
login_payload = {
    "email": email,
    "password": password
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

try:
    with urllib.request.urlopen(req_login) as resp:
        token_data = json.loads(resp.read().decode("utf-8"))
        user_access_token = token_data["access_token"]
        user_id = token_data["user"]["id"]
        print("2. LOGIN SUCCESSFUL!")
        print(f"   Logged in User ID: {user_id}")
        print(f"   Token: {user_access_token[:30]}...")
except Exception as e:
    print(f"2. LOGIN FAILED: {e}")
    sys.exit(1)

# 3. Upsert into Supabase `profiles` table
profile_payload = {
    "id": user_id,
    "email": email,
    "name": name,
    "role": role,
    "status": "active",
    "designation": designation,
    "department": department,
    "phone": phone,
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
        print("3. PROFILE RECORD CREATED / UPSERTED SUCCESSFULLY!")
        print("   Profile Data:", resp.read().decode("utf-8"))
except Exception as e:
    print(f"3. PROFILE UPSERT NOTE: {e}")

# 4. Link to default organization if exists
try:
    req_get_org = urllib.request.Request(
        f"{supabase_url}/rest/v1/organizations?select=id,name&limit=1",
        headers={
            "apikey": anon_key,
            "Authorization": f"Bearer {user_access_token}"
        },
        method="GET"
    )
    with urllib.request.urlopen(req_get_org) as resp_org:
        orgs = json.loads(resp_org.read().decode("utf-8"))
        if orgs:
            org_id = orgs[0]["id"]
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
                    "Prefer": "resolution=merge-duplicates"
                },
                method="POST"
            )
            with urllib.request.urlopen(req_mem) as resp_mem:
                print("4. ORGANIZATION MEMBERSHIP LINKED AS ADMIN!")
except Exception as e:
    print(f"4. ORG LINK NOTE: {e}")

print("\n=== ADMIN USER CREATION COMPLETE ===")
print(f"Email: {email}")
print(f"Password: {password}")
print(f"Role: {role}")
print(f"Name: {name}")
print(f"User ID: {user_id}")
