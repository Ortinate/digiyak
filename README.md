# Digiyak — Campus-Only Walking Delivery Network

> **Uber Package on foot for university campuses.** Students and campus vendors book side-hustling student **Runners** to walk food, parcels, and everyday items to hostels and lecture halls in minutes — with **MoMo Prepaid**, **MoMo Pay-After-Delivery**, and **Physical Cash** options.

## Tech Stack
- **Backend Core:** Java 25 LTS + Spring Boot 4.0 + Spring Modulith 2.0 + Gradle 9.1.0 Wrapper
- **Database:** PostgreSQL 17 + PostGIS (schema-per-module isolation + campus geofencing) + Flyway
- **Real-Time & Geospatial:** Redis 7 (`GEOADD`/`GEOSEARCH` live runner GPS + Pub/Sub SSE)
- **Payments:** Pluggable `PaymentGateway` (Mock MoMo sandbox + Paystack Ghana MoMo) + Double-Entry Ledger
- **Frontend:** Next.js 15 Mobile-First PWA + TypeScript + Tailwind CSS v4 + MapLibre GL JS (OpenFreeMap)

---

## Prerequisites

Install these before running the project:

| Tool | Required Version | Verify Command |
| :--- | :--- | :--- |
| **Java (JDK)** | **Java 25 LTS** (Eclipse Temurin recommended) | `java -version` |
| **Docker Desktop** | Latest (for PostGIS & Redis) | `docker --version` |
| **Node.js** | **Node 20+** (22 LTS recommended) | `node -v` |
| **pnpm** | **pnpm 10+** (`npm install -g pnpm`) | `pnpm -v` |

> **Windows Note (`JAVA_HOME`):** Make sure `JAVA_HOME` points to your Java 25 installation before running Gradle:
> ```powershell
> setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot"
> ```
> Then reopen your terminal / reload VS Code.

---

## Step 1 — Clone & Create `.env`

```bash
git clone https://github.com/Ortinate/digiyak.git
cd digiyak
```

Copy the environment template:

- **Windows (PowerShell):**
  ```powershell
  Copy-Item .env.example .env
  ```
- **macOS / Linux:**
  ```bash
  cp .env.example .env
  ```

---

## Step 2 — Start PostgreSQL (PostGIS) & Redis

Make sure **Docker Desktop** is running, then start the database and Redis containers from the repository root:

```bash
docker compose up -d
```

Verify both containers are healthy:

```bash
docker compose ps
```

Useful Docker commands:
```bash
docker compose logs -f postgres   # View PostGIS logs
docker compose down               # Stop containers (keeps DB data)
docker compose down -v            # Stop & wipe DB volumes for a clean reset
```

---

## Step 3 — Run the Backend (Java 25 + Spring Boot 4)

You do **not** need to install Gradle globally — use the included Gradle 9.1.0 wrapper (`gradlew` / `gradlew.bat`).

### Run the API Server (`http://localhost:8080`)
- **Windows (PowerShell):**
  ```powershell
  cd backend
  .\gradlew.bat bootRun
  ```
- **macOS / Linux:**
  ```bash
  cd backend
  ./gradlew bootRun
  ```

### Run Backend Tests & Spring Modulith Boundary Verification
- **Windows (PowerShell):**
  ```powershell
  cd backend
  .\gradlew.bat test
  ```
- **macOS / Linux:**
  ```bash
  cd backend
  ./gradlew test
  ```

### Backend URLs (Once Running)
- **Swagger / OpenAPI UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON Spec:** http://localhost:8080/v3/api-docs
- **Health Check:** http://localhost:8080/actuator/health

---

## Step 4 — Run the Frontend (Next.js PWA)

Open a **second terminal** from the repository root:

```bash
cd frontend
pnpm install
pnpm dev
```

The frontend runs at **http://localhost:3000**.

### Frontend Verification Commands
Run these before opening any PR:

```bash
cd frontend
pnpm lint        # ESLint (0 warnings/errors required)
pnpm typecheck   # TypeScript strict check (tsc --noEmit)
pnpm build       # Production Next.js build check
```

### App Routes
| Route | Role / Screen |
| :--- | :--- |
| `/` | Student "Send / Request a Runner" (Map pin-drop + 3 payment modes) |
| `/track/[code]` | Live 60fps Campus Walking Tracker + 4-Digit Delivery PIN |
| `/vendor` | Vendor Dispatch Desk (Wallet dispatch & WhatsApp Pay Link generator) |
| `/pay/[shortCode]` | Guest 1-Tap WhatsApp Pay-for-Delivery & Pay-After-Delivery MoMo checkout |
| `/runner` | Runner Gig Radar Map, Commission Float banner, Pickup/Delivery PIN pad |
| `/admin` | Campus Ops Map, Landmarks/Fee Matrix & Ledger Audit (`CAMPUS_ADMIN`) |
| `/super-admin` | Global Multi-Campus Console, Campus Admin Management & Audit Logs (`SUPER_ADMIN`) |

---

## Troubleshooting

| Issue | Fix |
| :--- | :--- |
| `ERROR: JAVA_HOME is set to an invalid directory` | Update `JAVA_HOME` to your JDK 25 folder (see Prerequisites above) and reopen your terminal. |
| Backend fails to connect to PostgreSQL/Redis | Confirm Docker Desktop is open and run `docker compose up -d && docker compose ps`. |
| First `./gradlew` run is slow | Normal on the first run — Gradle downloads Gradle 9.1.0 and caches Spring Boot 4 / PostGIS dependencies locally; subsequent builds take seconds. |
| Port `5432`, `6379`, `8080`, or `3000` already in use | Stop any local Postgres/Redis/Node/Java service using that port or adjust `SERVER_PORT` in `.env`. |
| VS Code shows red imports in Java files | Run `.\gradlew.bat test` once, then run **Java: Clean Java Language Server Workspace** in VS Code Command Palette. |

---

## Team Documentation (15-Member Workflow)
- [15-Member Ownership & Workstreams](docs/team-15-members.md)
- [Database Schema (DBML for dbdiagram.io)](docs/schema.dbml)
- [Architecture Overview](docs/architecture.md)
- [API Contracts](docs/api-contracts.md)
- [Contributing, Branching & PR Rules](CONTRIBUTING.md)
