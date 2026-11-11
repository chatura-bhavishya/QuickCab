import { Routes, Route, Link, useNavigate } from 'react-router-dom';

import Home from './pages/Home.jsx';
import UserLogin from './pages/UserLogin.jsx';
import UserRegister from './pages/UserRegister.jsx';
import DriverLogin from './pages/DriverLogin.jsx';
import DriverRegister from './pages/DriverRegister.jsx';
import BookRide from './pages/BookRide.jsx';
import RideStatus from './pages/RideStatus.jsx';
import DriverDashboard from './pages/DriverDashboard.jsx';
import History from './pages/History.jsx';
import Profile from './pages/Profile.jsx';
import RequireAuth from './components/RequireAuth.jsx';
import { getDriver, getUser } from './services/session';
import { logout as endSession } from './services/authService';

function Navbar() {
  const navigate = useNavigate();
  const user = getUser();
  const driver = getDriver();

  const logout = () => {
    endSession();
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
            <button onClick={logout}>Logout</button>
          </>
        )}
        {driver && (
          <>
            <Link to="/driver/dashboard">Dashboard</Link>
            <Link to="/history">History</Link>
            <Link to="/profile">Profile</Link>
            <button onClick={logout}>Logout</button>
          </>
        )}
      </div>
    </nav>
  );
}

export default function App() {
  return (
    <>
      <Navbar />
      <div className="container">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/user/login" element={<UserLogin />} />
          <Route path="/user/register" element={<UserRegister />} />
          <Route path="/driver/login" element={<DriverLogin />} />
          <Route path="/driver/register" element={<DriverRegister />} />
          <Route path="/book" element={<RequireAuth role="user"><BookRide /></RequireAuth>} />
          <Route path="/ride/:id" element={<RequireAuth role="user"><RideStatus /></RequireAuth>} />
          <Route path="/driver/dashboard" element={<RequireAuth role="driver"><DriverDashboard /></RequireAuth>} />
          <Route path="/history" element={<RequireAuth role="any"><History /></RequireAuth>} />
          <Route path="/profile" element={<RequireAuth role="any"><Profile /></RequireAuth>} />
        </Routes>
      </div>
    </>
  );
}
