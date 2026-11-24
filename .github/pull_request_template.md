## Summary
- **Member # / Module:** (e.g., Member 4 — `delivery`)
- **What changed:**

## Architectural & Safety Checklist
- [ ] Changes stay inside my owned module/directories (see `docs/team-15-members.md`)
- [ ] No cross-module `internal` package imports (Spring Modulith `ModularityTests` passes)
- [ ] No cross-schema SQL `JOIN`s added
- [ ] All money fields use integer pesewas (`Money`), never floating point
- [ ] Endpoints return RFC 7807 `ProblemDetail` errors via `ApiException`
- [ ] Unit / integration tests added or updated
