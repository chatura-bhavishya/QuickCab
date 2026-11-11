package com.application.cabapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.application.cabapp.model.Ride;
import com.application.cabapp.model.RideStatus;

public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByUserIdOrderByIdDesc(Long userId);

    List<Ride> findByDriverIdOrderByIdDesc(Long driverId);

    List<Ride> findByStatusAndVehicleTypeOrderByIdAsc(RideStatus status, String vehicleType);

    /**
     * Atomically assigns a driver, but only if the ride is still in the
     * expected status. Returns 1 on success, 0 if someone else got there
     * first, so two drivers can never accept the same ride.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Ride r set r.driverId = :driverId, r.status = :newStatus "
            + "where r.id = :rideId and r.status = :expectedStatus")
    int assignDriverIfStatus(@Param("rideId") Long rideId,
                             @Param("driverId") Long driverId,
                             @Param("expectedStatus") RideStatus expectedStatus,
                             @Param("newStatus") RideStatus newStatus);
}
