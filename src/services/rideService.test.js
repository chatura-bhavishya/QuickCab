import { describe, expect, it, vi } from 'vitest';
import apiClient from './apiClient';
import { bookRide, estimateFare } from './rideService';

describe('rideService', () => {
  it('sends map pins as lat/lng fields when both are set', async () => {
    const post = vi.spyOn(apiClient, 'post').mockResolvedValue({ data: { id: 1 } });

    await bookRide({ pickup: 'A', drop: 'B', vehicleType: 'car', pickupPin: [12.9, 77.6], dropPin: [13, 77.7] });

    expect(post).toHaveBeenCalledWith('/rides/book', {
      pickup: 'A', drop: 'B', vehicleType: 'car',
      pickupLat: 12.9, pickupLng: 77.6, dropLat: 13, dropLng: 77.7
    });
  });

  it('leaves pins out when only one is set', async () => {
    const post = vi.spyOn(apiClient, 'post').mockResolvedValue({ data: { id: 1 } });

    await bookRide({ pickup: 'A', drop: 'B', vehicleType: 'bike', pickupPin: [12.9, 77.6], dropPin: null });

    expect(post).toHaveBeenCalledWith('/rides/book', { pickup: 'A', drop: 'B', vehicleType: 'bike' });
  });

  it('asks the server for a fare estimate', async () => {
    const post = vi.spyOn(apiClient, 'post').mockResolvedValue({ data: { distanceKm: 4.2, fare: 113 } });

    await expect(estimateFare('car', [1, 2], [3, 4])).resolves.toEqual({ distanceKm: 4.2, fare: 113 });
    expect(post).toHaveBeenCalledWith('/rides/estimate',
      { vehicleType: 'car', pickupLat: 1, pickupLng: 2, dropLat: 3, dropLng: 4 });
  });
});
