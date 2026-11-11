package com.application.cabapp.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordServiceTest {

    private final PasswordService passwords = new PasswordService();

    @Test
    void hashesAndVerifiesPasswords() {
        String hash = passwords.hash("secret12");
        assertNotEquals("secret12", hash);
        assertTrue(passwords.matches("secret12", hash));
        assertFalse(passwords.matches("wrong", hash));
        assertFalse(passwords.needsRehash(hash));
    }

    @Test
    void acceptsLegacyPlaintextAndFlagsItForRehash() {
        assertTrue(passwords.matches("old-pass", "old-pass"));
        assertFalse(passwords.matches("other", "old-pass"));
        assertTrue(passwords.needsRehash("old-pass"));
    }

    @Test
    void rejectsNulls() {
        assertFalse(passwords.matches(null, "x"));
        assertFalse(passwords.matches("x", null));
    }
}
