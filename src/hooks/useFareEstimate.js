import { useEffect, useState } from 'react';
import { estimateFare } from '../services/rideService';

/**
 * Asks the server for the distance and fare once both pins are set.
 * Returns { estimate, loading, error }; estimate is null without pins.
 */
export function useFareEstimate(vehicleType, pickupPin, dropPin) {
  const [state, setState] = useState({ estimate: null, loading: false, error: '' });

  useEffect(() => {
    if (!pickupPin || !dropPin) {
      setState({ estimate: null, loading: false, error: '' });
      return undefined;
    }
    let cancelled = false; // ignore answers that arrive after the pins moved again
    setState(s => ({ ...s, loading: true, error: '' }));
    estimateFare(vehicleType, pickupPin, dropPin)
      .then(estimate => { if (!cancelled) setState({ estimate, loading: false, error: '' }); })
      .catch(e => {
        if (cancelled) return;
        const message = e?.response?.data?.error || 'Could not estimate the fare right now.';
        setState({ estimate: null, loading: false, error: message });
      });
    return () => { cancelled = true; };
  }, [vehicleType, pickupPin, dropPin]);

  return state;
}
