package com.application.cabapp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.application.cabapp.dto.request.DriverRegisterRequest;
import com.application.cabapp.dto.request.DriverUpdateRequest;
import com.application.cabapp.dto.request.LoginRequest;
import com.application.cabapp.dto.response.AuthResponse;
import com.application.cabapp.dto.response.DriverResponse;
import com.application.cabapp.exception.ApiException;
import com.application.cabapp.model.Driver;
import com.application.cabapp.repository.DriverRepository;
import com.application.cabapp.security.AuthPrincipal;
import com.application.cabapp.security.JwtService;
import com.application.cabapp.security.PasswordService;
import com.application.cabapp.security.Role;

/** Driver accounts: registration, login and managing your own profile. */
@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public DriverService(DriverRepository driverRepository, PasswordService passwordService, JwtService jwtService) {
        this.driverRepository = driverRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    @Transactional
    public DriverResponse register(DriverRegisterRequest request) {
        if (driverRepository.existsByUsername(request.username())) {
            throw ApiException.conflict("Username already exists");
        }
        Driver driver = new Driver();
        driver.setUsername(request.username());
        driver.setContact(request.contact().trim());
        driver.setVehicleType(request.vehicleType());
        driver.setVehicleNumber(request.vehicleNumber().trim());
        driver.setRating(Driver.DEFAULT_RATING);
        driver.setRatingCount(0);
        driver.setPassword(passwordService.hash(request.password()));
        return DriverResponse.from(driverRepository.save(driver));
    }

    @Transactional
    public AuthResponse<DriverResponse> login(LoginRequest request) {
        Driver driver = driverRepository.findByUsername(request.username())
                .filter(d -> passwordService.matches(request.password(), d.getPassword()))
                .orElseThrow(() -> ApiException.unauthorized("Invalid username or password"));

        if (passwordService.needsRehash(driver.getPassword())) {
            // Upgrade a legacy plaintext password to a BCrypt hash.
            driver.setPassword(passwordService.hash(request.password()));
        }
        String token = jwtService.generateToken(new AuthPrincipal(driver.getId(), Role.DRIVER));
        return new AuthResponse<>(token, jwtService.getExpirationSeconds(), DriverResponse.from(driver));
    }

    public DriverResponse getCurrent(AuthPrincipal principal) {
        return DriverResponse.from(loadCurrent(principal));
    }

    @Transactional
    public DriverResponse updateCurrent(AuthPrincipal principal, DriverUpdateRequest request) {
        Driver driver = loadCurrent(principal);
        driver.setContact(request.contact().trim());
        driver.setVehicleType(request.vehicleType());
        driver.setVehicleNumber(request.vehicleNumber().trim());
        if (request.password() != null && !request.password().isEmpty()) {
            driver.setPassword(passwordService.hash(request.password()));
        }
        return DriverResponse.from(driver);
    }

    @Transactional
    public void deleteCurrent(AuthPrincipal principal) {
        driverRepository.delete(loadCurrent(principal));
    }

    /** The caller's own driver account. Used by other services too. */
    public Driver loadCurrent(AuthPrincipal principal) {
        principal.requireRole(Role.DRIVER);
        return driverRepository.findById(principal.id())
                .orElseThrow(() -> ApiException.unauthorized("Account not found. Please log in again."));
    }
}
