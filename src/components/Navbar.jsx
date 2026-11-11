import { Link, useNavigate } from 'react-router-dom';
import { logout } from '../services/authService';
import { getDriver, getUser } from '../services/session';

export default function Navbar() {
  const navigate = useNavigate();
  const user = getUser();
  const driver = getDriver();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="navbar">
      <Link to="/" className="logo">Quick<span>Cab</span></Link>
      <div className="links">
        {!user && !driver && (
          <>
            <Link to="/user/login">User Login</Link>
            <Link to="/driver/login">Driver Login</Link>
          </>
        )}
        {user && (
          <>
            <Link to="/book">Book</Link>
            <Link to="/history">History</Link>
            <Link to="/profile">Profile</Link>
            <button onClick={handleLogout}>Logout</button>
          </>
        )}
        {driver && (
          <>
            <Link to="/driver/dashboard">Dashboard</Link>
            <Link to="/history">History</Link>
            <Link to="/profile">Profile</Link>
            <button onClick={handleLogout}>Logout</button>
          </>
        )}
      </div>
    </nav>
  );
}
