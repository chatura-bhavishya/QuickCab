import { useEffect } from 'react';
import { CircleMarker, MapContainer, Polyline, TileLayer, Tooltip, useMap, useMapEvents } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import { MAP } from '../../constants';

function ClickToPin({ onPick }) {
  useMapEvents({ click: (e) => onPick([e.latlng.lat, e.latlng.lng]) });
  return null;
}

// Zooms to show both pins whenever they change (used on read-only maps).
function FitToPins({ pickup, drop }) {
  const map = useMap();
  useEffect(() => {
    if (pickup && drop) map.fitBounds([pickup, drop], { padding: [40, 40] });
  }, [map, pickup, drop]);
  return null;
}

function Pin({ position, color, label }) {
  return (
    <CircleMarker center={position} radius={9} pathOptions={{ color, fillColor: color, fillOpacity: 0.85 }}>
      <Tooltip permanent direction="top" offset={[0, -8]}>{label}</Tooltip>
    </CircleMarker>
  );
}

/**
 * OpenStreetMap with optional pickup/drop pins and a dashed line between them.
 * Pass `onPick` to let the user drop pins by clicking; leave it out for a
 * read-only map that zooms to the route.
 */
export default function RouteMap({ pickup, drop, onPick, height = 280, label = 'Route map' }) {
  return (
    <div className="map-box" role="region" aria-label={label}>
      <MapContainer center={pickup || drop || MAP.DEFAULT_CENTER} zoom={MAP.DEFAULT_ZOOM}
        scrollWheelZoom={false} style={{ height }}>
        <TileLayer url={MAP.TILE_URL} attribution={MAP.ATTRIBUTION} />
        {onPick ? <ClickToPin onPick={onPick} /> : <FitToPins pickup={pickup} drop={drop} />}
        {pickup && <Pin position={pickup} color={MAP.PICKUP_COLOR} label="Pickup" />}
        {drop && <Pin position={drop} color={MAP.DROP_COLOR} label="Drop" />}
        {pickup && drop && (
          <Polyline positions={[pickup, drop]} pathOptions={{ color: MAP.ROUTE_COLOR, dashArray: '6 8' }} />
        )}
      </MapContainer>
    </div>
  );
}
