# QuickCab Backend (Spring Boot + MySQL)

REST API for the QuickCab frontend. See the [root README](../README.md) for
setup and environment variables.

```bash
cp .env.example .env   # set DB_PASSWORD
mvn spring-boot:run    # http://localhost:8080
mvn test               # unit tests
```

## API Endpoints

All endpoints are under `/api`. Validation failures return `400` with a short
plain-text message; unexpected errors return `500` with a generic message.

### Users
- POST `/api/users/register` body: `{username, email, password, contact}`
- POST `/api/users/login` body: `{username, password}`
- GET `/api/users/{id}`
- PUT `/api/users/{id}` body: `{email, contact, password?}` (blank password keeps the current one)
- DELETE `/api/users/{id}`

### Drivers
- POST `/api/drivers/register` body: `{username, password, contact, vehicleType, vehicleNumber}`
- POST `/api/drivers/login` body: `{username, password}`
- GET / PUT / DELETE `/api/drivers/{id}`

### Rides
- POST `/api/rides/book` body: `{userId, pickup, drop, vehicleType}` (fare is calculated server-side)
- GET `/api/rides/pending/{vehicleType}` (driver polls; OTP hidden)
- POST `/api/rides/{rideId}/accept/{driverId}` (only `PENDING` rides; `409` otherwise)
- POST `/api/rides/{rideId}/deny`
- POST `/api/rides/{rideId}/status` body: `{status: "ON_THE_WAY" | "ARRIVED"}`
- POST `/api/rides/{rideId}/start` body: `{otp: "1234"}` (only from `ARRIVED`)
- POST `/api/rides/{rideId}/cancel` body: `{by: "USER" | "DRIVER"}` (before the ride starts)
- POST `/api/rides/{rideId}/pay` (only from `STARTED`; completes the ride)
- GET `/api/rides/{rideId}` (ride + driver details)
- GET `/api/rides/user/{userId}` (rider history)
- GET `/api/rides/driver/{driverId}` (driver history; OTP hidden)
- GET `/api/rides/locations/{userId}` (past pickups/drops)
- POST `/api/rides/{rideId}/rate` body: `{rating: 1-5}` (only `COMPLETED` rides)

### Chat
- POST `/api/chat/send` body: `{rideId, sender: "USER" | "DRIVER", message}` (max 255 chars)
- GET `/api/chat/{rideId}`
