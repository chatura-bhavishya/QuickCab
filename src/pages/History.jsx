import { useEffect, useState } from 'react';
import api from '../api/api';
import { getDriver, getUser } from '../auth/session';

export default function History() {
  const [user] = useState(getUser);
  const [driver] = useState(getDriver);
  const [rides, setRides] = useState([]);
  const [err, setErr] = useState('');

  useEffect(() => {
    const url = user ? `/rides/user/${user.id}` : driver ? `/rides/driver/${driver.id}` : null;
    if (!url) return;
    api.get(url)
      .then(r => setRides(r.data))
      .catch(() => setErr('Could not load your ride history. Please try again.'));
  }, [user, driver]);

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
