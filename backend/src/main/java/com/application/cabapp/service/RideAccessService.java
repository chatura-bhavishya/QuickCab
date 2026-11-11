package com.application.cabapp.service;

import org.springframework.stereotype.Component;

import com.application.cabapp.exception.ApiException;
import com.application.cabapp.model.Ride;
import com.application.cabapp.repository.RideRepository;
import com.application.cabapp.security.AuthPrincipal;
import com.application.cabapp.security.Role;

/**
 * Central place for "who may touch this ride" rules, shared by RideService
 * and ChatService. Every ride lookup by id from a controller goes through here.
 */
@Component
public class RideAccessService {

    private static final String NO_ACCESS = "You do not have access to this ride";

    private final RideRepository rideRepository;

    public RideAccessService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    /** The rider who booked it, or the driver assigned to it. */
    public Ride getForParticipant(AuthPrincipal principal, Long rideId) {
        Ride ride = find(rideId);
        if (!isParticipant(principal, ride)) throw ApiException.forbidden(NO_ACCESS);
        return ride;
    }

    /** Only the rider who booked it. */
    public Ride getForRider(AuthPrincipal principal, Long rideId) {
        principal.requireRole(Role.USER);
        Ride ride = find(rideId);
        if (!principal.id().equals(ride.getUserId())) throw ApiException.forbidden(NO_ACCESS);
        return ride;
    }

    /** Only the driver assigned to it. */
    public Ride getForAssignedDriver(AuthPrincipal principal, Long rideId) {
        principal.requireRole(Role.DRIVER);
        Ride ride = find(rideId);
        if (!principal.id().equals(ride.getDriverId())) throw ApiException.forbidden(NO_ACCESS);
        return ride;
    }

    public static boolean isParticipant(AuthPrincipal principal, Ride ride) {
        return switch (principal.role()) {
            case USER -> principal.id().equals(ride.getUserId());
            case DRIVER -> principal.id().equals(ride.getDriverId());
        };
    }

    public Ride find(Long rideId) {
        return rideRepository.findById(rideId).orElseThrow(() -> ApiException.notFound("Ride not found"));
    }
}
