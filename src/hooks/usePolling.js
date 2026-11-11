import { useEffect, useRef } from 'react';

/**
 * Calls `callback` immediately and then every `intervalMs` while `enabled`.
 * Always invokes the latest callback, so it can read fresh state without
 * restarting the timer.
 */
export function usePolling(callback, intervalMs, enabled = true) {
  const latest = useRef(callback);

  useEffect(() => {
    latest.current = callback;
  });

  useEffect(() => {
    if (!enabled) return undefined;
    const tick = () => latest.current();
    tick();
    const id = setInterval(tick, intervalMs);
    return () => clearInterval(id);
  }, [intervalMs, enabled]);
}
