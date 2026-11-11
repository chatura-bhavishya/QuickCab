import { lazy, Suspense } from 'react';

// Leaflet is fairly heavy, so it's only downloaded on screens that show a map.
const RouteMap = lazy(() => import('./RouteMap'));

export default function LazyRouteMap(props) {
  const height = props.height || 280;
  return (
    <Suspense fallback={<div className="map-box map-loading" style={{ height }}>Loading map...</div>}>
      <RouteMap {...props} />
    </Suspense>
  );
}
