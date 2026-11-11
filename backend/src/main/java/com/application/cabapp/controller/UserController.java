package com.application.cabapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.application.cabapp.dto.request.LoginRequest;
import com.application.cabapp.dto.request.UserRegisterRequest;
import com.application.cabapp.dto.request.UserUpdateRequest;
import com.application.cabapp.dto.response.AuthResponse;
import com.application.cabapp.dto.response.UserResponse;
import com.application.cabapp.security.AuthPrincipal;
import com.application.cabapp.service.UserService;

import jakarta.validation.Valid;

/** Rider accounts. register/login are public; /me requires a rider token. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody UserRegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthPrincipal principal) {
        return userService.getCurrent(principal);
    }

    @PutMapping("/me")
    public UserResponse update(@AuthenticationPrincipal AuthPrincipal principal,
                               @Valid @RequestBody UserUpdateRequest request) {
        return userService.updateCurrent(principal, request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthPrincipal principal) {
        userService.deleteCurrent(principal);
    }
}
