package com.application.cabapp.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.application.cabapp.dto.request.BookRideRequest;
import com.application.cabapp.dto.request.FareEstimateRequest;
import com.application.cabapp.dto.request.RateRideRequest;
import com.application.cabapp.dto.request.RideStatusRequest;
import com.application.cabapp.dto.request.RoutePoints;
import com.application.cabapp.dto.request.StartRideRequest;
import com.application.cabapp.dto.response.DriverResponse;
import com.application.cabapp.dto.response.FareEstimateResponse;
import com.application.cabapp.dto.response.LocationsResponse;
import com.application.cabapp.dto.response.RideDetailsResponse;
import com.application.cabapp.dto.response.RideResponse;
import com.application.cabapp.exception.ApiException;
import com.application.cabapp.model.Driver;
import com.application.cabapp.model.Ride;
import com.application.cabapp.model.RideStatus;
import com.application.cabapp.repository.DriverRepository;
import com.application.cabapp.repository.RideRepository;
import com.application.cabapp.repository.UserRepository;
import com.application.cabapp.security.AttemptLimiter;
import com.application.cabapp.security.AuthPrincipal;
import com.application.cabapp.security.Role;

/**
 * The ride workflow. Every method receives the authenticated caller and only
 * lets them act on rides they own (rider) or are assigned to (driver).
 */
@Service
public class RideService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int OTP_MIN = 1000;
    private static final int OTP_RANGE = 9000; // OTPs are 4 digits: 1000-9999

    /**
     * Status changes a driver may make through the generic /status endpoint,
     * mapped to the status the ride must currently be in. STARTED (needs OTP),
     * COMPLETED (needs payment) and cancellation have their own endpoints.
     */
    private static final Map<RideStatus, RideStatus> ALLOWED_TRANSITIONS = Map.of(
            RideStatus.ON_THE_WAY, RideStatus.ACCEPTED,
            RideStatus.ARRIVED, RideStatus.ON_THE_WAY);

    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final RideAccessService rideAccess;
    private final DriverService driverService;
    private final AttemptLimiter otpAttemptLimiter;
    private final FareCalculator fareCalculator;

    public RideService(RideRepository rideRepository, DriverRepository driverRepository,
                       UserRepository userRepository, RideAccessService rideAccess, DriverService driverService,
                       @Qualifier("otpAttemptLimiter") AttemptLimiter otpAttemptLimiter,
                       FareCalculator fareCalculator) {
        this.rideRepository = rideRepository;
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
        this.rideAccess = rideAccess;
        this.driverService = driverService;
        this.otpAttemptLimiter = otpAttemptLimiter;
        this.fareCalculator = fareCalculator;
    }

    // ---------- Rider actions ----------

    @Transactional
    public RideResponse book(AuthPrincipal principal, BookRideRequest request) {
        principal.requireRole(Role.USER);
        if (request.isPartial()) {
            throw ApiException.badRequest("Set both the pickup and drop pins, or neither");
        }
        if (!userRepository.existsById(principal.id())) {
            throw ApiException.unauthorized("Account not found. Please log in again.");
        }
        Ride ride = new Ride();
        ride.setUserId(principal.id());
        ride.setPickup(request.pickup().trim());
        ride.setDrop(request.drop().trim());
        ride.setVehicleType(request.vehicleType());
        ride.setStatus(RideStatus.PENDING);
        ride.setOtp(String.valueOf(OTP_MIN + RANDOM.nextInt(OTP_RANGE)));
        // The fare is always worked out here, never taken from the client.
        if (request.hasPins()) {
            double km = tripDistance(request);
            ride.setPickupLat(request.pickupLat());
            ride.setPickupLng(request.pickupLng());
            ride.setDropLat(request.dropLat());
            ride.setDropLng(request.dropLng());
            ride.setDistanceKm(km);
            ride.setAmount(fareCalculator.fareFor(request.vehicleType(), km));
        } else {
            ride.setAmount(fareCalculator.minimumFare(request.vehicleType()));
        }
        ride.setPaid(false);
        ride.setCreatedAt(LocalDateTime.now());
        return RideResponse.forRider(rideRepository.save(ride));
    }

    /** Price a trip before booking it (shown on the booking screen). */
    public FareEstimateResponse estimate(FareEstimateRequest request) {
        double km = tripDistance(request);
        return new FareEstimateResponse(km, fareCalculator.fareFor(request.vehicleType(), km));
    }

    private double tripDistance(RoutePoints pins) {
        double km = fareCalculator.distanceKm(pins.pickupLat(), pins.pickupLng(), pins.dropLat(), pins.dropLng());
        if (km > FareCalculator.MAX_TRIP_KM) {
            throw ApiException.badRequest("Pickup and drop are too far apart");
        }
        return km;
    }

    public LocationsResponse getPastLocations(AuthPrincipal principal) {
        principal.requireRole(Role.USER);
        Set<String> pickups = new LinkedHashSet<>();
        Set<String> drops = new LinkedHashSet<>();
        for (Ride r : rideRepository.findByUserIdOrderByIdDesc(principal.id())) {
            pickups.add(r.getPickup());
            drops.add(r.getDrop());
        }
        return new LocationsResponse(pickups, drops);
    }

    @Transactional
    public DriverResponse rateDriver(AuthPrincipal principal, Long rideId, RateRideRequest request) {
        Ride ride = rideAccess.getForRider(principal, rideId);
        if (ride.getStatus() != RideStatus.COMPLETED || ride.getDriverId() == null) {
            throw ApiException.badRequest("You can only rate a completed ride");
        }
        if (ride.isDriverRated()) {
            throw ApiException.conflict("You have already rated this ride");
        }
        Driver driver = driverRepository.findById(ride.getDriverId())
                .orElseThrow(() -> ApiException.notFound("Driver not found"));

        int count = driver.getRatingCount() == null ? 0 : driver.getRatingCount();
        double current = driver.getRating() == null ? Driver.DEFAULT_RATING : driver.getRating();
        double average = ((current * count) + request.rating()) / (count + 1);
        driver.setRating(Math.round(average * 10.0) / 10.0); // one decimal place
        driver.setRatingCount(count + 1);
        ride.setDriverRated(true);
        return DriverResponse.from(driver);
    }

    // ---------- Driver actions ----------

    /** Waiting rides for the calling driver's own vehicle type. */
    public List<RideResponse> getPendingForDriver(AuthPrincipal principal) {
        Driver driver = driverService.loadCurrent(principal);
        return rideRepository.findByStatusAndVehicleTypeOrderByIdAsc(RideStatus.PENDING, driver.getVehicleType())
                .stream().map(RideResponse::forDriver).toList();
    }

    @Transactional
    public RideResponse accept(AuthPrincipal principal, Long rideId) {
        Driver driver = driverService.loadCurrent(principal);
        Ride ride = rideAccess.find(rideId);
        if (!driver.getVehicleType().equals(ride.getVehicleType())) {
            throw ApiException.forbidden("This ride needs a different vehicle type");
        }
        // Atomic check-and-set: fails if another driver accepted it first.
        int updated = rideRepository.assignDriverIfStatus(rideId, driver.getId(),
                RideStatus.PENDING, RideStatus.ACCEPTED);
        if (updated == 0) {
            throw ApiException.conflict("This ride is no longer available");
        }
        return RideResponse.forDriver(rideAccess.find(rideId));
    }

    /** Declining simply leaves the ride for other drivers. */
    public void deny(AuthPrincipal principal, Long rideId) {
        principal.requireRole(Role.DRIVER);
        rideAccess.find(rideId);
    }

    @Transactional
    public RideResponse updateStatus(AuthPrincipal principal, Long rideId, RideStatusRequest request) {
        Ride ride = rideAccess.getForAssignedDriver(principal, rideId);
        RideStatus target = RideStatus.valueOf(request.status());
        if (ride.getStatus() != ALLOWED_TRANSITIONS.get(target)) {
            throw ApiException.badRequest("Ride status cannot be changed right now");
        }
        ride.setStatus(target);
        return RideResponse.forDriver(ride);
    }

    @Transactional
    public RideResponse start(AuthPrincipal principal, Long rideId, StartRideRequest request) {
        Ride ride = rideAccess.getForAssignedDriver(principal, rideId);
        String limiterKey = "ride:" + rideId;
        if (otpAttemptLimiter.isBlocked(limiterKey)) {
            throw ApiException.tooManyRequests("Too many wrong OTPs. Please wait a few minutes and try again.");
        }
        if (ride.getStatus() != RideStatus.ARRIVED || !request.otp().equals(ride.getOtp())) {
            otpAttemptLimiter.recordFailure(limiterKey);
            throw ApiException.badRequest("Invalid OTP");
        }
        otpAttemptLimiter.reset(limiterKey);
        ride.setStatus(RideStatus.STARTED);
        return RideResponse.forDriver(ride);
    }

    /** Driver confirms payment was received, which completes the ride. */
    @Transactional
    public RideResponse completeWithPayment(AuthPrincipal principal, Long rideId) {
        Ride ride = rideAccess.getForAssignedDriver(principal, rideId);
        if (ride.getStatus() != RideStatus.STARTED) {
            throw ApiException.badRequest("Ride cannot be completed right now");
        }
        ride.setPaid(true);
        ride.setStatus(RideStatus.COMPLETED);
        return RideResponse.forDriver(ride);
    }

    // ---------- Shared actions ----------

    /** Rider or assigned driver cancels before the ride starts. Who cancelled comes from the token. */
    @Transactional
    public RideResponse cancel(AuthPrincipal principal, Long rideId) {
        Ride ride = rideAccess.getForParticipant(principal, rideId);
        if (!ride.getStatus().isCancellable()) {
            throw ApiException.badRequest("Ride cannot be cancelled");
        }
        ride.setStatus(principal.is(Role.DRIVER) ? RideStatus.CANCELLED_BY_DRIVER : RideStatus.CANCELLED_BY_USER);
        return toResponseFor(principal, ride);
    }

    public RideDetailsResponse getDetails(AuthPrincipal principal, Long rideId) {
        Ride ride = rideAccess.getForParticipant(principal, rideId);
        DriverResponse driver = ride.getDriverId() == null ? null
                : driverRepository.findById(ride.getDriverId()).map(DriverResponse::from).orElse(null);
        return new RideDetailsResponse(toResponseFor(principal, ride), driver);
    }

    /** Ride history of the caller (as rider or as driver). */
    public List<RideResponse> getMyRides(AuthPrincipal principal) {
        List<Ride> rides = principal.is(Role.USER)
                ? rideRepository.findByUserIdOrderByIdDesc(principal.id())
                : rideRepository.findByDriverIdOrderByIdDesc(principal.id());
        return rides.stream().map(r -> toResponseFor(principal, r)).toList();
    }

    private static RideResponse toResponseFor(AuthPrincipal principal, Ride ride) {
        return principal.is(Role.USER) ? RideResponse.forRider(ride) : RideResponse.forDriver(ride);
    }
}
