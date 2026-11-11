/** Waiting ride requests for the driver's vehicle type. */
export default function PendingRideList({ rides, onAccept, onDeny }) {
  return (
    <div className="card">
      <h3>Pending Ride Requests</h3>
      {rides.length === 0 && <p className="muted">No requests right now. Stay tuned!</p>}
      {rides.map(r => (
        <div key={r.id} style={{ borderBottom: '1px solid #000', padding: '10px 0' }}>
          <p><b>Pickup:</b> {r.pickup}</p>
          <p><b>Drop:</b> {r.drop}</p>
          {r.amount != null && (
            <p><b>Payment Amount:</b> ₹{r.amount}</p>
          )}
          <button className="btn btn-success" onClick={() => onAccept(r.id)}>Accept</button>{' '}
          <button className="btn btn-danger" onClick={() => onDeny(r.id)}>Deny</button>
        </div>
      ))}
    </div>
  );
}
