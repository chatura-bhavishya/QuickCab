// Small display helpers shared by the ride screens.

export const formatDistance = (km) => `${Number(km).toFixed(1)} km`;

export const formatFare = (amount) => `₹${Math.round(Number(amount))}`;

/** [lat, lng] from a ride's stored coordinates, or null if it has none. */
export function ridePins(ride) {
  if (!ride || ride.pickupLat == null || ride.dropLat == null) return { pickup: null, drop: null };
  return {
    pickup: [ride.pickupLat, ride.pickupLng],
    drop: [ride.dropLat, ride.dropLng]
  };
}

export const hasRoute = (ride) => ride?.pickupLat != null && ride?.dropLat != null;

/** Short label for a spot picked on the map, used when the text box is empty. */
export const pinLabel = ([lat, lng]) => `Pinned location (${lat.toFixed(4)}, ${lng.toFixed(4)})`;
