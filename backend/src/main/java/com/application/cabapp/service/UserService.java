package com.application.cabapp.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.application.cabapp.model.User;
import com.application.cabapp.repository.UserRepository;
import com.application.cabapp.security.PasswordService;
import com.application.cabapp.validation.InputValidator;

/**
 * UserService — owns ALL business logic for the User entity.
 * Controllers should delegate directly here and stay thin.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordService passwordService;

    // ---------- Basic CRUD ----------

    public Optional<User> getUserById(Long id) {
        return userRepo.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public User updateUser(Long id, User updated) {
        return userRepo.findById(id).map(u -> {
            u.setEmail(updated.getEmail());
            u.setContact(updated.getContact());
            if (updated.getPassword() != null && !updated.getPassword().isEmpty()) {
                u.setPassword(passwordService.hash(updated.getPassword()));
            }
            return userRepo.save(u);
        }).orElse(null);
    }

    public void deleteUser(Long id) {
        userRepo.deleteById(id);
    }

    // ---------- Business operations ----------

    /** Register a new user. Returns the saved user, or a 400 if invalid / username exists. */
    public ResponseEntity<?> register(User user) {
        String error = InputValidator.firstError(
                InputValidator.username(user.getUsername()),
                InputValidator.email(user.getEmail()),
                InputValidator.password(user.getPassword()),
                InputValidator.contact(user.getContact()));
        if (error != null) return ResponseEntity.badRequest().body(error);

        if (getUserByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        // Never let a client-supplied id overwrite an existing record.
        user.setId(null);
        user.setPassword(passwordService.hash(user.getPassword()));
        return ResponseEntity.ok(userRepo.save(user));
    }

    /** Authenticate a user against username/password from a request body map. */
    public ResponseEntity<?> login(Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        Optional<User> found = username == null ? Optional.empty() : getUserByUsername(username);

        if (found.isPresent() && passwordService.matches(password, found.get().getPassword())) {
            User user = found.get();
            if (passwordService.needsRehash(user.getPassword())) {
                // Upgrade a legacy plaintext password to a BCrypt hash.
                user.setPassword(passwordService.hash(password));
                userRepo.save(user);
            }
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
    }

    /** Fetch a user by id, returning 404 if absent. */
    public ResponseEntity<?> getById(Long id) {
        return getUserById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Update user, returning 400 if invalid or 404 if not found. */
    public ResponseEntity<?> update(Long id, User updated) {
        String error = InputValidator.firstError(
                InputValidator.email(updated.getEmail()),
                InputValidator.contact(updated.getContact()),
                InputValidator.optionalPassword(updated.getPassword()));
        if (error != null) return ResponseEntity.badRequest().body(error);

        User u = updateUser(id, updated);
        if (u == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(u);
    }

    /** Delete user. */
    public ResponseEntity<?> delete(Long id) {
        deleteUser(id);
        return ResponseEntity.ok("Deleted");
    }
}
