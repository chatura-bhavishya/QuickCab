package com.application.cabapp.controller;

import com.application.cabapp.model.Ride;
import com.application.cabapp.service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Thin HTTP adapter — all logic lives in RideService.
 * Response shapes (including the { ride, driver? } payload from GET /{rideId})
 * are preserved so the frontend continues to work unchanged.
 */
@RestController
@RequestMapping("/api/rides")
public class RideController {

    @Autowired
    private RideService rideService;

    @PostMapping("/book")
    public ResponseEntity<?> book(@RequestBody Ride ride) {
        return rideService.book(ride);
    }

    @GetMapping("/pending/{vehicleType}")
    public List<Ride> pending(@PathVariable String vehicleType) {
        return rideService.getPendingByVehicle(vehicleType);
    }

    @PostMapping("/{rideId}/accept/{driverId}")
    public ResponseEntity<?> accept(@PathVariable Long rideId, @PathVariable Long driverId) {
        return rideService.accept(rideId, driverId);
    }

    @PostMapping("/{rideId}/deny")
    public ResponseEntity<?> deny(@PathVariable Long rideId) {
        return rideService.deny(rideId);
    }

    @PostMapping("/{rideId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long rideId, @RequestBody Map<String, String> body) {
        return rideService.updateStatusResponse(rideId, body);
    }

    @PostMapping("/{rideId}/start")
    public ResponseEntity<?> start(@PathVariable Long rideId, @RequestBody Map<String, String> body) {
        return rideService.start(rideId, body);
    }

    /** Cancel a ride. Body: { "by": "USER" | "DRIVER" }. */
    @PostMapping("/{rideId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long rideId,
                                    @RequestBody(required = false) Map<String, String> body) {
        return rideService.cancel(rideId, body);
    }

    @PostMapping("/{rideId}/pay")
    public ResponseEntity<?> pay(@PathVariable Long rideId) {
        return rideService.pay(rideId);
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<?> get(@PathVariable Long rideId) {
        return rideService.getRideWithDriver(rideId);
    }

    @GetMapping("/user/{userId}")
    public List<Ride> userRides(@PathVariable Long userId) {
        return rideService.getUserRides(userId);
    }

    @GetMapping("/driver/{driverId}")
    public List<Ride> driverRides(@PathVariable Long driverId) {
        return rideService.getDriverRides(driverId);
    }

    @GetMapping("/locations/{userId}")
    public Map<String, Set<String>> locations(@PathVariable Long userId) {
        return rideService.getLocations(userId);
    }

    @PostMapping("/{rideId}/rate")
    public ResponseEntity<?> rate(@PathVariable Long rideId, @RequestBody Map<String, Double> body) {
        return rideService.rate(rideId, body);
    }
}
