import { describe, expect, it } from 'vitest';
import { formatDistance, formatFare, hasRoute, pinLabel, ridePins } from './format';

describe('format helpers', () => {
  it('formats distance and fare', () => {
    expect(formatDistance(4.25)).toBe('4.3 km');
    expect(formatFare(112.6)).toBe('₹113');
  });

  it('reads pins from a ride', () => {
    const ride = { pickupLat: 12.9, pickupLng: 77.6, dropLat: 13.0, dropLng: 77.7 };
    expect(ridePins(ride)).toEqual({ pickup: [12.9, 77.6], drop: [13.0, 77.7] });
    expect(hasRoute(ride)).toBe(true);
    expect(hasRoute({ pickupLat: null })).toBe(false);
    expect(ridePins({})).toEqual({ pickup: null, drop: null });
  });

  it('labels a pinned spot', () => {
    expect(pinLabel([12.971599, 77.594566])).toBe('Pinned location (12.9716, 77.5946)');
  });
});
