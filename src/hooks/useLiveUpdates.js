import { useEffect, useRef, useState } from 'react';
import { createLiveClient } from '../services/liveUpdates';
import { getToken } from '../services/session';

/**
 * Subscribes to STOMP topics while the component is mounted and calls
 * `onEvent(payload)` for each message. Falsy topics are ignored, so callers
 * can pass e.g. `activeRide && '/topic/rides/' + activeRide.id`.
 *
 * Returns true while connected, so callers can slow down their polling.
 */
export function useLiveUpdates(topics, onEvent) {
  const [connected, setConnected] = useState(false);
  const latest = useRef(onEvent);

  useEffect(() => {
    latest.current = onEvent;
  });

  // A stable key so the effect only reconnects when the topic list really changes.
  const topicKey = topics.filter(Boolean).join('|');

  useEffect(() => {
    const token = getToken();
    const topicList = topicKey ? topicKey.split('|') : [];
    if (!token || topicList.length === 0) return undefined;

    const client = createLiveClient(token);
    client.onConnect = () => {
      setConnected(true);
      topicList.forEach(topic => client.subscribe(topic, (message) => {
        let payload = {};
        try {
          payload = JSON.parse(message.body);
        } catch {
          // Not JSON - still worth a refresh.
        }
        latest.current(payload);
      }));
    };
    client.onWebSocketClose = () => setConnected(false);
    client.onStompError = () => setConnected(false);
    client.activate();

    return () => {
      setConnected(false);
      client.deactivate();
    };
  }, [topicKey]);

  return connected;
}
