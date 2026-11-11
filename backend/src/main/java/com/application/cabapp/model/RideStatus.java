package com.application.cabapp.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Lifecycle of a ride. Stored by name (EnumType.STRING), so the constant
 * names must never be renamed.
 *
 * PENDING -> ACCEPTED -> ON_THE_WAY -> ARRIVED -> STARTED -> COMPLETED,
 * with CANCELLED_BY_USER / CANCELLED_BY_DRIVER possible before STARTED.
 */
public enum RideStatus {
    PENDING,
    ACCEPTED,
    ON_THE_WAY,
    ARRIVED,
    STARTED,
    COMPLETED,
    CANCELLED_BY_USER,
    CANCELLED_BY_DRIVER;

    private static final Set<RideStatus> CANCELLABLE = EnumSet.of(PENDING, ACCEPTED, ON_THE_WAY, ARRIVED);

    /** A ride can be cancelled until it has started. */
    public boolean isCancellable() {
        return CANCELLABLE.contains(this);
    }
}
