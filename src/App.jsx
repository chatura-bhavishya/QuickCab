import { Routes, Route } from 'react-router-dom';

import ErrorBoundary from './components/ErrorBoundary.jsx';
import Navbar from './components/Navbar.jsx';
import RequireAuth from './components/RequireAuth.jsx';
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
import NotFound from './pages/NotFound.jsx';

export default function App() {
  return (
    <>
      <Navbar />
      <div className="container">
        <ErrorBoundary>
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
            <Route path="*" element={<NotFound />} />
          </Routes>
        </ErrorBoundary>
      </div>
    </>
  );
}
