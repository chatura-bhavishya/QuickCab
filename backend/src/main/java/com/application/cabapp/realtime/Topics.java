package com.application.cabapp.realtime;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** STOMP topic names, kept in one place for the publisher and the security check. */
public final class Topics {

    public static final String PREFIX = "/topic";
    private static final Pattern RIDE = Pattern.compile("^/topic/rides/(\\d+)$");
    private static final Pattern PENDING = Pattern.compile("^/topic/pending/(car|bike|auto)$");

    private Topics() {}

    public static String ride(Long rideId) {
        return PREFIX + "/rides/" + rideId;
    }

    public static String pending(String vehicleType) {
        return PREFIX + "/pending/" + vehicleType;
    }

    public static Optional<Long> rideIdOf(String destination) {
        Matcher m = RIDE.matcher(destination == null ? "" : destination);
        return m.matches() ? Optional.of(Long.valueOf(m.group(1))) : Optional.empty();
    }

    public static Optional<String> vehicleTypeOf(String destination) {
        Matcher m = PENDING.matcher(destination == null ? "" : destination);
        return m.matches() ? Optional.of(m.group(1)) : Optional.empty();
    }
}
