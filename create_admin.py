import urllib.request
import json
import uuid

supabase_url = "https://iqtkkvmphqvzqwkinmfo.supabase.co"
anon_key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlxdGtrdm1waHF2enF3a2lubWZvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc1MTIxNjUsImV4cCI6MjEwMzA4ODE2NX0.fwCfR7uqbo3Xyaljkl3mE2Oo7hufeFmIOnxbzaDW9y4"

email = "sudhanshu@gmail.com"
password = "AdminPassword@123"
name = "Sudhanshu Shekhar"
phone = "8228958397"
role = "admin"
designation = "flutter developer"
org_name = "codesoar technology"

signup_payload = {
    "email": email,
    "password": password,
    "data": {
        "name": name,
        "role": role,
        "phone": phone,
        "designation": designation
    }
}

req = urllib.request.Request(
    f"{supabase_url}/auth/v1/signup",
    data=json.dumps(signup_payload).encode("utf-8"),
    headers={
        "apikey": anon_key,
        "Authorization": f"Bearer {anon_key}",
        "Content-Type": "application/json"
    },
    method="POST"
)

try:
    with urllib.request.urlopen(req) as resp:
        res_data = json.loads(resp.read().decode("utf-8"))
        print("SIGNUP_SUCCESS:", json.dumps(res_data, indent=2))
except urllib.error.HTTPError as e:
    err_body = e.read().decode('utf-8')
    print(f"SIGNUP_ERROR {e.code}: {err_body}")
except Exception as e:
    print(f"ERROR: {e}")
