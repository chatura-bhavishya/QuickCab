import { describe, expect, it } from 'vitest';
import { liveUpdatesUrl } from './liveUpdates';

describe('liveUpdatesUrl', () => {
  it('swaps the /api path for /ws and http for ws', () => {
    expect(liveUpdatesUrl('http://localhost:8080/api', undefined)).toBe('ws://localhost:8080/ws');
    expect(liveUpdatesUrl('https://quickcab.example.com/api/', undefined)).toBe('wss://quickcab.example.com/ws');
  });

  it('uses an explicit URL when one is configured', () => {
    expect(liveUpdatesUrl('http://localhost:8080/api', 'wss://live.example.com/ws')).toBe('wss://live.example.com/ws');
  });
});
