package com.application.cabapp.model;

import java.util.Set;

/**
 * Ride status values. Stored as plain strings in the database, so these
 * constants must keep their exact spelling.
 */
public final class RideStatus {
    public static final String PENDING = "PENDING";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String ON_THE_WAY = "ON_THE_WAY";
    public static final String ARRIVED = "ARRIVED";
    public static final String STARTED = "STARTED";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED_BY_USER = "CANCELLED_BY_USER";
    public static final String CANCELLED_BY_DRIVER = "CANCELLED_BY_DRIVER";

    /** Statuses from which a ride may still be cancelled (before it has started). */
    public static final Set<String> CANCELLABLE = Set.of(PENDING, ACCEPTED, ON_THE_WAY, ARRIVED);

    private RideStatus() {}
}
