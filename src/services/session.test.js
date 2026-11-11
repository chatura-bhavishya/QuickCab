import { afterEach, describe, expect, it, vi } from 'vitest';
import { ROLES, STORAGE_KEYS } from '../constants';
import {
  clearSession, getDriver, getSession, getToken, getUser, saveSession, updateSessionAccount
} from './session';

const login = { token: 'jwt-123', expiresInSeconds: 3600, account: { id: 1, username: 'rider1' } };

describe('session', () => {
  afterEach(() => vi.useRealTimers());

  it('stores the token and account for the role', () => {
    saveSession(ROLES.USER, login);

    expect(getToken()).toBe('jwt-123');
    expect(getUser()).toEqual(login.account);
    expect(getDriver()).toBeNull();
  });

  it('expires sessions', () => {
    vi.useFakeTimers();
    saveSession(ROLES.DRIVER, login);
    expect(getDriver()).not.toBeNull();

    vi.advanceTimersByTime(3600 * 1000);
    expect(getSession()).toBeNull();
    expect(localStorage.getItem(STORAGE_KEYS.SESSION)).toBeNull();
  });

  it('ignores corrupted storage', () => {
    localStorage.setItem(STORAGE_KEYS.SESSION, '{not json');
    expect(getSession()).toBeNull();
  });

  it('updates the cached account and clears on logout', () => {
    saveSession(ROLES.USER, login);
    updateSessionAccount({ id: 1, username: 'rider1', email: 'new@example.com' });
    expect(getUser().email).toBe('new@example.com');

    clearSession();
    expect(getUser()).toBeNull();
  });
});
