// The logged-in session: { token, role, account, expiresAt } in localStorage.
import { ROLES, STORAGE_KEYS } from '../constants';

const MS_PER_SECOND = 1000;

// Older versions stored the account (and even its password) without a token.
STORAGE_KEYS.LEGACY.forEach(key => localStorage.removeItem(key));

export function getSession() {
  let session;
  try {
    session = JSON.parse(localStorage.getItem(STORAGE_KEYS.SESSION) || 'null');
  } catch {
    session = null;
  }
  if (!session?.token || !session.account || (session.expiresAt && Date.now() >= session.expiresAt)) {
    if (session) clearSession(); // corrupted or expired
    return null;
  }
  return session;
}

/** Stores the login response ({ token, expiresInSeconds, account }) for the given role. */
export function saveSession(role, { token, expiresInSeconds, account }) {
  const session = {
    token,
    role,
    account,
    expiresAt: expiresInSeconds ? Date.now() + expiresInSeconds * MS_PER_SECOND : null
  };
  localStorage.setItem(STORAGE_KEYS.SESSION, JSON.stringify(session));
}

/** Replaces the cached account details (e.g. after a profile update). */
export function updateSessionAccount(account) {
  const session = getSession();
  if (session) localStorage.setItem(STORAGE_KEYS.SESSION, JSON.stringify({ ...session, account }));
}

export function clearSession() {
  localStorage.removeItem(STORAGE_KEYS.SESSION);
}

export const getToken = () => getSession()?.token || null;
export const getRole = () => getSession()?.role || null;

export function getUser() {
  const session = getSession();
  return session?.role === ROLES.USER ? session.account : null;
}

export function getDriver() {
  const session = getSession();
  return session?.role === ROLES.DRIVER ? session.account : null;
}
