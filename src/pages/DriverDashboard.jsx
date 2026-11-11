import { useEffect, useState } from 'react';
import api from '../api/api';
import { getDriver } from '../auth/session';
import { LIMITS, POLL_INTERVAL_MS, RIDE_STATUS } from '../constants';
import { getErrorMessage } from '../utils/errors';
import { validateChatMessage } from '../utils/validation';

// Statuses during which the driver can still cancel the active ride.
const CANCELLABLE_STATUSES = [RIDE_STATUS.ACCEPTED, RIDE_STATUS.ON_THE_WAY, RIDE_STATUS.ARRIVED];
// Statuses we treat as an "active" ride (still in progress).
const ACTIVE_STATUSES = [RIDE_STATUS.ACCEPTED, RIDE_STATUS.ON_THE_WAY, RIDE_STATUS.ARRIVED, RIDE_STATUS.STARTED];

export default function DriverDashboard() {
  const [driver] = useState(getDriver);
  const [pending, setPending] = useState([]);
  const [activeRide, setActiveRide] = useState(null);
  const [otp, setOtp] = useState('');
  const [chat, setChat] = useState([]);
  const [msg, setMsg] = useState('');
  const [err, setErr] = useState('');
  const [notice, setNotice] = useState('');
  // Track the previously-active ride id so we can detect when it disappears
  // (e.g. the user cancelled it) and surface a notice to the driver.
  const [lastActiveId, setLastActiveId] = useState(null);

  const refresh = async () => {
    if (!driver) return;
    try {
      const p = await api.get(`/rides/pending/${driver.vehicleType}`);
      setPending(p.data);
      const mine = await api.get(`/rides/driver/${driver.id}`);
      const active = mine.data.find(r => ACTIVE_STATUSES.includes(r.status));

      // Detect a ride that was active last tick but is no longer — most likely
      // because the user cancelled it. Surface that to the driver.
      if (lastActiveId && (!active || active.id !== lastActiveId)) {
        const prev = mine.data.find(r => r.id === lastActiveId);
        if (prev && prev.status === RIDE_STATUS.CANCELLED_BY_USER) {
          setNotice('The user cancelled the ride.');
        }
      }
      setActiveRide(active || null);
      setLastActiveId(active ? active.id : null);

      if (active) {
        const c = await api.get(`/chat/${active.id}`);
        setChat(c.data);
      } else {
        setChat([]);
      }
    } catch {
      // Transient network/API error: keep the current view, retry on next poll.
    }
  };

  useEffect(() => {
    refresh();
    const t = setInterval(refresh, POLL_INTERVAL_MS);
    return () => clearInterval(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [lastActiveId]);

  /** Runs a ride action, showing a friendly notice if the API rejects it. */
  const runAction = async (action, failureMessage) => {
    try {
      await action();
    } catch (e) {
      setNotice(getErrorMessage(e, failureMessage));
    }
    refresh();
  };

  const accept = (rideId) => runAction(async () => {
    await api.post(`/rides/${rideId}/accept/${driver.id}`);
    setNotice('');
  }, 'This ride is no longer available.');
  const deny = (rideId) => runAction(
    () => api.post(`/rides/${rideId}/deny`),
    'Could not deny the ride. Please try again.');
  const setStatus = (status) => runAction(
    () => api.post(`/rides/${activeRide.id}/status`, { status }),
    'Could not update the ride status. Please try again.');
  const startRide = async () => {
    setErr('');
    try {
      await api.post(`/rides/${activeRide.id}/start`, { otp });
      setOtp(''); refresh();
    } catch {
      setErr('Invalid OTP');
    }
  };
  const cancelRide = async () => {
    setErr('');
    if (!activeRide) return;
    if (!window.confirm('Are you sure you want to cancel this ride?')) return;
    try {
      await api.post(`/rides/${activeRide.id}/cancel`, { by: 'DRIVER' });
      setNotice('You cancelled the ride.');
      refresh();
    } catch {
      setErr('Could not cancel — the ride may have already started.');
    }
  };
  const completePayment = () => runAction(
    () => api.post(`/rides/${activeRide.id}/pay`),
    'Could not complete the ride. Please try again.');
  const sendMsg = async () => {
    if (!msg.trim()) return;
    const validationError = validateChatMessage(msg);
    if (validationError) { setNotice(validationError); return; }
    await runAction(async () => {
      await api.post('/chat/send', { rideId: activeRide.id, sender: 'DRIVER', message: msg });
      setMsg('');
    }, 'Message could not be sent.');
  };

  if (!driver) return <div className="card">Please login as driver.</div>;

  return (
    <>
      <div className="card">
        <h2>Driver Dashboard</h2>
        <p>Welcome, <b>{driver.username}</b> ({driver.vehicleType} — {driver.vehicleNumber})</p>
        {notice && (
          <p style={{ color: '#b00', fontWeight: 600 }}>
            {notice}{' '}
            <button className="btn" style={{ marginLeft: 8 }} onClick={() => setNotice('')}>Dismiss</button>
          </p>
        )}
      </div>

      {!activeRide && (
        <div className="card">
          <h3>Pending Ride Requests</h3>
          {pending.length === 0 && <p className="muted">No requests right now. Stay tuned!</p>}
          {pending.map(r => (
            <div key={r.id} style={{ borderBottom: '1px solid #000', padding: '10px 0' }}>
              <p><b>Pickup:</b> {r.pickup}</p>
              <p><b>Drop:</b> {r.drop}</p>
              {r.amount != null && (
                <p><b>Payment Amount:</b> ₹{r.amount}</p>
              )}
              <button className="btn btn-success" onClick={() => accept(r.id)}>Accept</button>{' '}
              <button className="btn btn-danger" onClick={() => deny(r.id)}>Deny</button>
            </div>
          ))}
        </div>
      )}

      {activeRide && (
        <>
          <div className="card">
            <h3>Active Ride</h3>
            <p><b>From:</b> {activeRide.pickup} &nbsp; <b>To:</b> {activeRide.drop}</p>
            {activeRide.amount != null && (
              <p><b>Payment Amount:</b> ₹{activeRide.amount}</p>
            )}
            <p><b>Status:</b> <span className="badge">{activeRide.status}</span></p>

            {activeRide.status === RIDE_STATUS.ACCEPTED && (
              <button className="btn" onClick={() => setStatus(RIDE_STATUS.ON_THE_WAY)}>I'm On The Way</button>
            )}
            {activeRide.status === RIDE_STATUS.ON_THE_WAY && (
              <button className="btn" onClick={() => setStatus(RIDE_STATUS.ARRIVED)}>Arrived at Pickup</button>
            )}
            {activeRide.status === RIDE_STATUS.ARRIVED && (
              <div style={{ marginTop: 10 }}>
                <label>Enter OTP from user</label>
                <input value={otp} onChange={e => setOtp(e.target.value)} style={{ width: 120, margin: '0 8px' }}
                  inputMode="numeric" maxLength={LIMITS.OTP_LENGTH} />
                <button className="btn" onClick={startRide}>Start Ride</button>
                {err && <p style={{ color: '#b00' }}>{err}</p>}
              </div>
            )}
            {activeRide.status === RIDE_STATUS.STARTED && (
              <button className="btn btn-success" onClick={completePayment}>Payment Received → Complete Ride</button>
            )}

            {CANCELLABLE_STATUSES.includes(activeRide.status) && (
              <div style={{ marginTop: 12 }}>
                <button className="btn btn-danger" onClick={cancelRide}>Cancel Ride</button>
              </div>
            )}
          </div>

          <div className="card">
            <h3>Chat with User</h3>
            <div className="chat-box">
              {chat.map(c => (
                <div key={c.id} className={`chat-msg ${c.sender === 'DRIVER' ? 'me' : 'them'}`}>
                  {c.message}
                </div>
              ))}
              {chat.length === 0 && <p className="muted">No messages yet.</p>}
            </div>
            <div className="chat-input">
              <input value={msg} onChange={e => setMsg(e.target.value)} placeholder="Type a message..."
                maxLength={LIMITS.CHAT_MESSAGE_MAX} />
              <button className="btn" onClick={sendMsg}>Send</button>
            </div>
          </div>
        </>
      )}
    </>
  );
}
