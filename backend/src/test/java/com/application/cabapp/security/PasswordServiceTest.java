package com.application.cabapp.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.application.cabapp.exception.ApiException;

class PasswordServiceTest {

    private final PasswordService passwords = new PasswordService();

    @Test
    void hashesAndVerifiesPasswords() {
        String hash = passwords.hash("secret12");

        assertThat(hash).isNotEqualTo("secret12").startsWith("$2");
        assertThat(passwords.matches("secret12", hash)).isTrue();
        assertThat(passwords.matches("wrong", hash)).isFalse();
        assertThat(passwords.needsRehash(hash)).isFalse();
    }

    @Test
    void acceptsLegacyPlaintextAndFlagsItForRehash() {
        assertThat(passwords.matches("old-pass", "old-pass")).isTrue();
        assertThat(passwords.matches("other", "old-pass")).isFalse();
        assertThat(passwords.needsRehash("old-pass")).isTrue();
    }

    @Test
    void rejectsNullsAndOverlongPasswords() {
        assertThat(passwords.matches(null, "x")).isFalse();
        assertThat(passwords.matches("x", null)).isFalse();
        assertThatThrownBy(() -> passwords.hash("x".repeat(73))).isInstanceOf(ApiException.class);
    }
}
