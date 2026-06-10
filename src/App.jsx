import React from 'react';
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

function Navbar() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || 'null');
  const driver = JSON.parse(localStorage.getItem('driver') || 'null');

  const logout = () => {
    localStorage.clear();
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
          <Route path="/book" element={<BookRide />} />
          <Route path="/ride/:id" element={<RideStatus />} />
          <Route path="/driver/dashboard" element={<DriverDashboard />} />
          <Route path="/history" element={<History />} />
          <Route path="/profile" element={<Profile />} />
        </Routes>
      </div>
    </>
  );
}
