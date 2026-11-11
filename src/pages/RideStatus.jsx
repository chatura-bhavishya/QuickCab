import { useCallback, useState } from 'react';
import { useParams } from 'react-router-dom';
import ChatPanel from '../components/ChatPanel';
import RideRouteCard from '../components/map/RideRouteCard';
import DriverDetailsCard from '../components/ride/DriverDetailsCard';
import RatingCard from '../components/ride/RatingCard';
import {
  LIVE_FALLBACK_POLL_MS, OTP_VISIBLE_STATUSES, POLL_INTERVAL_MS, RIDE_STATUS, RIDER_CANCELLABLE_STATUSES, ROLES
} from '../constants';
import { useLiveUpdates } from '../hooks/useLiveUpdates';
import { usePolling } from '../hooks/usePolling';
import { getMessages, sendMessage } from '../services/chatService';
import { cancelRide as cancelRideRequest, getRideDetails, rateDriver } from '../services/rideService';

export default function RideStatus() {
  const { id } = useParams();
  const [ride, setRide] = useState(null);
  const [driver, setDriver] = useState(null);
  const [chat, setChat] = useState([]);
  const [rated, setRated] = useState(false);
  const [cancelErr, setCancelErr] = useState('');
  const [loadErr, setLoadErr] = useState('');

  const fetchRide = useCallback(async () => {
    try {
      const details = await getRideDetails(id);
      setRide(details.ride);
      setDriver(details.driver || null);
      setLoadErr('');
    } catch {
      setLoadErr('Could not load this ride.');
    }
  }, [id]);

  const fetchChat = useCallback(async () => {
    try {
      setChat(await getMessages(id));
    } catch {
      // Chat is refreshed on the next poll; keep the last messages shown.
    }
  }, [id]);

  // Live updates when the socket is up; polling stays on as a slower safety net.
  const live = useLiveUpdates([`/topic/rides/${id}`], (event) => {
    if (event.type !== 'CHAT') fetchRide();
    fetchChat();
  });
  usePolling(() => { fetchRide(); fetchChat(); }, live ? LIVE_FALLBACK_POLL_MS : POLL_INTERVAL_MS);

  const handleSend = async (text) => {
    await sendMessage(id, text);
    fetchChat();
  };

  const handleRate = async (stars) => {
    await rateDriver(id, stars);
    setRated(true);
  };

  const cancelRide = async () => {
    setCancelErr('');
    if (!window.confirm('Are you sure you want to cancel this ride?')) return;
    try {
      await cancelRideRequest(id);
      fetchRide();
    } catch {
      setCancelErr('Could not cancel — the ride may have already started.');
    }
  };

  if (!ride) {
    return loadErr
      ? <div className="card error" role="alert">{loadErr}</div>
      : <div className="card" role="status">Loading...</div>;
  }

  const cancelledByUser = ride.status === RIDE_STATUS.CANCELLED_BY_USER;
  const cancelledByDriver = ride.status === RIDE_STATUS.CANCELLED_BY_DRIVER;
  const isCancelled = cancelledByUser || cancelledByDriver;
  const isCompleted = ride.status === RIDE_STATUS.COMPLETED;
  const alreadyRated = rated || ride.driverRated;

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

        {isCompleted && <p className="success">Ride completed. Thank you!</p>}

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

        {RIDER_CANCELLABLE_STATUSES.includes(ride.status) && (
          <div style={{ marginTop: 12 }}>
            <button className="btn btn-danger" onClick={cancelRide}>Cancel Ride</button>
            {cancelErr && <p role="alert" style={{ color: '#b00', marginTop: 6 }}>{cancelErr}</p>}
          </div>
        )}
      </div>

      <RideRouteCard ride={ride} />

      {driver && <DriverDetailsCard driver={driver} />}

      {driver && !isCompleted && !isCancelled && (
        <ChatPanel title="Chat with Driver" messages={chat} ownSender={ROLES.USER} onSend={handleSend} />
      )}

      {isCompleted && driver && !alreadyRated && <RatingCard onSubmit={handleRate} />}
      {rated && <div className="card success">Thanks for rating!</div>}
    </>
  );
}
