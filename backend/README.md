# QuickCab Backend (Spring Boot + MySQL)

REST API for the QuickCab frontend. See the [root README](../README.md) for
setup and environment variables, and [ARCHITECTURE.md](../ARCHITECTURE.md) for
how authentication and ownership checks work.

```bash
cp .env.example .env      # set DB_PASSWORD and JWT_SECRET
./mvnw spring-boot:run    # http://localhost:8080
./mvnw test               # unit, web-layer and H2 integration tests
```

## Conventions

- All endpoints are under `/api` and exchange JSON.
- Only register and login are public. Everything else needs `Authorization: Bearer <token>`.
- The caller is always taken from the token. No endpoint accepts a rider or driver id.
- Errors look like `{ "status": 400, "error": "message", "fieldErrors": { ... } }`:
  `400` invalid input or action not allowed now, `401` missing/invalid token or wrong
  credentials, `403` not your resource or wrong role, `404` not found, `409` conflict,
  `429` too many failed logins or OTP attempts (try again later).

## Endpoints

### Riders (`/api/users`)
| Method | Path | Auth | Body / notes |
|--------|------|------|--------------|
| POST | `/register` | public | `{username, email, password, contact}` → 201 |
| POST | `/login` | public | `{username, password}` → `{token, expiresInSeconds, account}` |
| GET | `/me` | rider | own profile |
| PUT | `/me` | rider | `{email, contact, password?}` (blank password keeps the current one) |
| DELETE | `/me` | rider | → 204 |

### Drivers (`/api/drivers`)
| Method | Path | Auth | Body / notes |
|--------|------|------|--------------|
| POST | `/register` | public | `{username, password, contact, vehicleType, vehicleNumber}` → 201 |
| POST | `/login` | public | `{username, password}` → `{token, expiresInSeconds, account}` |
| GET / PUT / DELETE | `/me` | driver | PUT: `{contact, vehicleType, vehicleNumber, password?}` |

### Rides (`/api/rides`)
| Method | Path | Who | Notes |
|--------|------|-----|-------|
| POST | `/estimate` | anyone logged in | `{vehicleType, pickupLat, pickupLng, dropLat, dropLng}` → `{distanceKm, fare}` |
| POST | `/book` | rider | `{pickup, drop, vehicleType}` plus optional pins (`pickupLat`, `pickupLng`, `dropLat`, `dropLng`; all four or none) → 201. Fare and OTP are set by the server |
| GET | `/locations` | rider | past pickups/drops for autocomplete |
| POST | `/{id}/rate` | the ride's rider | `{rating: 1-5}`; only once, only when `COMPLETED` |
| GET | `/pending` | driver | `PENDING` rides for the driver's vehicle type (no OTP) |
| POST | `/{id}/accept` | driver | only while `PENDING` and matching vehicle type; `409` if already taken |
| POST | `/{id}/deny` | driver | → 204 (ride stays available to others) |
| POST | `/{id}/status` | assigned driver | `{status: "ON_THE_WAY" \| "ARRIVED"}`, in order |
| POST | `/{id}/start` | assigned driver | `{otp: "1234"}`; only from `ARRIVED` |
| POST | `/{id}/pay` | assigned driver | only from `STARTED`; completes the ride |
| POST | `/{id}/cancel` | rider or assigned driver | before `STARTED`; who cancelled comes from the token |
| GET | `/{id}` | rider or assigned driver | `{ride, driver}`; OTP only for the rider |
| GET | `/mine` | anyone logged in | the caller's ride history |

### Chat (`/api/chat`)
| Method | Path | Who | Notes |
|--------|------|-----|-------|
| POST | `/send` | rider or assigned driver | `{rideId, message}` (max 255 chars) → 201; sender from token |
| GET | `/{rideId}` | rider or assigned driver | messages in order |

## Live updates (WebSocket)

Connect to `ws://<host>/ws` with STOMP and send `Authorization: Bearer <token>` in the CONNECT frame.

| Topic | Who can subscribe | Message |
|-------|-------------------|---------|
| `/topic/rides/{id}` | that ride's rider or assigned driver | `{type: "RIDE" or "CHAT", rideId}` |
| `/topic/pending/{car,bike,auto}` | drivers with that vehicle type | `{type: "RIDE", rideId}` |

Messages only say *what* changed. Fetch the details through the REST endpoints above.

