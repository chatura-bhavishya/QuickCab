import { useEffect, useState } from 'react';
import { getMyRides } from '../services/rideService';

export default function History() {
  const [rides, setRides] = useState([]);
  const [err, setErr] = useState('');

  useEffect(() => {
    getMyRides()
      .then(setRides)
      .catch(() => setErr('Could not load your ride history. Please try again.'));
  }, []);

  return (
    <div className="card">
      <h2>Ride History</h2>
      {err && <div className="error">{err}</div>}
      {!err && rides.length === 0 && <p className="muted">No rides yet.</p>}
      {rides.length > 0 && (
        <table>
          <thead>
            <tr><th>ID</th><th>Pickup</th><th>Drop</th><th>Vehicle</th><th>Status</th></tr>
          </thead>
          <tbody>
            {rides.map(r => (
              <tr key={r.id}>
                <td>{r.id}</td>
                <td>{r.pickup}</td>
                <td>{r.drop}</td>
                <td>{r.vehicleType}</td>
                <td><span className="badge">{r.status}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
