package com.application.cabapp.config;

import java.time.Clock;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.application.cabapp.security.AttemptLimiter;

/** Limits for guessing passwords and ride OTPs. */
@Configuration
public class RateLimitConfig {

    @Bean
    public AttemptLimiter loginAttemptLimiter(
            @Value("${app.rate-limit.login.max-failures:5}") int maxFailures,
            @Value("${app.rate-limit.login.window-minutes:15}") long windowMinutes) {
        return new AttemptLimiter(maxFailures, Duration.ofMinutes(windowMinutes), Clock.systemUTC());
    }

    // A 4-digit OTP only has 9000 possibilities, so a handful of tries is plenty.
    @Bean
    public AttemptLimiter otpAttemptLimiter(
            @Value("${app.rate-limit.otp.max-failures:5}") int maxFailures,
            @Value("${app.rate-limit.otp.window-minutes:15}") long windowMinutes) {
        return new AttemptLimiter(maxFailures, Duration.ofMinutes(windowMinutes), Clock.systemUTC());
    }
}
