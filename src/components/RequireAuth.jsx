import { Navigate } from 'react-router-dom';
import { getDriver, getUser } from '../auth/session';

/**
 * Route guard. `role` is 'user', 'driver' or 'any' (either kind of account).
 * Visitors without the required session are sent to the matching login page.
 *
 * Note: this only protects the UI. The backend has no auth tokens yet, so the
 * API itself does not verify who is calling.
 */
export default function RequireAuth({ role, children }) {
  const user = getUser();
  const driver = getDriver();

  const allowed =
    (role === 'user' && user) ||
    (role === 'driver' && driver) ||
    (role === 'any' && (user || driver));

  if (!allowed) {
    return <Navigate to={role === 'driver' ? '/driver/login' : '/user/login'} replace />;
  }
  return children;
}
