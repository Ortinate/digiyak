# Digiyak — Campus-Only Walking Delivery Network

> **Uber Package on foot for university campuses.** Students and campus vendors book side-hustling student **Runners** to walk food, parcels, and everyday items to hostels and lecture halls in minutes — with **MoMo Prepaid**, **MoMo Pay-After-Delivery**, and **Physical Cash** options.

## How It Works (1-2-3 Flow)
1. **Pin & Choose Payment:** Drop a Pickup Pin and Drop-off Hostel/Lecture Hall Pin on the live campus map. Pay the delivery fee via **MoMo Prepaid**, **Pay-After-Delivery MoMo**, or **Physical Cash** (or send a 1-tap WhatsApp payment link).
2. **Walk & Track:** Nearby online student Runners get an instant ping with route, walk time, payment badge, and payout. One Runner claims the gig atomically, confirms pickup with a 4-digit Pickup Code, and walks it over while tracked live at 60fps.
3. **PIN & Automated 2-Way Split:** On 4-digit Delivery PIN handoff, Digiyak settles **~85% Runner / ~15% Digiyak** automatically (or deducts the 15% cut from the Runner's Commission Wallet on Cash deliveries, auto-offsetting on their next MoMo payout).

## Core Architecture & Stack
- **Backend Core:** Java 25 LTS + Spring Boot 4.0 + Spring Modulith 2.0 (Virtual Threads + Transactional Outbox)
- **Database:** PostgreSQL 17 + PostGIS (schema-per-module isolation + campus-only geofencing) + Flyway
- **Real-Time & Geospatial:** Redis 7 (`GEOADD`/`GEOSEARCH` live runner tracking + Pub/Sub SSE backplane)
- **Payments:** Pluggable `PaymentGateway` (built-in Mock MoMo sandbox + Paystack Ghana MoMo) + Double-Entry Ledger
- **Frontend:** Next.js Mobile-First PWA + MapLibre GL JS + OpenFreeMap (\$0 vector map tiles)

## Quick Start (Local Development)

### Prerequisites
- **Java 25 LTS** (Temurin / OpenJDK 25)
- **Docker Desktop** (for PostGIS + Redis)
- **Node.js 20+** and **pnpm**

### 1. Start PostGIS & Redis
```bash
cp .env.example .env
docker compose up -d
```

### 2. Run the Java 25 Backend
```bash
cd backend
./gradlew bootRun
# Swagger UI: http://localhost:8080/swagger-ui.html
# Health:     http://localhost:8080/actuator/health
```

### 3. Run the Next.js Frontend
```bash
cd frontend
pnpm install
pnpm dev
# App: http://localhost:3000
```

## Team Documentation
- [15-Member Ownership & Workstreams](docs/team-15-members.md)
- [Database Schema (DBML for dbdiagram.io)](docs/schema.dbml)
- [Contributing & Branching Rules](CONTRIBUTING.md)
