package com.application.cabapp.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class AttemptLimiterTest {

    /** A clock we can move forward by hand. */
    private static final class TestClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T10:00:00Z");

        void advance(Duration d) { now = now.plus(d); }
        @Override public Instant instant() { return now; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
    }

    private final TestClock clock = new TestClock();
    private final AttemptLimiter limiter = new AttemptLimiter(3, Duration.ofMinutes(15), clock);

    @Test
    void blocksAfterTheLimit() {
        limiter.recordFailure("alice");
        limiter.recordFailure("alice");
        assertThat(limiter.isBlocked("alice")).isFalse();

        limiter.recordFailure("alice");
        assertThat(limiter.isBlocked("alice")).isTrue();
        assertThat(limiter.isBlocked("bob")).isFalse();
    }

    @Test
    void unblocksWhenTheWindowPasses() {
        for (int i = 0; i < 3; i++) limiter.recordFailure("alice");

        clock.advance(Duration.ofMinutes(15));
        assertThat(limiter.isBlocked("alice")).isFalse();

        // and the count starts again from zero
        limiter.recordFailure("alice");
        assertThat(limiter.isBlocked("alice")).isFalse();
    }

    @Test
    void successResetsTheCount() {
        limiter.recordFailure("alice");
        limiter.recordFailure("alice");
        limiter.reset("alice");
        limiter.recordFailure("alice");

        assertThat(limiter.isBlocked("alice")).isFalse();
    }
}
