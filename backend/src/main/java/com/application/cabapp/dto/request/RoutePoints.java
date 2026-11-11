package com.application.cabapp.dto.request;

/**
 * Optional map pins for a trip. Either all four values are set or none are;
 * request records validate the ranges and call {@link #isPartial()}.
 */
public interface RoutePoints {
    Double pickupLat();
    Double pickupLng();
    Double dropLat();
    Double dropLng();

    default boolean hasPins() {
        return pickupLat() != null && pickupLng() != null && dropLat() != null && dropLng() != null;
    }

    default boolean isPartial() {
        boolean any = pickupLat() != null || pickupLng() != null || dropLat() != null || dropLng() != null;
        return any && !hasPins();
    }
}
