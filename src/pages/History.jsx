import React, { useEffect, useState } from 'react';
import api from '../api/api';

export default function History() {
  const user = JSON.parse(localStorage.getItem('user') || 'null');
  const driver = JSON.parse(localStorage.getItem('driver') || 'null');
  const [rides, setRides] = useState([]);

  useEffect(() => {
    const url = user ? `/rides/user/${user.id}` : driver ? `/rides/driver/${driver.id}` : null;
    if (url) api.get(url).then(r => setRides(r.data));
  }, []);

  return (
    <div className="card">
      <h2>Ride History</h2>
      {rides.length === 0 && <p className="muted">No rides yet.</p>}
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
