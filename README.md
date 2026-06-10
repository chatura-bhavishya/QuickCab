# Cab Booking Frontend (React + Vite)

## How to run in VS Code
1. Make sure the Spring Boot backend is running on `http://localhost:8080`.
2. Open this folder in VS Code, then in the terminal:

```
npm install
npm run dev
```

3. Open `http://localhost:5173` in your browser.

## Project structure
- `src/api/api.js` – Axios instance pointing to backend
- `src/App.jsx` – routes + navbar
- `src/pages/` – one file per page (Login, Register, BookRide, RideStatus, DriverDashboard, History, Profile, Home)
- `src/styles.css` – simple white + yellow theme (Rapido-inspired)

## Features
- User & Driver register / login / update / delete
- Book ride with past pickup/drop suggestions (HTML5 datalist)
- Choose vehicle (car / bike / auto)
- Driver accepts/denies requests
- Status flow: PENDING → ACCEPTED → ON_THE_WAY → ARRIVED → (OTP) → STARTED → (payment) → COMPLETED
- 4-digit OTP shown to user, entered by driver
- Live chat between user & driver (polled every 3s)
- Driver details + rating shown to user
- User rates driver after ride completes
- Ride history for both
