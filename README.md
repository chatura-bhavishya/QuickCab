# QuickCab

QuickCab is a full-stack cab booking web app. Riders book a car, bike or auto,
track the ride live, chat with their driver and rate the trip. Drivers see
nearby requests for their vehicle type, accept rides, verify the rider with an
OTP and complete the trip.

## Features

- **Rider and driver accounts**: registration, login, profile editing and account deletion
- **Ride booking** with pickup/drop suggestions from your past trips
- **Vehicle choice** (Car, Bike, Auto) with a server-calculated flat fare
- **Driver dashboard** showing pending requests for the driver's vehicle type, with accept / deny
- **Live ride tracking** through `PENDING → ACCEPTED → ON_THE_WAY → ARRIVED → STARTED → COMPLETED`
- **OTP verification**: the ride only starts when the driver enters the rider's 4-digit OTP
- **In-ride chat** between rider and driver
- **Cancellation** by rider or driver before the ride starts
- **Driver ratings** (1–5 stars) after a completed ride
- **Ride history** for riders and drivers

### Security notes

- Passwords are hashed with BCrypt and never returned by the API or stored in the browser
- All input is validated on both the client and the server (formats, length limits, allowed values)
- Fares, ride state and OTPs are controlled by the server; the ride workflow only allows valid status transitions
- OTPs are generated with `SecureRandom` and hidden from driver-facing endpoints
- Errors return friendly messages without stack traces
- Database credentials come from environment variables, never from source control

## Tech Stack

| Layer    | Technology |
|----------|------------|
| Frontend | React 18, React Router 6, Axios, Vite 5, plain CSS |
| Backend  | Java 17, Spring Boot 3 (Web, Data JPA), Spring Security Crypto (BCrypt) |
| Database | MySQL 8 (schema managed by Hibernate) |
| Tooling  | ESLint 9, Maven, JUnit 5 |

## Folder Structure

```
QuickCab/
├── index.html               # Vite entry HTML
├── src/                     # React frontend
│   ├── api/api.js           # Axios instance (reads VITE_API_BASE_URL)
│   ├── auth/session.js      # Logged-in user/driver stored in localStorage
│   ├── components/          # RequireAuth route guard
│   ├── pages/               # One component per screen
│   ├── utils/               # Form validation and error-message helpers
│   ├── constants.js         # Shared constants (statuses, vehicles, limits)
│   └── styles.css
├── backend/                 # Spring Boot API
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/application/cabapp/
│       │   ├── controller/  # REST endpoints (thin)
│       │   ├── service/     # Business logic
│       │   ├── model/       # JPA entities and constants
│       │   ├── repository/  # Spring Data repositories
│       │   ├── security/    # Password hashing
│       │   ├── validation/  # Server-side input validation
│       │   └── exception/   # Global error handler
│       ├── main/resources/application.properties
│       └── test/            # Unit tests
├── screenshots/
├── .env.example             # Frontend environment template
└── backend/.env.example     # Backend environment template
```

## Running Locally

### Prerequisites

- Node.js 18+ and npm
- Java 17+ and Maven 3.9+
- MySQL 8 running locally

### 1. Backend

```bash
cd backend
cp .env.example .env        # then set DB_PASSWORD (and DB_USERNAME if not root)
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. The `cabdb` database and its tables
are created automatically on first run.

> Run the command from inside `backend/` so `backend/.env` is picked up. You can
> also export the variables in your shell or IDE run configuration instead.

### 2. Frontend

```bash
# from the project root
cp .env.example .env        # optional; defaults to http://localhost:8080/api
npm install
npm run dev
```

Open `http://localhost:5173`.

### Useful scripts

| Command | What it does |
|---------|--------------|
| `npm run dev` | Start the Vite dev server |
| `npm run build` | Production build into `dist/` |
| `npm run lint` | Run ESLint |
| `cd backend && mvn test` | Run backend unit tests |

## Environment Variables

Copy the templates and fill in your values. Real `.env` files are git-ignored.

**Frontend** ([`.env.example`](.env.example))

| Variable | Description | Default |
|----------|-------------|---------|
| `VITE_API_BASE_URL` | Base URL of the backend API | `http://localhost:8080/api` |

**Backend** ([`backend/.env.example`](backend/.env.example))

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | JDBC URL of the MySQL database | `jdbc:mysql://localhost:3306/cabdb?...` |
| `DB_USERNAME` | MySQL user | `root` |
| `DB_PASSWORD` | MySQL password | **required** |
| `PORT` | HTTP port of the API | `8080` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated origins allowed to call the API | `http://localhost:5173,http://localhost:3000,http://127.0.0.1:*` |
| `SHOW_SQL` | Log generated SQL (debugging only) | `false` |

## API

See [backend/README.md](backend/README.md) for the endpoint reference.

## Screenshots

<!-- Replace or add screenshots here. -->

| | | |
|---|---|---|
| ![QuickCab screenshot 1](screenshots/4e4e8ace-3881-4eb7-a6ed-f4d8fae09498.jpg) | ![QuickCab screenshot 2](screenshots/a7b9d60e-6468-4375-8584-50c0376312a7.jpg) | ![QuickCab screenshot 3](screenshots/cd664a61-68aa-4814-8188-3a64deb8d1de.jpg) |

## Known Limitations

- The API does not yet issue auth tokens, so the server cannot verify *who* is
  making a request. The frontend guards pages, but a direct API caller can still
  read or modify another account by id. Adding JWT/session authentication is the
  next planned improvement.
