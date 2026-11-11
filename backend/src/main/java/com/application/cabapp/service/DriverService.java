package com.application.cabapp.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.application.cabapp.model.Driver;
import com.application.cabapp.repository.DriverRepository;
import com.application.cabapp.security.PasswordService;
import com.application.cabapp.validation.InputValidator;

/**
 * DriverService — owns ALL business logic for the Driver entity.
 */
@Service
public class DriverService {

    @Autowired
    private DriverRepository driverRepo;

    @Autowired
    private PasswordService passwordService;

    // ---------- Basic CRUD ----------

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
                d.setPassword(passwordService.hash(updated.getPassword()));
            }
            return driverRepo.save(d);
        }).orElse(null);
    }

    public void deleteDriver(Long id) {
        driverRepo.deleteById(id);
    }

    // ---------- Business operations ----------

    public ResponseEntity<?> register(Driver driver) {
        String error = InputValidator.firstError(
                InputValidator.username(driver.getUsername()),
                InputValidator.password(driver.getPassword()),
                InputValidator.contact(driver.getContact()),
                InputValidator.vehicleType(driver.getVehicleType()),
                InputValidator.vehicleNumber(driver.getVehicleNumber()));
        if (error != null) return ResponseEntity.badRequest().body(error);

        if (getDriverByUsername(driver.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        // Never let a client-supplied id or rating overwrite server-owned data.
        driver.setId(null);
        driver.setRating(Driver.DEFAULT_RATING);
        driver.setRatingCount(0);
        driver.setPassword(passwordService.hash(driver.getPassword()));
        return ResponseEntity.ok(driverRepo.save(driver));
    }

    public ResponseEntity<?> login(Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        Optional<Driver> found = username == null ? Optional.empty() : getDriverByUsername(username);

        if (found.isPresent() && passwordService.matches(password, found.get().getPassword())) {
            Driver driver = found.get();
            if (passwordService.needsRehash(driver.getPassword())) {
                // Upgrade a legacy plaintext password to a BCrypt hash.
                driver.setPassword(passwordService.hash(password));
                driverRepo.save(driver);
            }
            return ResponseEntity.ok(driver);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
    }

    public ResponseEntity<?> getById(Long id) {
        return getDriverById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<?> update(Long id, Driver updated) {
        String error = InputValidator.firstError(
                InputValidator.contact(updated.getContact()),
                InputValidator.vehicleType(updated.getVehicleType()),
                InputValidator.vehicleNumber(updated.getVehicleNumber()),
                InputValidator.optionalPassword(updated.getPassword()));
        if (error != null) return ResponseEntity.badRequest().body(error);

        Driver d = updateDriver(id, updated);
        if (d == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(d);
    }

    public ResponseEntity<?> delete(Long id) {
        deleteDriver(id);
        return ResponseEntity.ok("Deleted");
    }
}
