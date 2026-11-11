package com.application.cabapp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.application.cabapp.dto.request.LoginRequest;
import com.application.cabapp.dto.request.UserRegisterRequest;
import com.application.cabapp.dto.request.UserUpdateRequest;
import com.application.cabapp.dto.response.AuthResponse;
import com.application.cabapp.dto.response.UserResponse;
import com.application.cabapp.exception.ApiException;
import com.application.cabapp.model.User;
import com.application.cabapp.repository.UserRepository;
import com.application.cabapp.security.AuthPrincipal;
import com.application.cabapp.security.JwtService;
import com.application.cabapp.security.PasswordService;
import com.application.cabapp.security.Role;

/** Rider accounts: registration, login and managing your own profile. */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordService passwordService, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw ApiException.conflict("Username already exists");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email().trim());
        user.setContact(request.contact().trim());
        user.setPassword(passwordService.hash(request.password()));
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public AuthResponse<UserResponse> login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .filter(u -> passwordService.matches(request.password(), u.getPassword()))
                .orElseThrow(() -> ApiException.unauthorized("Invalid username or password"));

        if (passwordService.needsRehash(user.getPassword())) {
            // Upgrade a legacy plaintext password to a BCrypt hash.
            user.setPassword(passwordService.hash(request.password()));
        }
        String token = jwtService.generateToken(new AuthPrincipal(user.getId(), Role.USER));
        return new AuthResponse<>(token, jwtService.getExpirationSeconds(), UserResponse.from(user));
    }

    public UserResponse getCurrent(AuthPrincipal principal) {
        return UserResponse.from(loadCurrent(principal));
    }

    @Transactional
    public UserResponse updateCurrent(AuthPrincipal principal, UserUpdateRequest request) {
        User user = loadCurrent(principal);
        user.setEmail(request.email().trim());
        user.setContact(request.contact().trim());
        if (request.password() != null && !request.password().isEmpty()) {
            user.setPassword(passwordService.hash(request.password()));
        }
        return UserResponse.from(user);
    }

    @Transactional
    public void deleteCurrent(AuthPrincipal principal) {
        userRepository.delete(loadCurrent(principal));
    }

    /** The caller's own account. There is no way to load someone else's. */
    private User loadCurrent(AuthPrincipal principal) {
        principal.requireRole(Role.USER);
        return userRepository.findById(principal.id())
                .orElseThrow(() -> ApiException.unauthorized("Account not found. Please log in again."));
    }
}
