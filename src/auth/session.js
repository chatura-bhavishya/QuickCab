// Reads and writes the logged-in user / driver kept in localStorage.
import { STORAGE_KEYS } from '../constants';

function read(key) {
  let value;
  try {
    value = JSON.parse(localStorage.getItem(key) || 'null');
  } catch {
    // Corrupted entry: drop it rather than crash the app.
    localStorage.removeItem(key);
    return null;
  }
  // Older versions of the app stored the plaintext password here; scrub it.
  if (value && 'password' in value) {
    delete value.password;
    localStorage.setItem(key, JSON.stringify(value));
  }
  return value;
}

function write(key, account) {
  // Never persist a password in the browser, even if the API returned one.
  const { password, ...safe } = account || {};
  localStorage.setItem(key, JSON.stringify(safe));
}

export const getUser = () => read(STORAGE_KEYS.USER);
export const getDriver = () => read(STORAGE_KEYS.DRIVER);
export const saveUser = (user) => write(STORAGE_KEYS.USER, user);
export const saveDriver = (driver) => write(STORAGE_KEYS.DRIVER, driver);

export function clearSession() {
  localStorage.removeItem(STORAGE_KEYS.USER);
  localStorage.removeItem(STORAGE_KEYS.DRIVER);
}
