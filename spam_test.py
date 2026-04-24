import urllib.request
import urllib.error
import json
import threading
import time

BASE_URL = "http://localhost:8080/api"
TOTAL_REQUESTS = 200

success_count = 0
blocked_count = 0
error_count = 0
lock = threading.Lock()

def create_fresh_post():
    # Setup data to ensure User 1 and Bot 1 exist, ignore errors if they already do
    try:
        user_req = urllib.request.Request(f"{BASE_URL}/users", data=json.dumps({"username": "testuser", "premium": True}).encode("utf-8"), headers={"Content-Type": "application/json"}, method="POST")
        urllib.request.urlopen(user_req, timeout=5)
    except: pass
    
    try:
        bot_req = urllib.request.Request(f"{BASE_URL}/bots", data=json.dumps({"name": "TestBot", "personaDescription": "Bot"}).encode("utf-8"), headers={"Content-Type": "application/json"}, method="POST")
        urllib.request.urlopen(bot_req, timeout=5)
    except: pass

    # Create a fresh post
    post_req = urllib.request.Request(
        f"{BASE_URL}/posts",
        data=json.dumps({"authorId": 1, "authorType": "USER", "content": "Fresh Post for Spam Test"}).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="POST"
    )
    try:
        with urllib.request.urlopen(post_req, timeout=5) as resp:
            post_data = json.loads(resp.read().decode())
            return post_data['id']
    except Exception as e:
        if isinstance(e, urllib.error.HTTPError):
            raise Exception(f"HTTP {e.code}: {e.read().decode()}")
        raise e

def fire_request(post_id, human_id):
    global success_count, blocked_count, error_count
    url = f"{BASE_URL}/posts/{post_id}/comments"
    body = json.dumps({
        "authorId": 1,
        "authorType": "BOT",
        "content": f"Concurrent Bot Spam Comment {human_id}",
        "depthLevel": 1,
        "humanId": human_id
    }).encode("utf-8")

    req = urllib.request.Request(url, data=body, headers={"Content-Type": "application/json"}, method="POST")
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            with lock:
                success_count += 1
    except urllib.error.HTTPError as e:
        with lock:
            if e.code == 429:
                blocked_count += 1
            else:
                error_count += 1
    except Exception:
        with lock:
            error_count += 1

print("Preparing environment...")
try:
    fresh_post_id = create_fresh_post()
    print(f"Created a fresh Post with ID: {fresh_post_id}")
except Exception as e:
    print(f"Failed to create a fresh post. Make sure your Spring Boot server is running! Error: {e}")
    exit(1)

print("\nStarting Concurrency Spam Test on Post ID {}...".format(fresh_post_id))
print("Firing {} simultaneous requests...".format(TOTAL_REQUESTS))

threads = [threading.Thread(target=fire_request, args=(fresh_post_id, i)) for i in range(1, TOTAL_REQUESTS + 1)]

start = time.time()
for t in threads:
    t.start()

for t in threads:
    t.join()

elapsed = time.time() - start
print("\nTest Completed in {:.2f}s".format(elapsed))
print("Success (Comment Saved): {}".format(success_count))
print("Blocked (429 - Guardrail Working): {}".format(blocked_count))
print("Errors (Other issues): {}".format(error_count))

if success_count == 100 and blocked_count == 100:
    print("\nHORIZONTAL CAP TEST PASSED PERFECTLY!")
else:
    print("\nTEST FAILED - Check your logic")
