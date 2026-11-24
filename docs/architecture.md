# Digiyak Architecture Overview

Digiyak is a **campus-only walking delivery network** built as a horizontally scalable **Spring Modulith 2.0 Modular Monolith** on **Java 25 LTS** with a decoupled **Next.js Mobile-First PWA** and **MapLibre GL JS + OpenFreeMap**.

## Core Modules (Schema-Per-Module in PostgreSQL 17 + PostGIS)
1. `shared` — Open shared kernel (`Money` in pesewas, `ApiException`, RFC 7807 `GlobalExceptionHandler`, `CodeGenerator`, `AuthenticatedUser`).
2. `identity` — Student/Vendor/Runner/Admin auth, JWT Access/Refresh tokens, MoMo profile, and `no_show_count` anti-ghosting guard.
3. `campus` — PostGIS campus boundary geofence (`ST_Contains`), Zones, Landmarks (Hostels, Lecture Halls, Gates), and Zone-to-Zone walking fee rules.
4. `delivery` — Delivery requests (`MOMO_PREPAID`, `MOMO_PAY_AFTER_DELIVERY`, `PHYSICAL_CASH`, `VENDOR_WALLET`), 4-digit Pickup Code & Delivery PIN, and 1-tap WhatsApp Payment Links.
5. `dispatch` — Runner profiles, atomic optimistic-locked assignment claiming (`409 Conflict` under race conditions), Redis GEO live tracking, Redis Pub/Sub SSE fan-out, and async PostGIS audit trails.
6. `finance` — Double-entry ledger (`SUM(DEBIT) == SUM(CREDIT)`), automated 2-way split (85% Runner / 15% Digiyak), Runner Physical Cash Commission Float (`-GHS 10.00` limit + auto-offset), and pluggable `PaymentGateway` (Mock MoMo + Paystack Ghana MoMo).

See [schema.dbml](schema.dbml) for the full entity-relationship diagram and [team-15-members.md](team-15-members.md) for ownership boundaries.
