package com.application.cabapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.application.cabapp.dto.request.BookRideRequest;
import com.application.cabapp.dto.request.RateRideRequest;
import com.application.cabapp.dto.request.RideStatusRequest;
import com.application.cabapp.dto.request.StartRideRequest;
import com.application.cabapp.dto.response.DriverResponse;
import com.application.cabapp.dto.response.LocationsResponse;
import com.application.cabapp.dto.response.RideDetailsResponse;
import com.application.cabapp.dto.response.RideResponse;
import com.application.cabapp.security.AuthPrincipal;
import com.application.cabapp.service.RideService;

import jakarta.validation.Valid;

/**
 * Ride endpoints. The caller always comes from the JWT; rider and driver ids
 * are never accepted from the client.
 */
@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    // ----- Rider -----

    @PostMapping("/book")
    @ResponseStatus(HttpStatus.CREATED)
    public RideResponse book(@AuthenticationPrincipal AuthPrincipal principal,
                             @Valid @RequestBody BookRideRequest request) {
        return rideService.book(principal, request);
    }

    @GetMapping("/locations")
    public LocationsResponse locations(@AuthenticationPrincipal AuthPrincipal principal) {
        return rideService.getPastLocations(principal);
    }

    @PostMapping("/{rideId}/rate")
    public DriverResponse rate(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long rideId,
                               @Valid @RequestBody RateRideRequest request) {
        return rideService.rateDriver(principal, rideId, request);
    }

    // ----- Driver -----

    @GetMapping("/pending")
    public List<RideResponse> pending(@AuthenticationPrincipal AuthPrincipal principal) {
        return rideService.getPendingForDriver(principal);
    }

    @PostMapping("/{rideId}/accept")
    public RideResponse accept(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long rideId) {
        return rideService.accept(principal, rideId);
    }

    @PostMapping("/{rideId}/deny")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deny(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long rideId) {
        rideService.deny(principal, rideId);
    }

    @PostMapping("/{rideId}/status")
    public RideResponse updateStatus(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long rideId,
                                     @Valid @RequestBody RideStatusRequest request) {
        return rideService.updateStatus(principal, rideId, request);
    }

    @PostMapping("/{rideId}/start")
    public RideResponse start(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long rideId,
                              @Valid @RequestBody StartRideRequest request) {
        return rideService.start(principal, rideId, request);
    }

    @PostMapping("/{rideId}/pay")
    public RideResponse pay(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long rideId) {
        return rideService.completeWithPayment(principal, rideId);
    }

    // ----- Rider or assigned driver -----

    @PostMapping("/{rideId}/cancel")
    public RideResponse cancel(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long rideId) {
        return rideService.cancel(principal, rideId);
    }

    @GetMapping("/mine")
    public List<RideResponse> mine(@AuthenticationPrincipal AuthPrincipal principal) {
        return rideService.getMyRides(principal);
    }

    @GetMapping("/{rideId}")
    public RideDetailsResponse get(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long rideId) {
        return rideService.getDetails(principal, rideId);
    }
}
