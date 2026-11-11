// The logged-in person's own profile (rider or driver).
import apiClient from './apiClient';
import { accountPath } from './authService';
import { clearSession, updateSessionAccount } from './session';

export async function getMyAccount(role) {
  const { data } = await apiClient.get(accountPath(role));
  updateSessionAccount(data);
  return data;
}

export async function updateMyAccount(role, changes) {
  const { data } = await apiClient.put(accountPath(role), changes);
  updateSessionAccount(data);
  return data;
}

export async function deleteMyAccount(role) {
  await apiClient.delete(accountPath(role));
  clearSession();
}
