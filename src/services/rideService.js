// Ride API calls. The server identifies the rider/driver from the token,
// so no account ids are sent.
import apiClient from './apiClient';

const get = async (url) => (await apiClient.get(url)).data;
const post = async (url, body) => (await apiClient.post(url, body)).data;

// [lat, lng] pins -> the flat fields the API expects (both pins or neither).
const pinFields = (pickupPin, dropPin) => (pickupPin && dropPin
  ? { pickupLat: pickupPin[0], pickupLng: pickupPin[1], dropLat: dropPin[0], dropLng: dropPin[1] }
  : {});

// Rider
export const bookRide = ({ pickup, drop, vehicleType, pickupPin, dropPin }) =>
  post('/rides/book', { pickup, drop, vehicleType, ...pinFields(pickupPin, dropPin) });
export const estimateFare = (vehicleType, pickupPin, dropPin) =>
  post('/rides/estimate', { vehicleType, ...pinFields(pickupPin, dropPin) });
export const getPastLocations = () => get('/rides/locations');
export const rateDriver = (rideId, rating) => post(`/rides/${rideId}/rate`, { rating });

// Driver
export const getPendingRides = () => get('/rides/pending');
export const acceptRide = (rideId) => post(`/rides/${rideId}/accept`);
export const denyRide = (rideId) => post(`/rides/${rideId}/deny`);
export const updateRideStatus = (rideId, status) => post(`/rides/${rideId}/status`, { status });
export const startRide = (rideId, otp) => post(`/rides/${rideId}/start`, { otp });
export const completeRide = (rideId) => post(`/rides/${rideId}/pay`);

// Either
export const getRideDetails = (rideId) => get(`/rides/${rideId}`);
export const getMyRides = () => get('/rides/mine');
export const cancelRide = (rideId) => post(`/rides/${rideId}/cancel`);
