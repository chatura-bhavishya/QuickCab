package com.application.cabapp.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Hashes and verifies passwords with BCrypt.
 *
 * Accounts created before hashing was introduced still have plaintext
 * passwords in the database. {@link #matches} accepts those, and callers use
 * {@link #needsRehash} to transparently upgrade them on the next successful login.
 */
@Component
public class PasswordService {

    /** BCrypt cannot hash inputs longer than 72 bytes. */
    private static final int MAX_BCRYPT_BYTES = 72;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) return false;
        if (isBcryptHash(storedPassword)) {
            if (rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_BCRYPT_BYTES) return false;
            return encoder.matches(rawPassword, storedPassword);
        }
        // Legacy plaintext password: constant-time comparison.
        return MessageDigest.isEqual(
                rawPassword.getBytes(StandardCharsets.UTF_8),
                storedPassword.getBytes(StandardCharsets.UTF_8));
    }

    /** True when a stored password is legacy plaintext that can be upgraded to a hash. */
    public boolean needsRehash(String storedPassword) {
        return storedPassword != null && !isBcryptHash(storedPassword)
                && storedPassword.getBytes(StandardCharsets.UTF_8).length <= MAX_BCRYPT_BYTES;
    }

    private static boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
