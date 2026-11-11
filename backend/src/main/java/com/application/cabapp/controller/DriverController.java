package com.application.cabapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.application.cabapp.dto.request.DriverRegisterRequest;
import com.application.cabapp.dto.request.DriverUpdateRequest;
import com.application.cabapp.dto.request.LoginRequest;
import com.application.cabapp.dto.response.AuthResponse;
import com.application.cabapp.dto.response.DriverResponse;
import com.application.cabapp.security.AuthPrincipal;
import com.application.cabapp.service.DriverService;

import jakarta.validation.Valid;

/** Driver accounts. register/login are public; /me requires a driver token. */
@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public DriverResponse register(@Valid @RequestBody DriverRegisterRequest request) {
        return driverService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse<DriverResponse> login(@Valid @RequestBody LoginRequest request) {
        return driverService.login(request);
    }

    @GetMapping("/me")
    public DriverResponse me(@AuthenticationPrincipal AuthPrincipal principal) {
        return driverService.getCurrent(principal);
    }

    @PutMapping("/me")
    public DriverResponse update(@AuthenticationPrincipal AuthPrincipal principal,
                                 @Valid @RequestBody DriverUpdateRequest request) {
        return driverService.updateCurrent(principal, request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthPrincipal principal) {
        driverService.deleteCurrent(principal);
    }
}
