// STOMP-over-WebSocket client for live ride updates.
import { Client } from '@stomp/stompjs';
import { LIVE_RECONNECT_DELAY_MS } from '../constants';
import { API_BASE_URL } from './apiClient';

const HEARTBEAT_MS = 10000;

/**
 * ws(s)://host/ws, derived from the API URL (…/api -> …/ws) unless
 * VITE_WS_URL says otherwise.
 */
export function liveUpdatesUrl(apiBase = API_BASE_URL, explicit = import.meta.env.VITE_WS_URL) {
  if (explicit) return explicit;
  const url = new URL(apiBase, window.location.origin);
  url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:';
  url.pathname = `${url.pathname.replace(/\/api\/?$/, '')}/ws`;
  return url.toString();
}

/** A client that sends the JWT on CONNECT (browsers can't set headers on the handshake). */
export function createLiveClient(token) {
  return new Client({
    brokerURL: liveUpdatesUrl(),
    connectHeaders: { Authorization: `Bearer ${token}` },
    reconnectDelay: LIVE_RECONNECT_DELAY_MS,
    heartbeatIncoming: HEARTBEAT_MS,
    heartbeatOutgoing: HEARTBEAT_MS
  });
}
