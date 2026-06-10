package com.application.cabapp.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.application.cabapp.model.Driver;
import com.application.cabapp.model.Ride;
import com.application.cabapp.repository.DriverRepository;
import com.application.cabapp.repository.RideRepository;

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

    // Statuses from which a ride may still be cancelled (before it has started).
    private static final Set<String> CANCELLABLE_STATUSES = new HashSet<>(Arrays.asList(
        "PENDING", "ACCEPTED", "ON_THE_WAY", "ARRIVED"
    ));

    // ---------- Core ride operations ----------

    public Ride bookRide(Ride ride) {
        ride.setStatus("PENDING");
        ride.setOtp(String.valueOf(1000 + new Random().nextInt(9000)));
        if (ride.getAmount() == null) {
            ride.setAmount(calculateFare(ride.getVehicleType()));
        }
        return rideRepo.save(ride);
    }

    /** Simple flat fare per vehicle type. */
    private double calculateFare(String vehicleType) {
        if (vehicleType == null) return 100.0;
        switch (vehicleType.toLowerCase()) {
            case "bike": return 60.0;
            case "auto": return 90.0;
            case "car":  return 150.0;
            default:     return 100.0;
        }
    }

    public List<Ride> getPendingByVehicle(String vehicleType) {
        return rideRepo.findByStatusAndVehicleType("PENDING", vehicleType);
    }

    public Ride acceptRide(Long rideId, Long driverId) {
        return rideRepo.findById(rideId).map(r -> {
            r.setDriverId(driverId);
            r.setStatus("ACCEPTED");
            return rideRepo.save(r);
        }).orElse(null);
    }

    public Ride updateStatus(Long rideId, String status) {
        return rideRepo.findById(rideId).map(r -> {
            r.setStatus(status);
            return rideRepo.save(r);
        }).orElse(null);
    }

    public Ride startRide(Long rideId, String otp) {
        Ride r = rideRepo.findById(rideId).orElse(null);
        if (r == null) return null;
        if (r.getOtp() == null || !r.getOtp().equals(otp)) return null;
        r.setStatus("STARTED");
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
        if (!CANCELLABLE_STATUSES.contains(r.getStatus())) return null;
        String who = "DRIVER".equalsIgnoreCase(by) ? "DRIVER" : "USER";
        r.setStatus("CANCELLED_BY_" + who);
        return rideRepo.save(r);
    }

    public Ride markPaid(Long rideId) {
        return rideRepo.findById(rideId).map(r -> {
            r.setPaid(true);
            r.setStatus("COMPLETED");
            return rideRepo.save(r);
        }).orElse(null);
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
        return rideRepo.findByDriverIdOrderByIdDesc(driverId);
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
        if (r == null || r.getDriverId() == null) return null;
        Driver d = driverRepo.findById(r.getDriverId()).orElse(null);
        if (d == null) return null;
        int count = d.getRatingCount() == null ? 0 : d.getRatingCount();
        double current = d.getRating() == null ? 5.0 : d.getRating();
        double avg = ((current * count) + newRating) / (count + 1);
        d.setRating(Math.round(avg * 10.0) / 10.0);
        d.setRatingCount(count + 1);
        return driverRepo.save(d);
    }

    // ---------- HTTP-aware wrappers (moved out of controller) ----------

    public ResponseEntity<?> book(Ride ride) {
        return ResponseEntity.ok(bookRide(ride));
    }

    public ResponseEntity<?> accept(Long rideId, Long driverId) {
        Ride r = acceptRide(rideId, driverId);
        if (r == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(r);
    }

    public ResponseEntity<?> deny(Long rideId) {
        // Kept simple to preserve original controller behaviour.
        return ResponseEntity.ok("Denied");
    }

    public ResponseEntity<?> updateStatusResponse(Long rideId, Map<String, String> body) {
        Ride r = updateStatus(rideId, body.get("status"));
        if (r == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(r);
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
        if (r == null) return ResponseEntity.notFound().build();
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
        Driver d = rateDriver(rideId, body.get("rating"));
        if (d == null) return ResponseEntity.badRequest().body("Invalid");
        return ResponseEntity.ok(d);
    }
}
