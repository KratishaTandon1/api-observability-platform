# API Monitoring & Observability Platform

A full‑stack observability solution designed to track, analyze, and
visualize API metrics across microservices. Built with **Spring Boot
(Kotlin)**, **Next.js**, and a **Dual‑MongoDB Architecture**.

## 🚀 Features

### 1. API Tracking Client (Library)

-   **Interceptor-based**: Automatically captures request/response
    metrics (Method, Status, Latency, Size).
-   **Rate Limiting**: Token Bucket algorithm (Guava RateLimiter) ---
    non-blocking `tryAcquire()` ensures performance.
-   **Non-blocking Logging**: Metrics sent asynchronously to the
    collector.

### 2. Central Collector Service (Backend)

-   **Dual-Database Architecture**:
    -   High-volume logs stored separately from metadata.
-   **Alert Generation**:
    -   Slow APIs (Latency \> 500ms)
    -   Broken APIs (5xx)
    -   Rate Limit Hits (429)
-   **Security**: JWT Authentication for dashboard access.
-   **Concurrency Control**: Optimistic locking using `@Version`.

### 3. Observability Dashboard (Frontend)

-   **Live Monitoring** of slow, broken, and rate-limited APIs.
-   **Interactive Explorer**: Filter logs by Date, Status, Service,
    Search.
-   **Visual Analytics**: Traffic graphs, top slowest endpoints.
-   **Issue Management**: Mark incidents as "Resolved".

------------------------------------------------------------------------

## 🏗 Architecture & Design Decisions

### 1. Dual MongoDB Strategy

  -------------------------------------------------------------------------
  Database          Port        Collection              Purpose
  ----------------- ----------- ----------------------- -------------------
  Primary (Logs DB) 27017       `api_logs`              High-volume raw log
                                                        storage

  Secondary         27018       `alerts`, `users`       Alerts & user
  (Metadata DB)                                         accounts
  -------------------------------------------------------------------------

Reason: Log spikes must **not** impact login or alert management.

### 2. Rate Limiting

-   Implemented via **Guava RateLimiter**.
-   `tryAcquire()` ensures no blocking.
-   If denied, request continues normally but logs `rateLimitHit=true`.

### 3. Concurrency Handling

-   Multiple admins may attempt to resolve the same alert.
-   **Optimistic Locking** with `@Version` prevents lost updates.

------------------------------------------------------------------------

## 🛠 Tech Stack

-   **Backend**: Kotlin, Spring Boot 3.2, Spring Security (JWT)
-   **Frontend**: Next.js 14, Tailwind CSS, Recharts, Lucide React
-   **Database**: MongoDB (x2)
-   **Infra**: Docker Compose

------------------------------------------------------------------------

## ⚡ Setup & Run Instructions

### Prerequisites

-   Docker Desktop
-   Java 17+
-   Node.js 18+
-   Python 3 (Optional for fake data)

------------------------------------------------------------------------

### **Step 1: Start Databases**

``` bash
docker compose up -d
```

### **Step 2: Start Backend**

``` bash
cd central-collector
./gradlew bootRun
```

Backend: http://localhost:8080

### **Step 3: Start Frontend**

``` bash
cd nextjs-dashboard
npm run dev
```

Dashboard: http://localhost:3000

### **Step 4: Optional --- Generate Test Traffic**

``` bash
python generate_data.py
```

------------------------------------------------------------------------

## 🖥️ Usage Guide

### Login Credentials

-   **Username:** `admin`
-   **Password:** `admin123`

### How to Use

-   Explore logs using filters.
-   Analyze top slow endpoints.
-   Resolve issues directly from the dashboard.
-   Refresh to confirm persistence.

------------------------------------------------------------------------

## 📂 Database Schemas

### 1. ApiLog (Primary DB)

``` json
{
  "_id": "ObjectId",
  "serviceName": "order-service",
  "endpoint": "/api/pay",
  "method": "POST",
  "status": 500,
  "duration": 850,
  "timestamp": "ISODate",
  "rateLimitHit": false
}
```

### 2. Alert (Secondary DB)

``` json
{
  "_id": "ObjectId",
  "logId": "Ref(ApiLog)",
  "issueType": "BROKEN",
  "resolved": true,
  "resolvedAt": "ISODate",
  "version": 1
}
```
