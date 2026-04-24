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

## 📨 Notification Sweeper (Phase 3)
*   **Throttler:** Interactions are checked against a 15-minute cooldown (`user:{id}:notif_cooldown`). If active, notifications are pushed to a Redis List (`user:{id}:pending_notifs`).
*   **CRON Sweeper:** A `@Scheduled` task runs every 5 minutes, popping all pending messages from the Redis List, aggregating them, logging a summarized output, and clearing the list.

---
*Developed for the Grid07 Backend Engineering Assignment.*
