import { useState } from 'react';
import { PIN_TARGETS } from '../../constants';
import { formatDistance, formatFare } from '../../utils/format';
import LazyRouteMap from './LazyRouteMap';

/**
 * Lets the rider drop pickup and drop pins on a map. Pins are optional:
 * with both set the fare is distance-based, otherwise the minimum applies.
 */
export default function PinPicker({ pickupPin, dropPin, onPin, onClear, estimate }) {
  const [target, setTarget] = useState(PIN_TARGETS.PICKUP);

  const handlePick = (position) => {
    onPin(target, position);
    // Natural flow: pickup first, then straight on to drop.
    if (target === PIN_TARGETS.PICKUP && !dropPin) setTarget(PIN_TARGETS.DROP);
  };

  return (
    <div className="form-group">
      <label id="pin-label">Pin it on the map <span className="muted">(optional, for a distance-based fare)</span></label>
      <div className="tabs" role="group" aria-labelledby="pin-label">
        <button type="button" className={target === PIN_TARGETS.PICKUP ? 'active' : ''}
          aria-pressed={target === PIN_TARGETS.PICKUP} onClick={() => setTarget(PIN_TARGETS.PICKUP)}>
          {pickupPin ? 'Move pickup pin' : 'Set pickup pin'}
        </button>
        <button type="button" className={target === PIN_TARGETS.DROP ? 'active' : ''}
          aria-pressed={target === PIN_TARGETS.DROP} onClick={() => setTarget(PIN_TARGETS.DROP)}>
          {dropPin ? 'Move drop pin' : 'Set drop pin'}
        </button>
      </div>

      <LazyRouteMap pickup={pickupPin} drop={dropPin} onPick={handlePick} label="Pick pickup and drop on the map" />

      <div className="pin-summary" aria-live="polite">
        {!pickupPin && !dropPin && <span className="muted">Tap the map to place your pickup.</span>}
        {(pickupPin || dropPin) && !(pickupPin && dropPin) && (
          <span className="muted">Now tap where you want to go.</span>
        )}
        {estimate.loading && <span className="muted">Working out the fare...</span>}
        {estimate.error && <span className="error">{estimate.error}</span>}
        {estimate.estimate && (
          <span><b>{formatDistance(estimate.estimate.distanceKm)}</b> · estimated fare <b>{formatFare(estimate.estimate.fare)}</b></span>
        )}
        {(pickupPin || dropPin) && (
          <button type="button" className="link-button" onClick={() => { onClear(); setTarget(PIN_TARGETS.PICKUP); }}>
            Clear pins
          </button>
        )}
      </div>
    </div>
  );
}
