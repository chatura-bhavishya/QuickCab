import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api';

export default function BookRide() {
  const user = JSON.parse(localStorage.getItem('user') || 'null');
  const navigate = useNavigate();

  const [pickup, setPickup] = useState(''); 
  const [drop, setDrop] = useState('');
  const [vehicleType, setVehicleType] = useState('car');
  const [pastPickups, setPastPickups] = useState([]);
  const [pastDrops, setPastDrops] = useState([]);
  const [err, setErr] = useState('');

  useEffect(() => {
    if (!user) { navigate('/user/login'); return; }
    api.get(`/rides/locations/${user.id}`)
      .then(res => { setPastPickups(res.data.pickups || []); setPastDrops(res.data.drops || []); })
      .catch(() => {});
  }, []);

  const book = async (e) => {
    e.preventDefault();
    setErr('');
    try {
      const res = await api.post('/rides/book', {
        userId: user.id, pickup, drop, vehicleType
      });
      navigate(`/ride/${res.data.id}`);
    } catch {
      setErr('Could not book ride. Try again.');
    }
  };

  const vehicles = [
    { id: 'car', label: 'Car' },
    { id: 'bike', label: 'Bike' },
    { id: 'auto', label: 'Auto' }
  ];

  return (
    <div className="card" style={{ maxWidth: 600, margin: '20px auto' }}>
      <h2>Book Your Ride</h2>
      <form onSubmit={book}>
        <div className="form-group">
          <label>Pickup Location</label>
          <input list="pastPickups" value={pickup} onChange={e => setPickup(e.target.value)}
            placeholder="Enter pickup" required />
          <datalist id="pastPickups">
            {pastPickups.map((p, i) => <option key={i} value={p} />)}
          </datalist>
        </div>
        <div className="form-group">
          <label>Drop Location</label>
          <input list="pastDrops" value={drop} onChange={e => setDrop(e.target.value)}
            placeholder="Enter drop" required />
          <datalist id="pastDrops">
            {pastDrops.map((p, i) => <option key={i} value={p} />)}
          </datalist>
        </div>

        <label>Choose Vehicle</label>
        <div className="vehicles" style={{ marginTop: 8, marginBottom: 16 }}>
          {vehicles.map(v => (
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
