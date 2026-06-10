package com.application.cabapp.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.application.cabapp.model.User;
import com.application.cabapp.repository.UserRepository;

/**
 * UserService — owns ALL business logic for the User entity.
 * Controllers should delegate directly here and stay thin.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    // ---------- Basic CRUD ----------

    public User saveUser(User user) {
        return userRepo.save(user);
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

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
                u.setPassword(updated.getPassword());
            }
            return userRepo.save(u);
        }).orElse(null);
    }

    public void deleteUser(Long id) {
        userRepo.deleteById(id);
    }

    // ---------- Business operations (moved out of controller) ----------

    /** Register a new user. Returns the saved user, or a 400 if username exists. */
    public ResponseEntity<?> register(User user) {
        if (getUserByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        return ResponseEntity.ok(saveUser(user));
    }

    /** Authenticate a user against username/password from a request body map. */
    public ResponseEntity<?> login(Map<String, String> body) {
        Optional<User> u = getUserByUsername(body.get("username"));
        if (u.isPresent() && u.get().getPassword().equals(body.get("password"))) {
            return ResponseEntity.ok(u.get());
        }
        Map<String, String> err = new HashMap<>();
        err.put("error", "Invalid credentials");
        return ResponseEntity.status(401).body(err);
    }

    /** Fetch a user by id, returning 404 if absent. */
    public ResponseEntity<?> getById(Long id) {
        return getUserById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Update user, returning 404 if not found. */
    public ResponseEntity<?> update(Long id, User updated) {
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
