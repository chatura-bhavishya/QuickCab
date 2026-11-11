package com.application.cabapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

import com.application.cabapp.dto.request.BookRideRequest;
import com.application.cabapp.dto.request.RateRideRequest;
import com.application.cabapp.dto.request.RideStatusRequest;
import com.application.cabapp.dto.request.StartRideRequest;
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

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    private static final AuthPrincipal RIDER = new AuthPrincipal(1L, Role.USER);
    private static final AuthPrincipal OTHER_RIDER = new AuthPrincipal(2L, Role.USER);
    private static final AuthPrincipal DRIVER = new AuthPrincipal(10L, Role.DRIVER);
    private static final AuthPrincipal OTHER_DRIVER = new AuthPrincipal(11L, Role.DRIVER);

    @Mock RideRepository rideRepository;
    @Mock DriverRepository driverRepository;
    @Mock UserRepository userRepository;
    @Mock DriverService driverService;
    @Mock ApplicationEventPublisher events;

    private RideService rideService;

    @BeforeEach
    void setUp() {
        rideService = new RideService(rideRepository, driverRepository, userRepository,
                new RideAccessService(rideRepository), driverService,
                new AttemptLimiter(3, Duration.ofMinutes(15), Clock.systemUTC()), new FareCalculator(), events);
    }

    private static Ride ride(RideStatus status, Long driverId) {
        Ride r = new Ride();
        r.setId(100L);
        r.setUserId(RIDER.id());
        r.setDriverId(driverId);
        r.setPickup("A");
        r.setDrop("B");
        r.setVehicleType("car");
        r.setStatus(status);
        r.setOtp("4321");
        r.setAmount(150.0);
        return r;
    }

    private static Driver carDriver(Long id) {
        Driver d = new Driver();
        d.setId(id);
        d.setVehicleType("car");
        d.setRating(5.0);
        d.setRatingCount(0);
        return d;
    }

    private static HttpStatus statusOf(Throwable t) {
        return ((ApiException) t).getStatus();
    }

    // ----- Booking -----

    @Test
    void bookUsesCallerIdServerFareAndRandomOtp() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

        RideResponse booked = rideService.book(RIDER, new BookRideRequest(" Home ", "Office", "bike"));

        assertThat(booked.userId()).isEqualTo(1L);
        assertThat(booked.status()).isEqualTo(RideStatus.PENDING);
        assertThat(booked.amount()).isEqualTo(60.0);
        assertThat(booked.pickup()).isEqualTo("Home");
        assertThat(booked.otp()).matches("\\d{4}");
        assertThat(booked.driverId()).isNull();
    }

    @Test
    void bookingWithPinsUsesDistanceBasedFare() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

        // MG Road to Whitefield, Bengaluru: about 16 km in a straight line.
        RideResponse booked = rideService.book(RIDER,
                new BookRideRequest("MG Road", "Whitefield", "car", 12.9756, 77.6050, 12.9698, 77.7500));

        assertThat(booked.distanceKm()).isBetween(15.0, 16.5);
        assertThat(booked.amount()).isEqualTo(50 + 15 * booked.distanceKm(), org.assertj.core.data.Offset.offset(1.0));
        assertThat(booked.pickupLat()).isEqualTo(12.9756);
    }

    @Test
    void halfSetPinsAreRejected() {
        assertThatThrownBy(() -> rideService.book(RIDER,
                new BookRideRequest("A", "B", "car", 12.9, 77.6, null, null)))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void driversCannotBookRides() {
        assertThatThrownBy(() -> rideService.book(DRIVER, new BookRideRequest("A", "B", "car")))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.FORBIDDEN));
        verify(rideRepository, never()).save(any());
    }

    // ----- Ownership -----

    @Test
    void ridersCannotSeeOtherRidersRides() {
        when(rideRepository.findById(100L)).thenReturn(Optional.of(ride(RideStatus.PENDING, null)));

        assertThatThrownBy(() -> rideService.getDetails(OTHER_RIDER, 100L))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void unassignedDriversCannotSeeOrChangeTheRide() {
        when(rideRepository.findById(100L)).thenReturn(Optional.of(ride(RideStatus.ACCEPTED, DRIVER.id())));

        assertThatThrownBy(() -> rideService.getDetails(OTHER_DRIVER, 100L))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.FORBIDDEN));
        assertThatThrownBy(() -> rideService.updateStatus(OTHER_DRIVER, 100L, new RideStatusRequest("ON_THE_WAY")))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void otpIsOnlyVisibleToTheRider() {
        when(rideRepository.findById(100L)).thenReturn(Optional.of(ride(RideStatus.ACCEPTED, DRIVER.id())));
        when(driverRepository.findById(DRIVER.id())).thenReturn(Optional.of(carDriver(DRIVER.id())));

        assertThat(rideService.getDetails(RIDER, 100L).ride().otp()).isEqualTo("4321");
        assertThat(rideService.getDetails(DRIVER, 100L).ride().otp()).isNull();
    }

    // ----- Accepting -----

    @Test
    void acceptFailsWhenAnotherDriverGotThereFirst() {
        when(driverService.loadCurrent(DRIVER)).thenReturn(carDriver(DRIVER.id()));
        when(rideRepository.findById(100L)).thenReturn(Optional.of(ride(RideStatus.PENDING, null)));
        when(rideRepository.assignDriverIfStatus(100L, DRIVER.id(), RideStatus.PENDING, RideStatus.ACCEPTED))
                .thenReturn(0);

        assertThatThrownBy(() -> rideService.accept(DRIVER, 100L))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void acceptRequiresMatchingVehicleType() {
        Driver bikeDriver = carDriver(DRIVER.id());
        bikeDriver.setVehicleType("bike");
        when(driverService.loadCurrent(DRIVER)).thenReturn(bikeDriver);
        when(rideRepository.findById(100L)).thenReturn(Optional.of(ride(RideStatus.PENDING, null)));

        assertThatThrownBy(() -> rideService.accept(DRIVER, 100L))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.FORBIDDEN));
        verify(rideRepository, never()).assignDriverIfStatus(anyLong(), anyLong(), any(), any());
    }

    @Test
    void pendingListHidesOtps() {
        when(driverService.loadCurrent(DRIVER)).thenReturn(carDriver(DRIVER.id()));
        when(rideRepository.findByStatusAndVehicleTypeOrderByIdAsc(RideStatus.PENDING, "car"))
                .thenReturn(List.of(ride(RideStatus.PENDING, null)));

        assertThat(rideService.getPendingForDriver(DRIVER)).singleElement()
                .satisfies(r -> assertThat(r.otp()).isNull());
    }

    // ----- Workflow -----

    @Test
    void statusMustFollowTheAllowedOrder() {
        when(rideRepository.findById(100L)).thenReturn(Optional.of(ride(RideStatus.ACCEPTED, DRIVER.id())));

        assertThatThrownBy(() -> rideService.updateStatus(DRIVER, 100L, new RideStatusRequest("ARRIVED")))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.BAD_REQUEST));
        assertThat(rideService.updateStatus(DRIVER, 100L, new RideStatusRequest("ON_THE_WAY")).status())
                .isEqualTo(RideStatus.ON_THE_WAY);
    }

    @Test
    void startRequiresTheCorrectOtp() {
        Ride arrived = ride(RideStatus.ARRIVED, DRIVER.id());
        when(rideRepository.findById(100L)).thenReturn(Optional.of(arrived));

        assertThatThrownBy(() -> rideService.start(DRIVER, 100L, new StartRideRequest("0000")))
                .hasMessage("Invalid OTP");
        assertThat(rideService.start(DRIVER, 100L, new StartRideRequest("4321")).status())
                .isEqualTo(RideStatus.STARTED);
    }

    @Test
    void otpGuessingIsCutOff() {
        when(rideRepository.findById(100L)).thenReturn(Optional.of(ride(RideStatus.ARRIVED, DRIVER.id())));

        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> rideService.start(DRIVER, 100L, new StartRideRequest("0000")))
                    .hasMessage("Invalid OTP");
        }
        assertThatThrownBy(() -> rideService.start(DRIVER, 100L, new StartRideRequest("4321")))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
    }

    @Test
    void completeOnlyAfterStart() {
        when(rideRepository.findById(100L)).thenReturn(Optional.of(ride(RideStatus.ARRIVED, DRIVER.id())));

        assertThatThrownBy(() -> rideService.completeWithPayment(DRIVER, 100L))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void cancelRecordsWhoCancelledFromTheToken() {
        when(rideRepository.findById(100L)).thenReturn(Optional.of(ride(RideStatus.ACCEPTED, DRIVER.id())));
        assertThat(rideService.cancel(DRIVER, 100L).status()).isEqualTo(RideStatus.CANCELLED_BY_DRIVER);

        when(rideRepository.findById(100L)).thenReturn(Optional.of(ride(RideStatus.PENDING, null)));
        assertThat(rideService.cancel(RIDER, 100L).status()).isEqualTo(RideStatus.CANCELLED_BY_USER);
    }

    @Test
    void cannotCancelAStartedRide() {
        when(rideRepository.findById(100L)).thenReturn(Optional.of(ride(RideStatus.STARTED, DRIVER.id())));

        assertThatThrownBy(() -> rideService.cancel(RIDER, 100L))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    // ----- Rating -----

    @Test
    void ratingUpdatesAverageOnceOnly() {
        Ride completed = ride(RideStatus.COMPLETED, DRIVER.id());
        Driver driver = carDriver(DRIVER.id());
        driver.setRating(4.0);
        driver.setRatingCount(1);
        when(rideRepository.findById(100L)).thenReturn(Optional.of(completed));
        when(driverRepository.findById(DRIVER.id())).thenReturn(Optional.of(driver));

        assertThat(rideService.rateDriver(RIDER, 100L, new RateRideRequest(5.0)).rating()).isEqualTo(4.5);
        assertThatThrownBy(() -> rideService.rateDriver(RIDER, 100L, new RateRideRequest(5.0)))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void onlyTheRiderCanRate() {
        when(rideRepository.findById(100L)).thenReturn(Optional.of(ride(RideStatus.COMPLETED, DRIVER.id())));

        assertThatThrownBy(() -> rideService.rateDriver(OTHER_RIDER, 100L, new RateRideRequest(1.0)))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.FORBIDDEN));
    }
}
