import { useCallback, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../api/api';
import { LIMITS, POLL_INTERVAL_MS, RATING_STARS, RIDE_STATUS } from '../constants';
import { getErrorMessage } from '../utils/errors';
import { validateChatMessage } from '../utils/validation';

// Statuses during which the OTP is still relevant for the user to share.
const OTP_VISIBLE_STATUSES = [RIDE_STATUS.ACCEPTED, RIDE_STATUS.ON_THE_WAY, RIDE_STATUS.ARRIVED];
// Statuses during which the user can still cancel the ride.
const CANCELLABLE_STATUSES = [RIDE_STATUS.PENDING, RIDE_STATUS.ACCEPTED, RIDE_STATUS.ON_THE_WAY, RIDE_STATUS.ARRIVED];

export default function RideStatus() {
  const { id } = useParams();
  const [ride, setRide] = useState(null);
  const [driver, setDriver] = useState(null);
  const [chat, setChat] = useState([]);
  const [msg, setMsg] = useState('');
  const [rating, setRating] = useState(0);
  const [rated, setRated] = useState(false);
  const [cancelErr, setCancelErr] = useState('');
  const [loadErr, setLoadErr] = useState('');
  const [actionErr, setActionErr] = useState('');

  const fetchRide = useCallback(async () => {
    try {
      const res = await api.get(`/rides/${id}`);
      setRide(res.data.ride);
      setDriver(res.data.driver || null);
      setLoadErr('');
    } catch {
      setLoadErr('Could not load this ride.');
    }
  }, [id]);

  const fetchChat = useCallback(async () => {
    try {
      const res = await api.get(`/chat/${id}`);
      setChat(res.data);
    } catch {
      // Chat is refreshed on the next poll; keep the last messages shown.
    }
  }, [id]);

  useEffect(() => {
    fetchRide(); fetchChat();
    const t = setInterval(() => { fetchRide(); fetchChat(); }, POLL_INTERVAL_MS);
    return () => clearInterval(t);
  }, [fetchRide, fetchChat]);

  const sendMsg = async () => {
    setActionErr('');
    if (!msg.trim()) return;
    const validationError = validateChatMessage(msg);
    if (validationError) { setActionErr(validationError); return; }
    try {
      await api.post('/chat/send', { rideId: Number(id), sender: 'USER', message: msg });
      setMsg(''); fetchChat();
    } catch (e) {
      setActionErr(getErrorMessage(e, 'Message could not be sent.'));
    }
  };

  const submitRating = async () => {
    setActionErr('');
    if (!rating) return;
    try {
      await api.post(`/rides/${id}/rate`, { rating });
      setRated(true);
    } catch {
      setActionErr('Could not submit rating. Please try again.');
    }
  };

  const cancelRide = async () => {
    setCancelErr('');
    if (!window.confirm('Are you sure you want to cancel this ride?')) return;
    try {
      await api.post(`/rides/${id}/cancel`, { by: 'USER' });
      fetchRide();
    } catch {
      setCancelErr('Could not cancel — the ride may have already started.');
    }
  };

  if (!ride) return <div className="card">{loadErr || 'Loading...'}</div>;

  const cancelledByUser = ride.status === RIDE_STATUS.CANCELLED_BY_USER;
  const cancelledByDriver = ride.status === RIDE_STATUS.CANCELLED_BY_DRIVER;
  const isCancelled = cancelledByUser || cancelledByDriver;

  return (
    <>
      <div className="card">
        <h2>Ride Status</h2>
        <p><b>From:</b> {ride.pickup} &nbsp; <b>To:</b> {ride.drop}</p>
        <p><b>Vehicle:</b> {ride.vehicleType}</p>
        {ride.amount != null && (
          <p><b>Payment Amount:</b> ₹{ride.amount}</p>
        )}
        <p><b>Status:</b> <span className="badge">{ride.status}</span></p>

        {OTP_VISIBLE_STATUSES.includes(ride.status) && (
          <p style={{ marginTop: 10 }}>
            <b>Your OTP:</b>{' '}
            <span style={{ fontSize: 22, fontWeight: 700 }}>{ride.otp}</span>
            {' '}— Share with driver to start the ride.
          </p>
        )}

        {ride.status === RIDE_STATUS.COMPLETED && <p className="success">Ride completed. Thank you!</p>}

        {cancelledByUser && (
          <p className="success" style={{ color: '#b00' }}>
            You cancelled this ride.
          </p>
        )}
        {cancelledByDriver && (
          <p className="success" style={{ color: '#b00' }}>
            The driver cancelled this ride.
          </p>
        )}

        {CANCELLABLE_STATUSES.includes(ride.status) && (
          <div style={{ marginTop: 12 }}>
            <button className="btn btn-danger" onClick={cancelRide}>Cancel Ride</button>
            {cancelErr && <p style={{ color: '#b00', marginTop: 6 }}>{cancelErr}</p>}
          </div>
        )}
      </div>

      {driver && (
        <div className="card">
          <h3>Driver Details</h3>
          <p><b>Name:</b> {driver.username}</p>
          <p><b>Contact:</b> {driver.contact}</p>
          <p><b>Vehicle:</b> {driver.vehicleType} — {driver.vehicleNumber}</p>
          <p><b>Rating:</b> {driver.rating} ({driver.ratingCount} ratings)</p>
        </div>
      )}

      {driver && ride.status !== RIDE_STATUS.COMPLETED && !isCancelled && (
        <div className="card">
          <h3>Chat with Driver</h3>
          <div className="chat-box">
            {chat.map(c => (
              <div key={c.id} className={`chat-msg ${c.sender === 'USER' ? 'me' : 'them'}`}>
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
          {actionErr && <div className="error">{actionErr}</div>}
        </div>
      )}

      {ride.status === RIDE_STATUS.COMPLETED && driver && !rated && (
        <div className="card">
          <h3>Rate Your Driver</h3>
          <div>
            {RATING_STARS.map(s => (
              <span key={s} className="stars"
                style={{ opacity: s <= rating ? 1 : 0.3 }}
                onClick={() => setRating(s)}>★</span>
            ))}
          </div>
          <button className="btn" style={{ marginTop: 10 }} onClick={submitRating}>Submit Rating</button>
          {actionErr && <div className="error">{actionErr}</div>}
        </div>
      )}
      {rated && <div className="card success">Thanks for rating!</div>}
    </>
  );
}
