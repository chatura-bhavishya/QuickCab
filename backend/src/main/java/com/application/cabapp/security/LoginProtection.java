package com.application.cabapp.security;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.application.cabapp.exception.ApiException;

/**
 * Slows down password guessing: after too many failed logins for one account,
 * further attempts are refused for a while (HTTP 429).
 *
 * Note the trade-off: keying on the username means someone could lock a user
 * out temporarily by spamming wrong passwords. Adding the client IP to the key
 * is the usual next step.
 */
@Component
public class LoginProtection {

    private final AttemptLimiter limiter;

    public LoginProtection(@Qualifier("loginAttemptLimiter") AttemptLimiter limiter) {
        this.limiter = limiter;
    }

    public void checkAllowed(Role role, String username) {
        if (limiter.isBlocked(key(role, username))) {
            throw ApiException.tooManyRequests("Too many failed login attempts. Please try again in "
                    + limiter.getWindow().toMinutes() + " minutes.");
        }
    }

    public void recordFailure(Role role, String username) {
        limiter.recordFailure(key(role, username));
    }

    public void recordSuccess(Role role, String username) {
        limiter.reset(key(role, username));
    }

    private static String key(Role role, String username) {
        return role.name() + ":" + username.toLowerCase(Locale.ROOT);
    }
}
