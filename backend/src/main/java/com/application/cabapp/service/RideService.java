package com.application.cabapp.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.application.cabapp.model.Driver;
import com.application.cabapp.model.Ride;
import com.application.cabapp.model.RideStatus;
import com.application.cabapp.model.VehicleType;
import com.application.cabapp.repository.DriverRepository;
import com.application.cabapp.repository.RideRepository;
import com.application.cabapp.repository.UserRepository;
import com.application.cabapp.validation.InputValidator;

/**
 * RideService — owns ALL business logic for the Ride entity, including
 * cross-entity workflows (driver assignment, rating updates) so that
 * controllers stay as thin HTTP adapters.
 */
@Service
public class RideService {

    @Autowired
    private RideRepository rideRepo;

    @Autowired
    private DriverRepository driverRepo;

    @Autowired
    private UserRepository userRepo;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int OTP_MIN = 1000;
    private static final int OTP_RANGE = 9000; // OTPs are 4 digits: 1000-9999

    // Flat fares per vehicle type (INR).
    private static final double FARE_BIKE = 60.0;
    private static final double FARE_AUTO = 90.0;
    private static final double FARE_CAR = 150.0;
    private static final double FARE_DEFAULT = 100.0;

    /**
     * Status changes a driver may make through the generic /status endpoint.
     * STARTED (needs OTP), COMPLETED (needs payment) and cancellation have
     * their own endpoints with their own checks.
     */
    private static final Map<String, String> ALLOWED_STATUS_TRANSITIONS = Map.of(
            RideStatus.ON_THE_WAY, RideStatus.ACCEPTED,
            RideStatus.ARRIVED, RideStatus.ON_THE_WAY);

    // ---------- Core ride operations ----------

    public Ride bookRide(Ride ride) {
        // Server owns every field except userId, pickup, drop and vehicleType.
        ride.setId(null);
        ride.setDriverId(null);
        ride.setPaid(false);
        ride.setCreatedAt(LocalDateTime.now());
        ride.setStatus(RideStatus.PENDING);
        ride.setOtp(String.valueOf(OTP_MIN + RANDOM.nextInt(OTP_RANGE)));
        ride.setAmount(calculateFare(ride.getVehicleType()));
        return rideRepo.save(ride);
    }

    /** Simple flat fare per vehicle type. Always computed server-side. */
    private double calculateFare(String vehicleType) {
        if (vehicleType == null) return FARE_DEFAULT;
        switch (vehicleType.toLowerCase()) {
            case VehicleType.BIKE: return FARE_BIKE;
            case VehicleType.AUTO: return FARE_AUTO;
            case VehicleType.CAR:  return FARE_CAR;
            default:               return FARE_DEFAULT;
        }
    }

    public List<Ride> getPendingByVehicle(String vehicleType) {
        return withoutOtp(rideRepo.findByStatusAndVehicleType(RideStatus.PENDING, vehicleType));
    }

    public Ride updateStatus(Long rideId, String status) {
        return rideRepo.findById(rideId).map(r -> {
            r.setStatus(status);
            return rideRepo.save(r);
        }).orElse(null);
    }

    public Ride startRide(Long rideId, String otp) {
        Ride r = rideRepo.findById(rideId).orElse(null);
        if (r == null || !RideStatus.ARRIVED.equals(r.getStatus())) return null;
        if (r.getOtp() == null || !r.getOtp().equals(otp)) return null;
        r.setStatus(RideStatus.STARTED);
        return rideRepo.save(r);
    }

    /**
     * Cancel a ride. `by` must be "USER" or "DRIVER". Returns null if the ride
     * does not exist or is in a status that no longer allows cancellation
     * (e.g. STARTED, COMPLETED, already cancelled).
     */
    public Ride cancelRide(Long rideId, String by) {
        Ride r = rideRepo.findById(rideId).orElse(null);
        if (r == null) return null;
        if (!RideStatus.CANCELLABLE.contains(r.getStatus())) return null;
        r.setStatus("DRIVER".equalsIgnoreCase(by) ? RideStatus.CANCELLED_BY_DRIVER : RideStatus.CANCELLED_BY_USER);
        return rideRepo.save(r);
    }

    public Ride markPaid(Long rideId) {
        Ride r = rideRepo.findById(rideId).orElse(null);
        if (r == null || !RideStatus.STARTED.equals(r.getStatus())) return null;
        r.setPaid(true);
        r.setStatus(RideStatus.COMPLETED);
        return rideRepo.save(r);
    }

    public Optional<Ride> getRide(Long rideId) {
        return rideRepo.findById(rideId);
    }

    public Optional<Driver> getDriver(Long driverId) {
        return driverRepo.findById(driverId);
    }

    public List<Ride> getUserRides(Long userId) {
        return rideRepo.findByUserIdOrderByIdDesc(userId);
    }

    public List<Ride> getDriverRides(Long driverId) {
        return withoutOtp(rideRepo.findByDriverIdOrderByIdDesc(driverId));
    }

    public Map<String, Set<String>> getLocations(Long userId) {
        Set<String> pickups = new LinkedHashSet<>();
        Set<String> drops = new LinkedHashSet<>();
        for (Ride r : rideRepo.findByUserIdOrderByIdDesc(userId)) {
            pickups.add(r.getPickup());
            drops.add(r.getDrop());
        }
        Map<String, Set<String>> out = new HashMap<>();
        out.put("pickups", pickups);
        out.put("drops", drops);
        return out;
    }

    public Driver rateDriver(Long rideId, double newRating) {
        Ride r = rideRepo.findById(rideId).orElse(null);
        if (r == null || r.getDriverId() == null || !RideStatus.COMPLETED.equals(r.getStatus())) return null;
        Driver d = driverRepo.findById(r.getDriverId()).orElse(null);
        if (d == null) return null;
        int count = d.getRatingCount() == null ? 0 : d.getRatingCount();
        double current = d.getRating() == null ? Driver.DEFAULT_RATING : d.getRating();
        double avg = ((current * count) + newRating) / (count + 1);
        d.setRating(Math.round(avg * 10.0) / 10.0); // round to one decimal place
        d.setRatingCount(count + 1);
        return driverRepo.save(d);
    }

    // ---------- OTP redaction ----------

    /**
     * Drivers must not see the ride OTP — the user reads it out to prove the
     * driver picked up the right person. Returns detached copies so the
     * managed entities (and the stored OTP) are untouched.
     */
    private static Ride withoutOtp(Ride r) {
        Ride copy = new Ride();
        copy.setId(r.getId());
        copy.setUserId(r.getUserId());
        copy.setDriverId(r.getDriverId());
        copy.setPickup(r.getPickup());
        copy.setDrop(r.getDrop());
        copy.setVehicleType(r.getVehicleType());
        copy.setStatus(r.getStatus());
        copy.setPaid(r.isPaid());
        copy.setAmount(r.getAmount());
        copy.setCreatedAt(r.getCreatedAt());
        copy.setOtp(null);
        return copy;
    }

    private static List<Ride> withoutOtp(List<Ride> rides) {
        return rides.stream().map(RideService::withoutOtp).toList();
    }

    // ---------- HTTP-aware wrappers ----------

    public ResponseEntity<?> book(Ride ride) {
        String error = InputValidator.firstError(
                ride.getUserId() == null ? "User is required" : null,
                InputValidator.location(ride.getPickup(), "Pickup location"),
                InputValidator.location(ride.getDrop(), "Drop location"),
                InputValidator.vehicleType(ride.getVehicleType()));
        if (error != null) return ResponseEntity.badRequest().body(error);
        if (!userRepo.existsById(ride.getUserId())) return ResponseEntity.badRequest().body("Unknown user");
        return ResponseEntity.ok(bookRide(ride));
    }

    public ResponseEntity<?> accept(Long rideId, Long driverId) {
        Ride r = rideRepo.findById(rideId).orElse(null);
        if (r == null || !driverRepo.existsById(driverId)) return ResponseEntity.notFound().build();
        // Only a ride that is still waiting can be accepted (prevents taking over another driver's ride).
        if (!RideStatus.PENDING.equals(r.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("This ride is no longer available");
        }
        r.setDriverId(driverId);
        r.setStatus(RideStatus.ACCEPTED);
        return ResponseEntity.ok(withoutOtp(rideRepo.save(r)));
    }

    public ResponseEntity<?> deny(Long rideId) {
        // Kept simple to preserve original controller behaviour.
        return ResponseEntity.ok("Denied");
    }

    public ResponseEntity<?> updateStatusResponse(Long rideId, Map<String, String> body) {
        String status = body.get("status");
        String requiredCurrent = status == null ? null : ALLOWED_STATUS_TRANSITIONS.get(status);
        if (requiredCurrent == null) return ResponseEntity.badRequest().body("Invalid status");

        Ride current = rideRepo.findById(rideId).orElse(null);
        if (current == null) return ResponseEntity.notFound().build();
        if (!requiredCurrent.equals(current.getStatus())) {
            return ResponseEntity.badRequest().body("Ride status cannot be changed right now");
        }
        return ResponseEntity.ok(withoutOtp(updateStatus(rideId, status)));
    }

    public ResponseEntity<?> start(Long rideId, Map<String, String> body) {
        Ride r = startRide(rideId, body.get("otp"));
        if (r == null) return ResponseEntity.badRequest().body("Invalid OTP");
        return ResponseEntity.ok(r);
    }

    public ResponseEntity<?> cancel(Long rideId, Map<String, String> body) {
        String by = body == null ? null : body.get("by");
        if (by == null) by = "USER";
        Ride r = cancelRide(rideId, by);
        if (r == null) return ResponseEntity.badRequest().body("Ride cannot be cancelled");
        return ResponseEntity.ok(r);
    }

    public ResponseEntity<?> pay(Long rideId) {
        Ride r = markPaid(rideId);
        if (r == null) return ResponseEntity.badRequest().body("Ride cannot be completed right now");
        return ResponseEntity.ok(r);
    }

    /** Returns { ride, driver? } — same shape the frontend expects. */
    public ResponseEntity<?> getRideWithDriver(Long rideId) {
        return getRide(rideId).map(r -> {
            Map<String, Object> resp = new HashMap<>();
            resp.put("ride", r);
            if (r.getDriverId() != null) {
                Optional<Driver> d = getDriver(r.getDriverId());
                d.ifPresent(driver -> resp.put("driver", driver));
            }
            return ResponseEntity.ok(resp);
        }).orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<?> rate(Long rideId, Map<String, Double> body) {
        Double rating = body == null ? null : body.get("rating");
        String error = InputValidator.rating(rating);
        if (error != null) return ResponseEntity.badRequest().body(error);
        Driver d = rateDriver(rideId, rating);
        if (d == null) return ResponseEntity.badRequest().body("Invalid");
        return ResponseEntity.ok(d);
    }
}
