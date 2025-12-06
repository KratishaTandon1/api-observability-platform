import requests
import random
import time
import json
from datetime import datetime

# Configuration
COLLECTOR_URL = "http://localhost:8080/api/v1/logs"
SERVICES = ["order-service", "payment-service", "user-service", "inventory-service"]
ENDPOINTS = ["/api/orders", "/api/pay", "/api/profile", "/api/search", "/api/stock"]
METHODS = ["GET", "POST", "PUT", "DELETE"]

def generate_log():
    # 1. Prepare Data
    service = random.choice(SERVICES)
    endpoint = random.choice(ENDPOINTS)
    method = random.choice(METHODS)
    
    status = 200
    duration = random.randint(20, 300)
    rate_limit = False
    
    # 2. Simulate Scenarios
    scenario = random.choices(["normal", "slow", "error", "ratelimit"], weights=[70, 15, 10, 5])[0]
    
    if scenario == "slow":
        duration = random.randint(501, 2000)
    elif scenario == "error":
        status = random.choice([500, 502, 503])
    elif scenario == "ratelimit":
        status = 429
        rate_limit = True

    # 3. Build JSON (Exact match for the simplified ApiLog.kt)
    payload = {
        "serviceName": service,
        "endpoint": endpoint,
        "method": method,          # Matches val method
        "status": status,          # Matches val status
        "duration": duration,      # Matches val duration
        "requestSize": random.randint(100, 5000),
        "responseSize": random.randint(100, 5000),
        # Timestamp removed: The backend will auto-generate it using Instant.now()
        # This prevents the 400 Bad Request error.
        "rateLimitHit": rate_limit, # Matches val rateLimitHit
        "resolved": False
    }

    return payload

def main():
    print(f"🚀 Sending logs to {COLLECTOR_URL}...")
    try:
        while True:
            # Create a small batch
            batch = [generate_log() for _ in range(random.randint(1, 3))]
            headers = {'Content-Type': 'application/json'}
            
            try:
                response = requests.post(COLLECTOR_URL, data=json.dumps(batch), headers=headers)
                
                if response.status_code == 202:
                    print(f"✅ Sent {len(batch)} logs   ", end='\r')
                else:
                    print(f"\n❌ Failed: {response.status_code}")
                    print(f"👉 Server Response: {response.text}")
            except Exception as e:
                 print(f"\n❌ Connection Error: Is the backend running? ({e})")

            time.sleep(1) 
            
    except KeyboardInterrupt:
        print("\n🛑 Stopped.")

if __name__ == "__main__":
    main()