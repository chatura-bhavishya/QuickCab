package com.application.cabapp.security;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Issues and verifies HMAC-SHA256 signed JWT access tokens.
 * The token subject is the account id and the "role" claim says whether it
 * belongs to a USER or a DRIVER.
 */
@Service
public class JwtService {

    private static final String ROLE_CLAIM = "role";
    /** HS256 requires a key of at least 256 bits. */
    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey key;
    private final Duration ttl;
    private final Clock clock;

    @Autowired
    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        this(secret, Duration.ofMinutes(expirationMinutes), Clock.systemUTC());
    }

    JwtService(String secret, Duration ttl, Clock clock) {
        byte[] bytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT_SECRET must be set and at least " + MIN_SECRET_BYTES + " bytes long");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.ttl = ttl;
        this.clock = clock;
    }

    public String generateToken(AuthPrincipal principal) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(String.valueOf(principal.id()))
                .claim(ROLE_CLAIM, principal.role().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    /** Returns the principal for a valid, unexpired token, or empty for anything else. */
    public Optional<AuthPrincipal> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long id = Long.valueOf(claims.getSubject());
            Role role = Role.valueOf(claims.get(ROLE_CLAIM, String.class));
            return Optional.of(new AuthPrincipal(id, role));
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            // Bad signature, expired, malformed, or missing claims.
            return Optional.empty();
        }
    }

    public long getExpirationSeconds() {
        return ttl.toSeconds();
    }
}
