# Digiyak — 15-Member Team Ownership & Delivery Map

Each member owns a clean, isolated slice of the codebase so all 15 engineers can work simultaneously on feature branches with minimal merge conflicts.

---

## Backend Core — Java 25 + Spring Boot 4 + Spring Modulith 2 (Members 1–8)

| # | Role | Owned Directories & Files | Core Responsibilities |
| :-: | :--- | :--- | :--- |
| **1** | **Platform Lead & Shared Kernel** | `backend/build.gradle.kts`, `settings.gradle.kts`, `application*.yml`, `DigiyakApplication.java`, `com/digiyak/shared/*` | Gradle/Spring Boot 4/Virtual Threads setup, `Money` (pesewas), `ApiException`, `GlobalExceptionHandler` (RFC 7807), `CodeGenerator`, `AuthenticatedUser`, OpenAPI config, PR architecture reviews. |
| **2** | **Identity & Auth Backend** | `com/digiyak/identity/*`, `V2__create_identity_schema.sql` | Student/Vendor/Runner/`CAMPUS_ADMIN`/`SUPER_ADMIN` auth, JWT Access + Refresh tokens, Spring Security filter chain, MoMo wallet profile, `admin_audit_logs`, `no_show_count` & `allow_postpaid` anti-ghosting guard. |
| **3** | **Campus, PostGIS & Pricing Backend** | `com/digiyak/campus/*`, `V3__create_campus_schema.sql` | PostGIS campus boundary polygon (`ST_Contains`), Campus Zones, Landmarks (Hostels, Lecture Halls, Gates), Zone-to-Zone walking fee quote engine, Redis GeoJSON map-data cache. |
| **4** | **Delivery Requests & WhatsApp Pay Links** | `com/digiyak/delivery/*`, `V4__create_delivery_schema.sql` | `DeliveryRequest` aggregate & state machine (MoMo Prepaid, Pay-After-Delivery MoMo, Physical Cash, Vendor Wallet), 4-digit Pickup Code & Delivery PIN, WhatsApp `PaymentLink` (`digiyak.app/pay/{code}`), outbox events. |
| **5** | **Dispatch & Atomic Runner Assignment** | `com/digiyak/dispatch/assignment/*`, `V5__create_dispatch_schema.sql` | `RunnerProfile` (online toggle, accepts cash, cash suspension listener), `DeliveryAssignment`, atomic single-winner claim query (`409 Conflict` under contention), pickup/handoff/completion flow. |
| **6** | **Live Tracking, Redis GEO & SSE** | `com/digiyak/dispatch/realtime/*` | `GEOADD`/`GEOSEARCH` live runner coordinates in Redis, `RedisPubSubBroadcaster`, Virtual-Thread `SseEmitterRegistry` (horizontally scalable tracking & runner gig radar), async PostGIS trail persistence. |
| **7** | **Finance Double-Entry Ledger & Cash Float** | `com/digiyak/finance/ledger/*`, `V6__create_finance_schema.sql` | Immutable double-entry ledger (`SUM(DEBIT) == SUM(CREDIT)`), 2-Way Split (85% Runner / 15% Digiyak), Physical Cash 15% debit, `-GHS 10.00` cash credit limit suspension, auto-offset calculation, zero-sum audit endpoint. |
| **8** | **MoMo Gateway & Payouts** | `com/digiyak/finance/gateway/*` | Pluggable `PaymentGateway`, `MockMoMoGateway` (local sandbox USSD simulator), `PaystackMoMoGateway` (Collection, Pay-After handoff prompt, HMAC-SHA512 webhook verification, Transfers to MTN/Telecel/AirtelTigo MoMo). |

---

## Frontend — Mobile-First Next.js PWA + MapLibre GL JS (Members 9–13)

| # | Role | Owned Directories & Files | Core Responsibilities |
| :-: | :--- | :--- | :--- |
| **9** | **Frontend Core, PWA & Shared Campus Map** | `frontend/package.json`, `next.config.ts`, `manifest.json`, `frontend/src/components/*`, `frontend/src/lib/*` | Next.js App Router PWA shell, Tailwind design tokens, mobile bottom nav, auth store, typed API client, SSE hook, GHS formatter, reusable `<CampusMap />` (MapLibre GL JS + OpenFreeMap). |
| **10** | **Requester & Live Tracking UI** | `frontend/src/app/page.tsx`, `app/track/[code]/*`, `features/request-delivery/*`, `features/live-tracking/*` | Map-first "Send / Request a Runner" (Pickup Pin → Drop-off Hostel/Lecture Hall Pin → 3 Payment Modes selector) + public live 60fps walking tracker & 4-digit Delivery PIN card. |
| **11** | **Vendor Dispatch & WhatsApp Pay Link UI** | `frontend/src/app/vendor/*`, `app/pay/[shortCode]/*`, `features/vendor-dispatch/*` | Vendor 1-tap "Call a Runner" (Prepaid Delivery Wallet + MoMo top-up) & WhatsApp Pay Link generator + zero-login Guest MoMo Checkout page (`/pay/[shortCode]`). |
| **12** | **Runner Gig Radar & Earnings UI** | `frontend/src/app/runner/*`, `features/runner-console/*` | Online toggle, live campus gig radar map (`MoMo Paid`, `Pay After`, `Cash` badges), Commission Float banner & top-up, Pickup Code + Cash Collected + Delivery PIN modal, "Simulate Campus Walk" demo control. |
| **13** | **Admin & Super Admin Ops UI** | `frontend/src/app/admin/*`, `frontend/src/app/super-admin/*`, `features/admin-ops/*` | Campus Ops map (`/admin` for `CAMPUS_ADMIN`), Landmark/Zone/Fee Matrix CRUD, Split % & Cash Credit Limit settings, Double-Entry Ledger Audit, plus Global Multi-Campus Console (`/super-admin` for `SUPER_ADMIN`: onboard campuses, manage Campus Admins, global ledger & audit logs). |

---

## Infrastructure, QA & Documentation (Members 14–15)

| # | Role | Owned Directories & Files | Core Responsibilities |
| :-: | :--- | :--- | :--- |
| **14** | **DevOps, Docker, CI/CD & Base DB** | `docker-compose.yml`, `.env.example`, `.github/*`, `infra/*`, `V1__init_schemas_and_postgis.sql`, `V7__seed_campus_and_demo_data.sql` | PostGIS + Redis Docker stack, Dockerfiles, GitHub Actions CI, `CODEOWNERS`, base schemas + UG Legon / campus seed dataset. |
| **15** | **QA, Automated Testing & Docs** | `backend/src/test/*`, `frontend/tests/*`, `docs/*`, `README.md`, `CONTRIBUTING.md` | `ModularityTests`, Testcontainers integration tests (3 payment modes, cash float auto-offset, 25-thread runner claim concurrency, PostGIS geofence), Playwright E2E flows, DBML & API docs. |

---

## Suggested Sprint 1 Kickoff Order (Zero Blocking)
1. **Day 1 (Foundation — Already Ready from Lead):** Members 1, 9, and 14 finalize shared configs, DB schemas, and UI primitives.
2. **Days 2–5 (Parallel Domain Build):**
   - Backend Members **2, 3, 4, 5, 6, 7, 8** build their isolated modules in parallel against `docs/schema.dbml`.
   - Frontend Members **10, 11, 12, 13** build their screens in parallel using `<CampusMap />` and typed mock/live API client.
3. **Days 6–7 (Integration & Hardening):** Member **15** runs full Testcontainers + E2E verification across all 3 payment flows; Member **1** merges and tags MVP release.
