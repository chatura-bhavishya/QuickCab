package com.application.cabapp.repository;

import com.application.cabapp.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByUserIdOrderByIdDesc(Long userId);
    List<Ride> findByDriverIdOrderByIdDesc(Long driverId);
    List<Ride> findByStatusAndVehicleType(String status, String vehicleType);
}
