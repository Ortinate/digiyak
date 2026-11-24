# Digiyak API Contracts (v1)

Interactive OpenAPI 3.1 docs are auto-generated at `http://localhost:8080/swagger-ui.html`.

## Endpoint Ownership Map
- **Identity (Member 2):** `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`, `GET /api/v1/me`, `PATCH /api/v1/me/momo`, `POST /api/v1/super-admin/admins`, `GET /api/v1/super-admin/audit-logs`
- **Campus (Member 3):** `GET /api/v1/campuses/{id}/map-data`, `GET /api/v1/campuses/{id}/landmarks`, `GET /api/v1/campuses/{id}/quote`, `POST /api/v1/admin/campuses/{id}/landmarks`, `POST /api/v1/super-admin/campuses`
- **Delivery (Member 4):** `POST /api/v1/deliveries`, `GET /api/v1/deliveries/{trackingCode}`, `POST /api/v1/deliveries/{id}/payment-link`, `GET /api/v1/pay/{shortCode}`
- **Dispatch (Member 5):** `GET /api/v1/dispatch/assignments/open`, `POST /api/v1/dispatch/assignments/{id}/claim`, `POST /api/v1/dispatch/assignments/{id}/pickup`, `POST /api/v1/dispatch/assignments/{id}/complete`
- **Realtime (Member 6):** `POST /api/v1/dispatch/runners/location`, `GET /api/v1/stream/deliveries/{trackingCode}`, `GET /api/v1/stream/runner/assignments`
- **Finance Ledger (Member 7):** `GET /api/v1/wallets/me`, `GET /api/v1/admin/ledger/audit`, `GET /api/v1/super-admin/ledger/global`
- **MoMo Gateway (Member 8):** `POST /api/v1/webhooks/paystack`, `POST /api/v1/payments/mock/{reference}/approve`
