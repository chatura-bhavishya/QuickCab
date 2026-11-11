// Registration, login and logout for riders and drivers.
import { ROLES } from '../constants';
import apiClient from './apiClient';
import { clearSession, saveSession } from './session';

const BASE_PATHS = {
  [ROLES.USER]: '/users',
  [ROLES.DRIVER]: '/drivers'
};

export async function register(role, form) {
  const { data } = await apiClient.post(`${BASE_PATHS[role]}/register`, form);
  return data;
}

/** Logs in and stores the session. Resolves to the account. */
export async function login(role, credentials) {
  const { data } = await apiClient.post(`${BASE_PATHS[role]}/login`, credentials);
  saveSession(role, data);
  return data.account;
}

/** Tokens are stateless, so logging out just forgets the token on this device. */
export function logout() {
  clearSession();
}

export const accountPath = (role) => `${BASE_PATHS[role]}/me`;
