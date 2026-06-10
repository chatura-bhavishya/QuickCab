# Cab Booking Backend (Spring Boot + MySQL)

Project structure mirrors the `cabify` reference project:

```
cabapp/
├── pom.xml
├── HELP.md
├── .gitignore
├── .gitattributes
├── .project
└── src/
    ├── main/
    │   ├── java/com/application/cabapp/
    │   │   ├── CabappApplication.java
    │   │   ├── CorsConfig.java
    │   │   ├── controller/
    │   │   │   ├── UserController.java
    │   │   │   ├── DriverController.java
    │   │   │   ├── RideController.java
    │   │   │   └── ChatController.java
    │   │   ├── model/
    │   │   │   ├── User.java
    │   │   │   ├── Driver.java
    │   │   │   ├── Ride.java
    │   │   │   └── ChatMessage.java
    │   │   ├── repository/
    │   │   │   ├── UserRepository.java
    │   │   │   ├── DriverRepository.java
    │   │   │   ├── RideRepository.java
    │   │   │   └── ChatRepository.java
    │   │   └── service/
    │   │       ├── UserService.java
    │   │       ├── DriverService.java
    │   │       ├── RideService.java
    │   │       └── ChatService.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/application/cabapp/
            └── CabappApplicationTests.java
```

## How to run in Eclipse
1. Make sure MySQL is running. Update `src/main/resources/application.properties` with your MySQL username/password (default is `root`/`root`). The DB `cabdb` will be auto-created.
2. In Eclipse: **File → Import → Existing Maven Projects** → select this folder.
3. Right-click `CabappApplication.java` → **Run As → Java Application**.
4. Backend runs at: `http://localhost:8080`

## API Endpoints (quick reference)

### Users
- POST `/api/users/register`  body: `{username,email,password,contact}`
- POST `/api/users/login`     body: `{username,password}`
- GET  `/api/users/{id}`
- PUT  `/api/users/{id}`
- DELETE `/api/users/{id}`

### Drivers
- POST `/api/drivers/register` body: `{username,password,contact,vehicleType,vehicleNumber}`
- POST `/api/drivers/login`
- GET/PUT/DELETE `/api/drivers/{id}`

### Rides
- POST `/api/rides/book` body: `{userId,pickup,drop,vehicleType}`
- GET  `/api/rides/pending/{vehicleType}`        (driver polls)
- POST `/api/rides/{rideId}/accept/{driverId}`
- POST `/api/rides/{rideId}/deny`
- POST `/api/rides/{rideId}/status` body: `{status: "ON_THE_WAY" | "ARRIVED"}`
- POST `/api/rides/{rideId}/start`  body: `{otp: "1234"}`
- POST `/api/rides/{rideId}/pay`
- GET  `/api/rides/{rideId}`         (returns ride + driver details)
- GET  `/api/rides/user/{userId}`    (history)
- GET  `/api/rides/driver/{driverId}`
- GET  `/api/rides/locations/{userId}`  (past pickups/drops)
- POST `/api/rides/{rideId}/rate`    body: `{rating: 4.5}`

### Chat
- POST `/api/chat/send` body: `{rideId,sender:"USER"|"DRIVER",message}`
- GET  `/api/chat/{rideId}`

## Architecture notes
- Layered: Controller → Service → Repository (JPA) → MySQL.
- Same package convention as `cabify`: `com.application.<artifact>`.
- `model`, `repository`, `service` folders mirror cabify; `controller` folder added because cabapp exposes REST endpoints.
- Passwords are stored as plain text for simplicity (mention BCrypt as an improvement).
- CORS enabled for React dev server in `CorsConfig.java`.
