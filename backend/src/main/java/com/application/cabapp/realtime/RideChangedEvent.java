package com.application.cabapp.realtime;

/**
 * Raised by the services whenever a ride (or its chat) changes.
 *
 * @param rideId              the ride that changed
 * @param vehicleType         its vehicle type, so drivers of that type can refresh their list
 * @param pendingListChanged  true if the ride entered or left the PENDING list
 * @param kind                what changed, so clients can skip work (e.g. only reload chat)
 */
public record RideChangedEvent(Long rideId, String vehicleType, boolean pendingListChanged, Kind kind) {

    public enum Kind { RIDE, CHAT }

    public static RideChangedEvent ride(Long rideId, String vehicleType, boolean pendingListChanged) {
        return new RideChangedEvent(rideId, vehicleType, pendingListChanged, Kind.RIDE);
    }

    public static RideChangedEvent chat(Long rideId, String vehicleType) {
        return new RideChangedEvent(rideId, vehicleType, false, Kind.CHAT);
    }
}
