import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import VehiclePicker from '../components/forms/VehiclePicker';
import { DEFAULT_VEHICLE_TYPE, LIMITS } from '../constants';
import { bookRide, getPastLocations } from '../services/rideService';
import { getErrorMessage } from '../utils/errors';
import { firstError, validateLocation } from '../utils/validation';

export default function BookRide() {
  const navigate = useNavigate();
  const [pickup, setPickup] = useState('');
  const [drop, setDrop] = useState('');
  const [vehicleType, setVehicleType] = useState(DEFAULT_VEHICLE_TYPE);
  const [pastPickups, setPastPickups] = useState([]);
  const [pastDrops, setPastDrops] = useState([]);
  const [err, setErr] = useState('');

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

    try {
      const ride = await bookRide({ pickup, drop, vehicleType });
      navigate(`/ride/${ride.id}`);
    } catch (e) {
      setErr(getErrorMessage(e, 'Could not book ride. Try again.'));
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

        <label>Choose Vehicle</label>
        <VehiclePicker value={vehicleType} onChange={setVehicleType} />

        <button className="btn btn-block">Book Ride</button>
        {err && <div className="error">{err}</div>}
      </form>
    </div>
  );
}
