package com.application.cabapp.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-that-is-long-enough-123";
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private JwtService serviceAt(Instant instant) {
        return new JwtService(SECRET, Duration.ofMinutes(30), Clock.fixed(instant, ZoneOffset.UTC));
    }

    @Test
    void roundTripsIdAndRole() {
        JwtService jwt = serviceAt(NOW);
        String token = jwt.generateToken(new AuthPrincipal(42L, Role.DRIVER));

        assertThat(jwt.parse(token)).contains(new AuthPrincipal(42L, Role.DRIVER));
    }

    @Test
    void rejectsExpiredTokens() {
        String token = serviceAt(NOW).generateToken(new AuthPrincipal(1L, Role.USER));

        assertThat(serviceAt(NOW.plus(Duration.ofMinutes(31))).parse(token)).isEmpty();
    }

    @Test
    void rejectsTamperedTokens() {
        JwtService jwt = serviceAt(NOW);
        String token = jwt.generateToken(new AuthPrincipal(1L, Role.USER));
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");

        assertThat(jwt.parse(tampered)).isEmpty();
        assertThat(jwt.parse("not-a-jwt")).isEmpty();
    }

    @Test
    void rejectsTokensSignedWithAnotherSecret() {
        JwtService other = new JwtService("a-completely-different-secret-value-456", Duration.ofMinutes(30),
                Clock.fixed(NOW, ZoneOffset.UTC));
        String token = other.generateToken(new AuthPrincipal(1L, Role.USER));

        assertThat(serviceAt(NOW).parse(token)).isEmpty();
    }

    @Test
    void refusesShortOrMissingSecret() {
        assertThatThrownBy(() -> new JwtService("too-short", Duration.ofMinutes(1), Clock.systemUTC()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new JwtService(null, Duration.ofMinutes(1), Clock.systemUTC()))
                .isInstanceOf(IllegalStateException.class);
    }
}
