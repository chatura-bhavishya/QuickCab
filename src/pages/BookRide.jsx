import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import FormMessage from '../components/FormMessage';
import VehiclePicker from '../components/forms/VehiclePicker';
import PinPicker from '../components/map/PinPicker';
import { DEFAULT_VEHICLE_TYPE, LIMITS, PIN_TARGETS } from '../constants';
import { useFareEstimate } from '../hooks/useFareEstimate';
import { bookRide, getPastLocations } from '../services/rideService';
import { getErrorMessage } from '../utils/errors';
import { pinLabel } from '../utils/format';
import { firstError, validateLocation } from '../utils/validation';

export default function BookRide() {
  const navigate = useNavigate();
  const [pickup, setPickup] = useState('');
  const [drop, setDrop] = useState('');
  const [vehicleType, setVehicleType] = useState(DEFAULT_VEHICLE_TYPE);
  const [pastPickups, setPastPickups] = useState([]);
  const [pastDrops, setPastDrops] = useState([]);
  const [err, setErr] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [pickupPin, setPickupPin] = useState(null);
  const [dropPin, setDropPin] = useState(null);
  const estimate = useFareEstimate(vehicleType, pickupPin, dropPin);

  const handlePin = (target, position) => {
    // Fill an empty address box so the rider doesn't have to type one.
    if (target === PIN_TARGETS.PICKUP) {
      setPickupPin(position);
      setPickup(current => current || pinLabel(position));
    } else {
      setDropPin(position);
      setDrop(current => current || pinLabel(position));
    }
  };

  const clearPins = () => { setPickupPin(null); setDropPin(null); };

  useEffect(() => {
    // Past locations are only autocomplete suggestions, so failures are ignored.
    getPastLocations()
      .then(data => { setPastPickups(data.pickups || []); setPastDrops(data.drops || []); })
      .catch(() => {});
  }, []);

  const book = async (e) => {
    e.preventDefault();
    setErr('');
    const validationError = firstError(
      validateLocation(pickup, 'Pickup location'),
      validateLocation(drop, 'Drop location')
    );
    if (validationError) { setErr(validationError); return; }

    setSubmitting(true);
    try {
      const ride = await bookRide({ pickup, drop, vehicleType, pickupPin, dropPin });
      navigate(`/ride/${ride.id}`);
    } catch (e) {
      setErr(getErrorMessage(e, 'Could not book ride. Try again.'));
      setSubmitting(false);
    }
  };

  return (
    <div className="card" style={{ maxWidth: 600, margin: '20px auto' }}>
      <h2>Book Your Ride</h2>
      <form onSubmit={book}>
        <div className="form-group">
          <label htmlFor="pickup">Pickup Location</label>
          <input id="pickup" list="pastPickups" value={pickup} onChange={e => setPickup(e.target.value)}
            placeholder="Enter pickup" maxLength={LIMITS.LOCATION_MAX} required />
          <datalist id="pastPickups">
            {pastPickups.map(p => <option key={p} value={p} />)}
          </datalist>
        </div>
        <div className="form-group">
          <label htmlFor="drop">Drop Location</label>
          <input id="drop" list="pastDrops" value={drop} onChange={e => setDrop(e.target.value)}
            placeholder="Enter drop" maxLength={LIMITS.LOCATION_MAX} required />
          <datalist id="pastDrops">
            {pastDrops.map(p => <option key={p} value={p} />)}
          </datalist>
        </div>

        <PinPicker pickupPin={pickupPin} dropPin={dropPin} onPin={handlePin} onClear={clearPins}
          estimate={estimate} />

        <label id="vehicle-label">Choose Vehicle</label>
        <VehiclePicker value={vehicleType} onChange={setVehicleType} labelledBy="vehicle-label" />

        <button className="btn btn-block" disabled={submitting}>{submitting ? 'Booking...' : 'Book Ride'}</button>
        <FormMessage error={err} />
      </form>
    </div>
  );
}
