import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api';
import { getUser } from '../auth/session';
import { DEFAULT_VEHICLE_TYPE, LIMITS, VEHICLES } from '../constants';
import { getErrorMessage } from '../utils/errors';
import { firstError, validateLocation } from '../utils/validation';

export default function BookRide() {
  const [user] = useState(getUser);
  const navigate = useNavigate();

  const [pickup, setPickup] = useState('');
  const [drop, setDrop] = useState('');
  const [vehicleType, setVehicleType] = useState(DEFAULT_VEHICLE_TYPE);
  const [pastPickups, setPastPickups] = useState([]);
  const [pastDrops, setPastDrops] = useState([]);
  const [err, setErr] = useState('');

  useEffect(() => {
    if (!user) { navigate('/user/login'); return; }
    // Past locations are only suggestions, so failures are ignored.
    api.get(`/rides/locations/${user.id}`)
      .then(res => { setPastPickups(res.data.pickups || []); setPastDrops(res.data.drops || []); })
      .catch(() => {});
  }, [user, navigate]);

  const book = async (e) => {
    e.preventDefault();
    setErr('');
    const validationError = firstError(
      validateLocation(pickup, 'Pickup location'),
      validateLocation(drop, 'Drop location')
    );
    if (validationError) { setErr(validationError); return; }

    try {
      const res = await api.post('/rides/book', {
        userId: user.id, pickup, drop, vehicleType
      });
      navigate(`/ride/${res.data.id}`);
    } catch (e) {
      setErr(getErrorMessage(e, 'Could not book ride. Try again.'));
    }
  };

  return (
    <div className="card" style={{ maxWidth: 600, margin: '20px auto' }}>
      <h2>Book Your Ride</h2>
      <form onSubmit={book}>
        <div className="form-group">
          <label>Pickup Location</label>
          <input list="pastPickups" value={pickup} onChange={e => setPickup(e.target.value)}
            placeholder="Enter pickup" maxLength={LIMITS.LOCATION_MAX} required />
          <datalist id="pastPickups">
            {pastPickups.map((p, i) => <option key={i} value={p} />)}
          </datalist>
        </div>
        <div className="form-group">
          <label>Drop Location</label>
          <input list="pastDrops" value={drop} onChange={e => setDrop(e.target.value)}
            placeholder="Enter drop" maxLength={LIMITS.LOCATION_MAX} required />
          <datalist id="pastDrops">
            {pastDrops.map((p, i) => <option key={i} value={p} />)}
          </datalist>
        </div>

        <label>Choose Vehicle</label>
        <div className="vehicles" style={{ marginTop: 8, marginBottom: 16 }}>
          {VEHICLES.map(v => (
            <div key={v.id}
              className={`vehicle-card ${vehicleType === v.id ? 'selected' : ''}`}
              onClick={() => setVehicleType(v.id)}>
              <div>{v.label}</div>
            </div>
          ))}
        </div>

        <button className="btn btn-block">Book Ride</button>
        {err && <div className="error">{err}</div>}
      </form>
    </div>
  );
}
