// Ride API calls. The server identifies the rider/driver from the token,
// so no account ids are sent.
import apiClient from './apiClient';

const get = async (url) => (await apiClient.get(url)).data;
const post = async (url, body) => (await apiClient.post(url, body)).data;

// Rider
export const bookRide = ({ pickup, drop, vehicleType }) => post('/rides/book', { pickup, drop, vehicleType });
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
