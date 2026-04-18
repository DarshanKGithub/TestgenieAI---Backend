import requests
import json
import uuid

BASE_URL = "http://localhost:8080"

def main():
    email = f"user_{uuid.uuid4().hex[:8]}@example.com"
    payload = {
        "email": email,
        "password": "password123"
    }
    
    # 2) Registers a temp user and extracts JWT
    print(f"Registering user with email: {email}")
    try:
        register_response = requests.post(f"{BASE_URL}/api/v1/auth/register", json=payload)
    except Exception as e:
        print(f"Error connecting to server: {e}")
        return

    # Try login to get token
    print("Logging in...")
    login_response = requests.post(f"{BASE_URL}/api/v1/auth/login", json=payload)
    
    token = None
    if login_response.status_code == 200:
        token = login_response.json().get('token')
    elif register_response.status_code == 201:
        token = register_response.json().get('token')

    if not token:
        print("Could not obtain JWT token.")
        return

    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }

    # 3) Calls GET /api/v1/test-runs?page=0&size=1
    print("\nCall 1: GET /api/v1/test-runs?page=0&size=1")
    res1 = requests.get(f"{BASE_URL}/api/v1/test-runs?page=0&size=1", headers=headers)
    print(f"Status: {res1.status_code}")
    print(f"Body: {res1.text}")

    # 4) Calls POST /api/v1/ai/failure-analysis
    print("\nCall 2: POST /api/v1/ai/failure-analysis")
    sample_json = {
        "suiteName": "UserAuthSuite",
        "testName": "LoginTest",
        "errorLog": "java.lang.NullPointerException: Cannot invoke \"String.length()\" because \"input\" is null\n\tat com.testgenieai.backend.service.AuthService.login(AuthService.java:42)"
    }
    res2 = requests.post(f"{BASE_URL}/api/v1/ai/failure-analysis", headers=headers, json=sample_json)
    print(f"Status: {res2.status_code}")
    print(f"Body: {res2.text}")

if __name__ == "__main__":
    main()
