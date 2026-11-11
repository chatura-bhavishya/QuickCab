import { describe, expect, it } from 'vitest';
import { ROLES } from '../constants';
import apiClient from './apiClient';
import { saveSession } from './session';

// Run the request interceptor directly, without making a network call.
const applyRequestInterceptor = (config) =>
  apiClient.interceptors.request.handlers[0].fulfilled({ headers: {}, ...config });

describe('apiClient', () => {
  it('sends the JWT when logged in', () => {
    saveSession(ROLES.USER, { token: 'jwt-abc', expiresInSeconds: 60, account: { id: 1 } });

    expect(applyRequestInterceptor({}).headers.Authorization).toBe('Bearer jwt-abc');
  });

  it('sends no Authorization header when logged out', () => {
    expect(applyRequestInterceptor({}).headers.Authorization).toBeUndefined();
  });
});
