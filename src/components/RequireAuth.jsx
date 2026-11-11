import { Navigate } from 'react-router-dom';
import { getDriver, getUser } from '../services/session';

/**
 * Route guard. `role` is 'user', 'driver' or 'any' (either kind of account).
 * Visitors without the required session are sent to the matching login page.
 *
 * This is a UX guard only; the API independently verifies the JWT and
 * ownership on every request.
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
