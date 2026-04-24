# Grid07 Backend Engineering Assignment: API Gateway & Guardrails

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

A high-performance Spring Boot microservice acting as a central API gateway and guardrail system. This project handles concurrent requests, manages distributed state using Redis, and implements event-driven scheduling.

## 📌 Features

*   **Core API:** RESTful endpoints for Users, Bots, Posts, and Comments.
*   **Redis Guardrails:** Throttles traffic and enforces strict operational caps.
*   **Thread Safety:** Bulletproof atomic operations preventing race conditions under high concurrency.
*   **Notification Engine:** Throttled, scheduled batch push-notifications (CRON Sweeper).
*   **Stateless Architecture:** Application state is entirely managed by Redis.

---

## 🛠️ Tech Stack
*   **Language:** Java 17
*   **Framework:** Spring Boot 3.x, Spring Data JPA, Spring Data Redis
*   **Database:** PostgreSQL (Source of truth)
*   **Cache/State:** Redis (Gatekeeper)
*   **Infrastructure:** Docker & Docker Compose

---

## 🚀 Getting Started

### Prerequisites
*   Java 17+ installed
*   Maven installed
*   Docker & Docker Compose installed

### 1. Start Infrastructure (Postgres & Redis)
```bash
docker-compose up -d
```

### 2. Run the Application
```bash
./mvnw spring-boot:run
```
*The application will start on `http://localhost:8080`*

---

## 🧪 Testing the API

### Postman Collection
A complete Postman collection is included in the repository.
1. Open Postman.
2. Click **Import** and select `Grid07_Assignment.postman_collection.json`.
3. Use the configured endpoints to create Users, Bots, Posts, and Comments.

### Concurrency & Spam Testing (Phase 4)
To prove the system's resilience against race conditions, a Python script is included to simulate 200 concurrent bot interactions firing at the exact same millisecond.

**How to run the spam test:**
```bash
python spam_test.py
```
**Expected Output:**
The script dynamically provisions a fresh Post and blasts it with 200 concurrent threads. You will see exactly `100` successful inserts and `100` requests returning `429 Too Many Requests`, proving the atomic locks function perfectly.

---

## 🔒 Architectural Decisions & Thread Safety (Phase 2)

A primary focus of this architecture is guaranteeing strict thread safety when validating the **Horizontal Cap (Max 100 bot replies per post)** under extreme concurrency. 

### How Thread Safety & Atomic Locks are Guaranteed:
1.  **Redis as the Gatekeeper:** Java memory (HashMaps/Static variables) is completely bypassed to maintain true statelessness. Redis handles all counting operations.
2.  **Atomic `INCR` Operations:** Instead of the traditional (and dangerous) `GET -> Check -> SET` pattern, the application utilizes Redis's atomic `INCR` command via `redisTemplate.opsForValue().increment()`.
3.  **The Validation Flow:**
    *   When a request arrives, Redis atomically increments the counter and returns the new value in a single, indivisible operation.
    *   The Java application evaluates this returned value (`if newCount > 100`).
    *   If the limit is exceeded, the application rolls back the increment (`DECR`) and immediately throws a `RuntimeException`.
4.  **Database Integrity:** The `addComment` method is annotated with `@Transactional`. If the Redis guardrail throws an exception, the PostgreSQL transaction is aborted, ensuring the database (the source of truth) never receives the 101st comment.

This approach guarantees zero race conditions, even if hundreds of containers process requests simultaneously.

---
Challenges I Faced & How I Solved Them
Building this system was a great learning experience, especially dealing with distributed state. Here are some key challenges I tackled:

1. The "Race Condition" Trap
Initially, I thought of doing a simple GET count from Redis, checking it in Java, and then doing a SET. But I realized that under high concurrency (like 200 bots hitting at once), two requests might read the same count simultaneously and both would proceed, breaking the 100-comment limit.

The Fix: I switched to using Redis's atomic INCR operation. Since Redis is single-threaded, it ensures every increment is handled one-by-one. I then checked the result of that increment in Java. This guaranteed that the 101st request would always be blocked, no matter how fast it came.

2. Local vs Docker Redis Collision
During testing, I noticed my guardrails weren't resetting even after I flushed the Docker Redis. It was quite a head-scratcher!

The Fix: After some debugging with netstat and tasklist, I discovered a native Windows Redis service was running on the same port as my Docker container. My Spring Boot app was silently connecting to the native one. Once I identified this "ghost" state, I standardized my environment and ensured the app was talking to the right instance.


3. Designing a Bulletproof Concurrency Test
Running a concurrency test multiple times on the same post was causing "false failures" because of leftover data in Redis from previous runs.

The Fix: Instead of manually clearing Redis every time, I upgraded my spam_test.py script to automatically provision a completely fresh Post via the API for every test run. This made the test "idempotent" and much more reliable for evaluation.

4. Balancing User Experience with Notification Spam
Implementing the 15-minute notification window was tricky because I had to handle both an active cooldown and a growing list of pending notifications without losing any data.

The Fix: I used a combination of a simple Redis Key (for the cooldown timer) and a Redis List (for the pending queue). This kept the logic stateless and ensured that the "CRON Sweeper" could easily summarize the activity without needing complex database queries.


## 📨 Notification Sweeper (Phase 3)
*   **Throttler:** Interactions are checked against a 15-minute cooldown (`user:{id}:notif_cooldown`). If active, notifications are pushed to a Redis List (`user:{id}:pending_notifs`).
*   **CRON Sweeper:** A `@Scheduled` task runs every 5 minutes, popping all pending messages from the Redis List, aggregating them, logging a summarized output, and clearing the list.

---
*Developed for the Grid07 Backend Engineering Assignment.*
