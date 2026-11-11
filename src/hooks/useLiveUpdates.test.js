import { act, renderHook } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ROLES } from '../constants';
import { saveSession } from '../services/session';
import { useLiveUpdates } from './useLiveUpdates';

// A fake STOMP client so the hook can be tested without a server. (A plain class,
// because the test config restores vi.fn() implementations between tests.)
const clients = [];
vi.mock('@stomp/stompjs', () => ({
  Client: class {
    constructor(config) {
      this.config = config;
      this.handlers = {};
      this.activated = false;
      this.deactivated = false;
      clients.push(this);
    }
    activate() { this.activated = true; }
    deactivate() { this.deactivated = true; }
    subscribe(topic, handler) { this.handlers[topic] = handler; }
  }
}));

describe('useLiveUpdates', () => {
  beforeEach(() => {
    clients.length = 0;
    saveSession(ROLES.USER, { token: 'jwt-1', expiresInSeconds: 60, account: { id: 1 } });
  });

  it('connects with the token, subscribes, and forwards messages', () => {
    const onEvent = vi.fn();
    const { result } = renderHook(() => useLiveUpdates(['/topic/rides/7', null], onEvent));

    const client = clients[0];
    expect(client.config.connectHeaders.Authorization).toBe('Bearer jwt-1');
    expect(client.activated).toBe(true);
    expect(result.current).toBe(false);

    act(() => client.onConnect());
    expect(result.current).toBe(true);
    expect(Object.keys(client.handlers)).toEqual(['/topic/rides/7']);

    client.handlers['/topic/rides/7']({ body: '{"type":"RIDE","rideId":7}' });
    expect(onEvent).toHaveBeenCalledWith({ type: 'RIDE', rideId: 7 });
  });

  it('disconnects on unmount', () => {
    const { unmount } = renderHook(() => useLiveUpdates(['/topic/rides/7'], vi.fn()));
    unmount();
    expect(clients[0].deactivated).toBe(true);
  });

  it('does nothing without a session or topics', () => {
    renderHook(() => useLiveUpdates([null, undefined], vi.fn()));
    expect(clients).toHaveLength(0);
  });
});
