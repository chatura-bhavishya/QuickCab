# QuickCab Architecture

This document explains how a request moves through QuickCab, how the data is
stored, how authentication works, and why the main design choices were made.

## 1. System Overview

```mermaid
flowchart LR
    subgraph Browser
        Pages["pages/"] --> Components["components/ + hooks/"]
        Pages --> Services["services/<br/>apiClient (axios)"]
        Services --> Session["session.js<br/>(localStorage)"]
    end
    Services -- "REST/JSON + Bearer JWT" --> SecurityChain
    subgraph Backend["Spring Boot API"]
        SecurityChain["Spring Security chain<br/>CORS → JwtAuthenticationFilter → authorize"] --> Controller
        Controller["@RestController<br/>@Valid request DTOs"] --> Service["@Service<br/>business rules"]
        Service --> Access["RideAccessService<br/>ownership checks"]
        Service --> Repo["JpaRepository"]
        Access --> Repo
        Advice["GlobalExceptionHandler"] -.-> Controller
    end
    Repo --> MySQL[("MySQL")]
```

| Layer | Responsibility | Never does |
|-------|----------------|-----------|
| Page / component | Rendering, local UI state, client-side validation | Build URLs or talk to axios directly |
| Frontend service | One function per API call; attaches the token | Hold UI state |
| Controller | HTTP mapping, `@Valid`, reads the caller via `@AuthenticationPrincipal` | Business logic, return entities |
| Service | Business rules, ride workflow, ownership checks, transactions | Know about HTTP |
| Repository | Database access via Spring Data JPA (parameterized queries) | String-built SQL |

## 2. Data Flow: Booking a Ride

```mermaid
sequenceDiagram
    participant R as Rider (React)
    participant API as Spring Boot API
    participant DB as MySQL
    participant D as Driver (React)

    R->>API: POST /api/rides/book {pickup, drop, vehicleType} + JWT
    API->>API: Validate DTO, read rider id from JWT
    API->>DB: INSERT ride (PENDING, fare from server, random OTP)
    API-->>R: 201 ride (with OTP)
    loop every 3 s
        D->>API: GET /api/rides/pending
        API-->>D: pending rides for the driver's vehicle type (no OTP)
    end
    D->>API: POST /api/rides/{id}/accept
    API->>DB: UPDATE ... SET driver, ACCEPTED WHERE id=? AND status='PENDING'
    API-->>D: 200 (or 409 if another driver was first)
    loop every 3 s
        R->>API: GET /api/rides/{id}
        API-->>R: ride + driver details
    end
    D->>API: status ON_THE_WAY → ARRIVED
    R-->>D: tells the driver the OTP in person
    D->>API: POST /api/rides/{id}/start {otp}
    D->>API: POST /api/rides/{id}/pay  → COMPLETED
    R->>API: POST /api/rides/{id}/rate {rating}
```

### Ride state machine

```mermaid
stateDiagram-v2
    [*] --> PENDING: rider books
    PENDING --> ACCEPTED: driver accepts (atomic)
    ACCEPTED --> ON_THE_WAY: driver
    ON_THE_WAY --> ARRIVED: driver
    ARRIVED --> STARTED: driver enters correct OTP
    STARTED --> COMPLETED: driver confirms payment
    PENDING --> CANCELLED_BY_USER
    ACCEPTED --> CANCELLED_BY_USER
    ON_THE_WAY --> CANCELLED_BY_USER
    ARRIVED --> CANCELLED_BY_USER
    ACCEPTED --> CANCELLED_BY_DRIVER
    ON_THE_WAY --> CANCELLED_BY_DRIVER
    ARRIVED --> CANCELLED_BY_DRIVER
    COMPLETED --> [*]
```

Every transition is checked on the server. For example, `/status` only allows
`ACCEPTED → ON_THE_WAY` and `ON_THE_WAY → ARRIVED`, and payment is only
accepted from `STARTED`.

## 3. Database

Tables are created by Hibernate (`ddl-auto=update`). Relationships are by id
columns (no JPA associations), which keeps entities simple and avoids lazy-loading
surprises when they are mapped to DTOs.

```mermaid
erDiagram
    USERS ||--o{ RIDES : books
    DRIVERS ||--o{ RIDES : "is assigned"
    RIDES ||--o{ CHAT_MESSAGES : has

    USERS {
        bigint id PK
        varchar username UK
        varchar email
        varchar password "BCrypt hash"
        varchar contact
    }
    DRIVERS {
        bigint id PK
        varchar username UK
        varchar password "BCrypt hash"
        varchar contact
        varchar vehicle_type "car | bike | auto"
        varchar vehicle_number
        double rating
        int rating_count
    }
    RIDES {
        bigint id PK
        bigint user_id FK
        bigint driver_id FK "null until accepted"
        varchar pickup
        varchar drop "quoted: reserved word"
        varchar vehicle_type
        varchar status "RideStatus enum"
        varchar otp "4 digits"
        bit paid
        double amount "server-calculated fare"
        bit driver_rated
        datetime created_at
    }
    CHAT_MESSAGES {
        bigint id PK
        bigint ride_id FK
        varchar sender "USER | DRIVER"
        varchar message
        datetime sent_at
    }
```

Riders and drivers are separate tables, so an id alone is ambiguous. That is why
the JWT always carries both the id and the role.

## 4. Authentication and Authorization (JWT)

```mermaid
sequenceDiagram
    participant B as Browser
    participant S as Spring Boot
    participant DB as MySQL

    B->>S: POST /api/users/login {username, password}
    S->>DB: find user by username
    S->>S: BCrypt.matches(password, hash)
    S->>S: sign JWT {sub: id, role: USER, exp} with HS256 + JWT_SECRET
    S-->>B: {token, expiresInSeconds, account}
    B->>B: store session in localStorage

    B->>S: GET /api/rides/42  (Authorization: Bearer <token>)
    S->>S: JwtAuthenticationFilter verifies signature + expiry
    S->>S: SecurityContext ← AuthPrincipal(id, role)
    S->>S: RideAccessService: is caller the ride's rider or assigned driver?
    S-->>B: 200 ride  |  401 bad/missing token  |  403 not yours
```

**Authentication** (who are you?)
- Passwords are hashed with BCrypt. Accounts created before hashing are upgraded on their next successful login.
- Login returns a signed JWT: subject = account id, claim `role` = `USER` or `DRIVER`, expiry = `JWT_EXPIRATION_MINUTES`.
- The signing key comes from `JWT_SECRET`. The app refuses to start without it or if it is shorter than 32 bytes.
- `JwtAuthenticationFilter` validates the token on each request. An invalid token simply leaves the request anonymous, and `SecurityConfig` then returns 401 for anything except register and login.

**Authorization** (what may you do?)
- The caller's identity is **only** taken from the token. Endpoints use `/me`, `/mine`, `/pending` instead of accepting account ids, and body fields such as `userId`, `sender` or `by` are ignored.
- `RideAccessService` is the single place that decides who may touch a ride: `getForRider`, `getForAssignedDriver` and `getForParticipant`.
- Responses are role-aware: the OTP is only returned to the rider.

**Frontend side**
- `apiClient` adds `Authorization: Bearer …` to every request. On a 401 it clears the session and redirects to the login page.
- `RequireAuth` guards routes for a better UX. It is not the security boundary; the API is.
- Logout deletes the token locally. Tokens are stateless and expire on their own.

## 5. Error Handling

All errors use one JSON shape:

```json
{ "status": 400, "error": "Enter a valid email address", "fieldErrors": { "email": "Enter a valid email address" } }
```

- Services throw `ApiException` (400/401/403/404/409) with a user-safe message.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) maps those, validation errors, Spring MVC errors and anything unexpected. Unexpected errors are logged on the server and returned as a generic 500.
- The security layer writes the same shape for 401/403.
- The frontend's `getErrorMessage` only displays short, plain messages from this shape and falls back to a friendly default otherwise. An `ErrorBoundary` catches rendering crashes.

## 6. Key Design Decisions

| Decision | Why | Trade-off |
|----------|-----|-----------|
| **Stateless JWT** instead of server sessions | No session store; any API instance can serve any request (easy horizontal scaling); simple for a SPA | Tokens can't be revoked before expiry; mitigated by a short-ish expiry |
| **Token in `localStorage`** | Simple and works across tabs; no cookies means no CSRF | Readable by injected scripts if an XSS bug existed; React escapes output and there is no `dangerouslySetInnerHTML`. httpOnly cookies are a future improvement |
| **Identity from token, not from URLs** | Removes the whole class of "change the id in the URL" (IDOR) bugs | Endpoints are less generic (`/me`, `/mine`) |
| **Central `RideAccessService`** | One place to review and test ownership rules | Services depend on it |
| **DTOs (Java records)** for requests and responses | Validation lives on the request; responses can't leak fields like passwords; API is decoupled from the schema | Some mapping code |
| **Atomic conditional UPDATE** to accept rides | Two drivers clicking Accept at once can't both win, without locks or version columns | A custom JPQL query |
| **Enums stored as strings** | Readable data; existing rows stayed compatible when the code moved from strings to enums | Renaming a constant needs a data migration |
| **Polling every 3 s** | Simple, works everywhere, no extra infrastructure | More requests than WebSockets; fine at this scale |
| **Server-side fare and OTP** | Clients can't choose their price; OTP comes from `SecureRandom` | Flat fares only for now |
| **Frontend services layer + hooks** | Pages stay declarative; API changes touch one file; logic is unit-testable | A few more files |
| **H2 for the integration test** | The full Spring context and every rule are tested in CI without a database server | H2 is not MySQL; Testcontainers would be closer to production |
