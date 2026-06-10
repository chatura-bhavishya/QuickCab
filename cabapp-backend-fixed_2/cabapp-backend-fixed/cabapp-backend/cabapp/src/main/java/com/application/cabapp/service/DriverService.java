package com.application.cabapp.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.application.cabapp.model.Driver;
import com.application.cabapp.repository.DriverRepository;

/**
 * DriverService — owns ALL business logic for the Driver entity.
 */
@Service
public class DriverService {

    @Autowired
    private DriverRepository driverRepo;

    // ---------- Basic CRUD ----------

    public Driver saveDriver(Driver driver) {
        if (driver.getRating() == null) driver.setRating(5.0);
        if (driver.getRatingCount() == null) driver.setRatingCount(0);
        return driverRepo.save(driver);
    }

    public Driver save(Driver driver) {
        return driverRepo.save(driver);
    }

    public List<Driver> getAllDrivers() {
        return driverRepo.findAll();
    }

    public Optional<Driver> getDriverById(Long id) {
        return driverRepo.findById(id);
    }

    public Optional<Driver> getDriverByUsername(String username) {
        return driverRepo.findByUsername(username);
    }

    public Driver updateDriver(Long id, Driver updated) {
        return driverRepo.findById(id).map(d -> {
            d.setContact(updated.getContact());
            d.setVehicleType(updated.getVehicleType());
            d.setVehicleNumber(updated.getVehicleNumber());
            if (updated.getPassword() != null && !updated.getPassword().isEmpty()) {
                d.setPassword(updated.getPassword());
            }
            return driverRepo.save(d);
        }).orElse(null);
    }

    public void deleteDriver(Long id) {
        driverRepo.deleteById(id);
    }

    // ---------- Business operations (moved out of controller) ----------

    public ResponseEntity<?> register(Driver driver) {
        if (getDriverByUsername(driver.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        driver.setRating(5.0);
        driver.setRatingCount(0);
        return ResponseEntity.ok(saveDriver(driver));
    }

    public ResponseEntity<?> login(Map<String, String> body) {
        Optional<Driver> d = getDriverByUsername(body.get("username"));
        if (d.isPresent() && d.get().getPassword().equals(body.get("password"))) {
            return ResponseEntity.ok(d.get());
        }
        Map<String, String> err = new HashMap<>();
        err.put("error", "Invalid credentials");
        return ResponseEntity.status(401).body(err);
    }

    public ResponseEntity<?> getById(Long id) {
        return getDriverById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<?> update(Long id, Driver updated) {
        Driver d = updateDriver(id, updated);
        if (d == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(d);
    }

    public ResponseEntity<?> delete(Long id) {
        deleteDriver(id);
        return ResponseEntity.ok("Deleted");
    }
}
