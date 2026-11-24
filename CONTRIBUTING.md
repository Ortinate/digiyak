# Contributing to Digiyak (15-Member Team Rules)

To allow all **15 members** to ship in parallel without merge conflicts or architecture drift, follow these rules strictly.

## 1. Branch Naming & Workflow
1. Always branch from the latest `main`:
   ```bash
   git checkout main && git pull origin main
   git checkout -b feat/m<member-number>-<short-slug>
   # Example: feat/m4-whatsapp-payment-links
   ```
2. Keep PRs focused **strictly inside your owned module/folder** (see [docs/team-15-members.md](docs/team-15-members.md) and [.github/CODEOWNERS](.github/CODEOWNERS)).
3. Use Conventional Commits: `feat(delivery): ...`, `fix(dispatch): ...`, `chore(infra): ...`, `test(finance): ...`.
4. Never push directly to `main`. Open a PR and get review from the Platform Lead (Member 1) + relevant domain reviewer.

## 2. Hard Architectural Rules (Enforced by CI)
- **Spring Modulith Boundaries:** Never import another module's `internal` package. Cross-module calls must go through public API interfaces or Spring Modulith domain events (`@ApplicationModuleListener`). `ModularityTests` will fail CI on violations.
- **No Cross-Schema SQL Joins:** Each module owns its PostgreSQL schema (`identity`, `campus`, `delivery`, `dispatch`, `finance`). Never `JOIN` across schemas — store logical UUID references and call the owning module's public Java service.
- **Money in Pesewas Only:** Always use `com.digiyak.shared.Money` (`long pesewas`). Never use `float` or `double` for money.
- **Zero GPS Writes to Postgres on Heartbeats:** Runner live locations go to **Redis GEO + Pub/Sub**; only sampled audit trails flush asynchronously to `dispatch.delivery_trail_points`.
- **Flyway Migration Ownership:** Never edit an already-merged migration. Use your assigned schema prefix (`V2__` Identity, `V3__` Campus, `V4__` Delivery, `V5__` Dispatch, `V6__` Finance, `V7__` Seed) or timestamped migrations `V20261124_HHMM__<module>_<description>.sql`.

## 3. Definition of Done (Before Opening a PR)
- [ ] Backend compiles & passes `./gradlew test` (including `ModularityTests`).
- [ ] Frontend passes `pnpm --dir frontend build` with zero TypeScript errors.
- [ ] New endpoints are documented via OpenAPI annotations and use `ProblemDetail` (`ApiException`) for errors.
- [ ] Financial and delivery mutations require an `Idempotency-Key` where applicable.
