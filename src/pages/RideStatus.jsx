import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../api/api';

// Statuses during which the OTP is still relevant for the user to share.
const OTP_VISIBLE_STATUSES = ['ACCEPTED', 'ON_THE_WAY', 'ARRIVED'];
// Statuses during which the user can still cancel the ride.
const CANCELLABLE_STATUSES = ['PENDING', 'ACCEPTED', 'ON_THE_WAY', 'ARRIVED'];

export default function RideStatus() {
  const { id } = useParams();
  const [ride, setRide] = useState(null);
  const [driver, setDriver] = useState(null);
  const [chat, setChat] = useState([]);
  const [msg, setMsg] = useState('');
  const [rating, setRating] = useState(0);
  const [rated, setRated] = useState(false);
  const [cancelErr, setCancelErr] = useState('');

  const fetchRide = async () => {
    const res = await api.get(`/rides/${id}`);
    setRide(res.data.ride);
    setDriver(res.data.driver || null);
  };
  const fetchChat = async () => {
    const res = await api.get(`/chat/${id}`);
    setChat(res.data);
  };

  useEffect(() => {
    fetchRide(); fetchChat();
    const t = setInterval(() => { fetchRide(); fetchChat(); }, 3000);
    return () => clearInterval(t);
  }, [id]);

  const sendMsg = async () => {
    if (!msg.trim()) return;
    await api.post('/chat/send', { rideId: Number(id), sender: 'USER', message: msg });
    setMsg(''); fetchChat();
  };

  const submitRating = async () => {
    if (!rating) return;
    await api.post(`/rides/${id}/rate`, { rating });
    setRated(true);
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

  if (!ride) return <div className="card">Loading...</div>;

  const cancelledByUser = ride.status === 'CANCELLED_BY_USER';
  const cancelledByDriver = ride.status === 'CANCELLED_BY_DRIVER';
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

        {ride.status === 'COMPLETED' && <p className="success">Ride completed. Thank you!</p>}

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

      {driver && ride.status !== 'COMPLETED' && !isCancelled && (
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
            <input value={msg} onChange={e => setMsg(e.target.value)} placeholder="Type a message..." />
            <button className="btn" onClick={sendMsg}>Send</button>
          </div>
        </div>
      )}

      {ride.status === 'COMPLETED' && driver && !rated && (
        <div className="card">
          <h3>Rate Your Driver</h3>
          <div>
            {[1,2,3,4,5].map(s => (
              <span key={s} className="stars"
                style={{ opacity: s <= rating ? 1 : 0.3 }}
                onClick={() => setRating(s)}>★</span>
            ))}
          </div>
          <button className="btn" style={{ marginTop: 10 }} onClick={submitRating}>Submit Rating</button>
        </div>
      )}
      {rated && <div className="card success">Thanks for rating!</div>}
    </>
  );
}
