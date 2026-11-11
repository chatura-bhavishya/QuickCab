// In-ride chat. The sender (rider or driver) is taken from the token.
import apiClient from './apiClient';

export async function getMessages(rideId) {
  const { data } = await apiClient.get(`/chat/${rideId}`);
  return data;
}

export async function sendMessage(rideId, message) {
  const { data } = await apiClient.post('/chat/send', { rideId: Number(rideId), message });
  return data;
}
