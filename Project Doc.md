## 📄 PROJECT_DOCS.md

# TrustSphere Banking System

## Table of Contents

1. [Overview](#overview)
2. [Modules](#modules)
3. [API Reference](#api-reference)
4. [Data Models & Schemas](#data-models--schemas)
5. [Security & Auth](#security--auth)
6. [Deployment](#deployment)
7. [Front-End Integration](#front-end-integration)
8. [Changelog](#changelog)

---

## Overview

An end-to-end, Jakarta EE 10 banking system with:

- **EJB** business logic
- **JAX-RS** REST-API
- **JMS** for events
- **JPA** + Hibernate for persistence
- **JWT** auth + RBAC

---

## Modules

| Name               | Artifact         | Purpose                     |
| ------------------ | ---------------- | --------------------------- |
| `trustsphere-core` | `core-1.0.0.jar` | Entities, Enums, DTOs       |
| `trustsphere-ejb`  | `ejb-1.0.0.jar`  | Business Services, DAOs     |
| `trustsphere-rest` | `rest-1.0.0.war` | JAX-RS endpoints            |
| `trustsphere-ear`  | `ear-1.0.0.ear`  | Assembly + Deployment desc. |

---

## API Reference

### Authentication

- `POST /auth/login` → `{ token }`
- **Header**: `Authorization: Bearer <JWT>`

### Accounts

- `POST /api/accounts`
  - Roles: TELLER, ADMIN
  - Body: `AccountDTO`
- `GET /api/accounts/{id}`
  - Roles: USER (own), TELLER, ADMIN
- `PUT /api/accounts/{id}/status?status=ACTIVE`
  - Roles: TELLER, ADMIN

### Transactions

- `POST /api/transactions/transfer`
  - Form-params: `srcId`, `tgtId`, `amount`
  - Roles: USER, TELLER, ADMIN

### Audit

- `GET /api/audit/recent?limit=50`
  - Roles: AUDITOR, ADMIN

### Notifications

- `GET /api/notifications/user/{userId}?page=0&size=50`

### Users

- `POST /api/users`
  - Roles: ADMIN
- `GET /api/users`

_(See OpenAPI schema in `/openapi.json`)_

---

## Data Models & Schemas

### `AccountDTO`

```json5
{
  id: "string",
  accountNumber: "string",
  balance: 123.45,
  status: "ACTIVE",
  userId: "uuid",
  createdAt: "ISO_INSTANT",
  updatedAt: "ISO_INSTANT",
}
```

---

## Security & Auth

- **JWT** (HS256 or RSA) via `Authorization: Bearer`
- **Roles** drive `@RolesAllowed` at EJB & JAX-RS layers
- **Rate Limit**: 60 req/min, 1 000 req/hr per IP
- **CORS**: controlled via `application.properties`

---

## Deployment

1. **Database**: MySQL 8+, schema via Flyway.
2. **App Server**: Payara / GlassFish 6
3. **Resources** (`glassfish-resources.xml`):

   - JDBC pool: `trust_sphere_jdbc`
   - JMS topics: `audit.alert.high`, `bank.txn.created`

4. **EAR**: drop `trustsphere-ear.ear` into `deploy/`.

---

## Front-End Integration

- Use **fetch()** with `Authorization` header.
- Wrap errors by reading `X-Correlation-ID`.
- Example snippet:

  ```js
  async function apiGet(path) {
    const token = await getJwt();
    const res = await fetch(`/api${path}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    if (!res.ok) {
      const err = await res.json();
      console.error(`[${res.headers.get("X-Correlation-ID")}]`, err);
      throw err;
    }
    return res.json();
  }
  ```

---

## Changelog


```
- **v1.0.0**
  - Initial GA release.
```

