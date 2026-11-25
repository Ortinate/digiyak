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

## Team Workflow (15 Members)

Digiyak is built by a 15-person team working in parallel. Each member owns one slice of the codebase, so most PRs touch only your own folder and merge conflicts stay rare.

### Who Owns What

| # | Area | Folder |
| :-: | :--- | :--- |
| 1 | Platform Lead & Shared Kernel | `backend/src/main/java/com/digiyak/shared/`, Gradle & app config |
| 2 | Identity & Auth | `backend/.../identity/` |
| 3 | Campus, PostGIS & Pricing | `backend/.../campus/` |
| 4 | Delivery Requests & Pay Links | `backend/.../delivery/` |
| 5 | Dispatch & Runner Assignment | `backend/.../dispatch/assignment/` |
| 6 | Live Tracking (Redis GEO & SSE) | `backend/.../dispatch/realtime/` |
| 7 | Finance Ledger & Cash Float | `backend/.../finance/ledger/` |
| 8 | MoMo Gateway & Payouts | `backend/.../finance/gateway/` |
| 9 | Frontend Core, PWA & Campus Map | `frontend/src/components/`, `frontend/src/lib/` |
| 10 | Requester & Live Tracking UI | `frontend/src/app/page.tsx`, `app/track/`, `features/request-delivery/`, `features/live-tracking/` |
| 11 | Vendor Dispatch & Pay Link UI | `frontend/src/app/vendor/`, `app/pay/`, `features/vendor-dispatch/` |
| 12 | Runner Console UI | `frontend/src/app/runner/`, `features/runner-console/` |
| 13 | Admin & Super Admin UI | `frontend/src/app/admin/`, `app/super-admin/`, `features/admin-ops/` |
| 14 | DevOps, Docker, CI & Seed Data | `docker-compose.yml`, `infra/`, `.github/`, `V1__` & `V7__` migrations |
| 15 | QA, Tests & Docs | `backend/src/test/`, `frontend/tests/`, `docs/` |

Full responsibilities are in [docs/team-15-members.md](docs/team-15-members.md). Reviewers are assigned automatically from [.github/CODEOWNERS](.github/CODEOWNERS).

### Daily Flow

1. **Start from the latest `main`:**
   ```bash
   git checkout main
   git pull origin main
   ```
2. **Create a branch** named `<type>/m<your-number>-<short-description>`:
   ```bash
   git checkout -b feat/m4-whatsapp-pay-links
   ```
   Types: `feat`, `fix`, `docs`, `test`, `chore`, `refactor`.
3. **Work inside your own folder.** If you need something from another module, call its public API (for example `IdentityModuleApi` or `DeliveryModuleApi`) or listen to its events. Never import another module's `internal` package.
4. **Commit with Conventional Commits:**
   ```bash
   git commit -m "feat(delivery): add payment link expiry check"
   ```
5. **Run the checks locally before pushing** (CI runs the same ones):
   ```bash
   # Backend
   cd backend && ./gradlew clean check        # Windows: .\gradlew.bat clean check

   # Frontend
   cd frontend && pnpm lint && pnpm typecheck && pnpm build
   ```
6. **Push your branch:**
   ```bash
   git push -u origin feat/m4-whatsapp-pay-links
   ```

### Opening a Pull Request

1. Go to https://github.com/Ortinate/digiyak/pulls and click **New pull request**, or open the "Compare & pull request" banner GitHub shows after you push.
2. Set **base: `main`** and **compare: your branch**.
3. Fill in the PR template: your member number, the module, what changed, and tick the checklist.
4. Keep PRs small and focused on one feature or fix. Large PRs are slow to review.
5. Wait for **green CI** (Backend CI and/or Frontend CI) and **one approval** from a CODEOWNER (the Platform Lead for backend, Member 9 for frontend).
6. Address review comments with new commits on the same branch. Don't force-push once review has started.
7. Merge with **Squash and merge**, then delete the branch.

### Rules That CI Enforces

- `ModularityTests` fails the build if a module reaches into another module's `internal` package.
- Java compiles with `-Xlint:all -Werror`, so any compiler warning breaks the build.
- Frontend must pass ESLint, TypeScript strict mode, and a production build.

### Team Rules

- Never push directly to `main`.
- Never edit a Flyway migration that's already merged. Add a new one instead.
- Money is always integer pesewas (`Money`), never `float` or `double`.
- No SQL `JOIN`s across schemas (`identity`, `campus`, `delivery`, `dispatch`, `finance`).
- Ask in the team channel before changing anything outside your folder.

More detail: [CONTRIBUTING.md](CONTRIBUTING.md).

### Team Documentation
- [15-Member Ownership & Workstreams](docs/team-15-members.md)
- [Database Schema (DBML for dbdiagram.io)](docs/schema.dbml)
- [Architecture Overview](docs/architecture.md)
- [API Contracts](docs/api-contracts.md)
- [Contributing, Branching & PR Rules](CONTRIBUTING.md)
