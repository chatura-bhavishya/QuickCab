import { formatDistance, hasRoute, ridePins } from '../../utils/format';
import LazyRouteMap from './LazyRouteMap';

/** Read-only map of a ride's route; renders nothing for rides booked without pins. */
export default function RideRouteCard({ ride }) {
  if (!hasRoute(ride)) return null;
  const { pickup, drop } = ridePins(ride);

  return (
    <div className="card">
      <h3>Route</h3>
      {ride.distanceKm != null && (
        <p className="muted" style={{ marginBottom: 10 }}>About {formatDistance(ride.distanceKm)} in a straight line</p>
      )}
      <LazyRouteMap pickup={pickup} drop={drop} height={240} label="Ride route" />
    </div>
  );
}
