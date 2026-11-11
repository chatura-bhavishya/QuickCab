import { useState } from 'react';
import ChatPanel from '../components/ChatPanel';
import ActiveRideCard from '../components/driver/ActiveRideCard';
import PendingRideList from '../components/driver/PendingRideList';
import { DRIVER_ACTIVE_STATUSES, POLL_INTERVAL_MS, RIDE_STATUS, ROLES } from '../constants';
import { usePolling } from '../hooks/usePolling';
import { getMessages, sendMessage } from '../services/chatService';
import {
  acceptRide, cancelRide, completeRide, denyRide, getMyRides,
  getPendingRides, startRide, updateRideStatus
} from '../services/rideService';
import { getDriver } from '../services/session';
import { getErrorMessage } from '../utils/errors';

export default function DriverDashboard() {
  const [driver] = useState(getDriver);
  const [pending, setPending] = useState([]);
  const [activeRide, setActiveRide] = useState(null);
  const [chat, setChat] = useState([]);
  const [notice, setNotice] = useState('');
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  // The previously-active ride id, so we can notice when it disappears
  // (e.g. the rider cancelled it) and tell the driver.
  const [lastActiveId, setLastActiveId] = useState(null);

  const refresh = async () => {
    if (!driver) return;
    try {
      setPending(await getPendingRides());
      const mine = await getMyRides();
      const active = mine.find(r => DRIVER_ACTIVE_STATUSES.includes(r.status));

      if (lastActiveId && (!active || active.id !== lastActiveId)) {
        const prev = mine.find(r => r.id === lastActiveId);
        if (prev && prev.status === RIDE_STATUS.CANCELLED_BY_USER) {
          setNotice('The user cancelled the ride.');
        }
      }
      setActiveRide(active || null);
      setLastActiveId(active ? active.id : null);
      setChat(active ? await getMessages(active.id) : []);
      setLoadError('');
    } catch {
      // Keep the current view and retry on the next poll.
      setLoadError('Having trouble reaching the server. Retrying...');
    } finally {
      setLoading(false);
    }
  };

  usePolling(refresh, POLL_INTERVAL_MS, !!driver);

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
    await acceptRide(rideId);
    setNotice('');
  }, 'This ride is no longer available.');
  const deny = (rideId) => runAction(() => denyRide(rideId), 'Could not deny the ride. Please try again.');
  const setStatus = (status) => runAction(() => updateRideStatus(activeRide.id, status),
    'Could not update the ride status. Please try again.');
  const complete = () => runAction(() => completeRide(activeRide.id),
    'Could not complete the ride. Please try again.');

  // These two let ActiveRideCard show its own inline error, so they rethrow.
  const start = async (otp) => {
    await startRide(activeRide.id, otp);
    refresh();
  };
  const cancel = async () => {
    await cancelRide(activeRide.id);
    setNotice('You cancelled the ride.');
    refresh();
  };

  const handleSend = async (text) => {
    await sendMessage(activeRide.id, text);
    refresh();
  };

  if (!driver) return <div className="card">Please login as driver.</div>;

  return (
    <>
      <div className="card">
        <h2>Driver Dashboard</h2>
        <p>Welcome, <b>{driver.username}</b> ({driver.vehicleType} — {driver.vehicleNumber})</p>
        {notice && (
          <p role="alert" style={{ color: '#b00', fontWeight: 600 }}>
            {notice}{' '}
            <button className="btn" style={{ marginLeft: 8 }} onClick={() => setNotice('')}>Dismiss</button>
          </p>
        )}
        {loadError && <p className="error" role="status">{loadError}</p>}
      </div>

      {!activeRide && <PendingRideList rides={pending} loading={loading} onAccept={accept} onDeny={deny} />}

      {activeRide && (
        <>
          <ActiveRideCard ride={activeRide} onSetStatus={setStatus} onStart={start}
            onComplete={complete} onCancel={cancel} />
          <ChatPanel title="Chat with User" messages={chat} ownSender={ROLES.DRIVER} onSend={handleSend} />
        </>
      )}
    </>
  );
}
