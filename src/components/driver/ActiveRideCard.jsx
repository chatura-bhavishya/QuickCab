import { useState } from 'react';
import { DRIVER_CANCELLABLE_STATUSES, LIMITS, RIDE_STATUS } from '../../constants';

/**
 * The driver's current ride and the next action for its status.
 * onStart(otp) and onCancel() return promises that reject on failure.
 */
export default function ActiveRideCard({ ride, onSetStatus, onStart, onComplete, onCancel }) {
  const [otp, setOtp] = useState('');
  const [err, setErr] = useState('');

  const start = async () => {
    setErr('');
    try {
      await onStart(otp);
      setOtp('');
    } catch {
      setErr('Invalid OTP');
    }
  };

  const cancel = async () => {
    setErr('');
    if (!window.confirm('Are you sure you want to cancel this ride?')) return;
    try {
      await onCancel();
    } catch {
      setErr('Could not cancel — the ride may have already started.');
    }
  };

  return (
    <div className="card">
      <h3>Active Ride</h3>
      <p><b>From:</b> {ride.pickup} &nbsp; <b>To:</b> {ride.drop}</p>
      {ride.amount != null && (
        <p><b>Payment Amount:</b> ₹{ride.amount}</p>
      )}
      <p><b>Status:</b> <span className="badge">{ride.status}</span></p>

      {ride.status === RIDE_STATUS.ACCEPTED && (
        <button className="btn" onClick={() => onSetStatus(RIDE_STATUS.ON_THE_WAY)}>I'm On The Way</button>
      )}
      {ride.status === RIDE_STATUS.ON_THE_WAY && (
        <button className="btn" onClick={() => onSetStatus(RIDE_STATUS.ARRIVED)}>Arrived at Pickup</button>
      )}
      {ride.status === RIDE_STATUS.ARRIVED && (
        <div style={{ marginTop: 10 }}>
          <label htmlFor="ride-otp">Enter OTP from user</label>
          <input id="ride-otp" value={otp} onChange={e => setOtp(e.target.value)}
            style={{ width: 120, margin: '0 8px' }} inputMode="numeric" maxLength={LIMITS.OTP_LENGTH} />
          <button className="btn" onClick={start}>Start Ride</button>
        </div>
      )}
      {ride.status === RIDE_STATUS.STARTED && (
        <button className="btn btn-success" onClick={onComplete}>Payment Received → Complete Ride</button>
      )}

      {DRIVER_CANCELLABLE_STATUSES.includes(ride.status) && (
        <div style={{ marginTop: 12 }}>
          <button className="btn btn-danger" onClick={cancel}>Cancel Ride</button>
        </div>
      )}
      {err && <p role="alert" style={{ color: '#b00' }}>{err}</p>}
    </div>
  );
}
