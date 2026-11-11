package com.application.cabapp.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Counts failed attempts per key (a username, a ride id...) and blocks the key
 * once it hits the limit, until the window runs out.
 *
 * Kept in memory on purpose: it's enough for a single instance. With several
 * instances behind a load balancer this would move to Redis.
 */
public class AttemptLimiter {

    private final int maxFailures;
    private final Duration window;
    private final Clock clock;
    private final Map<String, Failures> failuresByKey = new ConcurrentHashMap<>();

    private record Failures(Instant firstFailure, int count) {}

    public AttemptLimiter(int maxFailures, Duration window, Clock clock) {
        this.maxFailures = maxFailures;
        this.window = window;
        this.clock = clock;
    }

    public boolean isBlocked(String key) {
        Failures failures = current(key);
        return failures != null && failures.count() >= maxFailures;
    }

    public void recordFailure(String key) {
        Instant now = clock.instant();
        failuresByKey.compute(key, (k, existing) ->
                existing == null || expired(existing, now)
                        ? new Failures(now, 1)
                        : new Failures(existing.firstFailure(), existing.count() + 1));
    }

    public void reset(String key) {
        failuresByKey.remove(key);
    }

    public Duration getWindow() {
        return window;
    }

    private Failures current(String key) {
        Failures failures = failuresByKey.get(key);
        if (failures != null && expired(failures, clock.instant())) {
            failuresByKey.remove(key, failures);
            return null;
        }
        return failures;
    }

    private boolean expired(Failures failures, Instant now) {
        return !now.isBefore(failures.firstFailure().plus(window));
    }
}
