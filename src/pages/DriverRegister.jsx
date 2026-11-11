import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register } from '../services/authService';
import { DEFAULT_VEHICLE_TYPE, LIMITS, REDIRECT_DELAY_MS, ROLES, VEHICLES } from '../constants';
import { getErrorMessage } from '../utils/errors';
import {
  firstError, validateContact, validatePassword, validateUsername,
  validateVehicleNumber, validateVehicleType
} from '../utils/validation';

export default function DriverRegister() {
  const [form, setForm] = useState({
    username: '', password: '', contact: '', vehicleType: DEFAULT_VEHICLE_TYPE, vehicleNumber: ''
  });
  const [err, setErr] = useState('');
  const [msg, setMsg] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErr(''); setMsg('');
    const validationError = firstError(
      validateUsername(form.username),
      validatePassword(form.password),
      validateContact(form.contact),
      validateVehicleType(form.vehicleType),
      validateVehicleNumber(form.vehicleNumber)
    );
    if (validationError) { setErr(validationError); return; }

    try {
      await register(ROLES.DRIVER, form);
      setMsg('Registered! Redirecting to login...');
      setTimeout(() => navigate('/driver/login'), REDIRECT_DELAY_MS);
    } catch (e) {
      setErr(getErrorMessage(e, 'Registration failed'));
    }
  };

  return (
    <div className="card" style={{ maxWidth: 460, margin: '40px auto' }}>
      <h2>Driver Register</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-group"><label>Username</label>
          <input name="username" value={form.username} onChange={handleChange}
            maxLength={LIMITS.USERNAME_MAX} autoComplete="username" required /></div>
        <div className="form-group"><label>Password</label>
          <input name="password" type="password" value={form.password} onChange={handleChange}
            maxLength={LIMITS.PASSWORD_MAX} autoComplete="new-password" required /></div>
        <div className="form-group"><label>Contact</label>
          <input name="contact" type="tel" value={form.contact} onChange={handleChange}
            maxLength={LIMITS.CONTACT_MAX} autoComplete="tel" required /></div>
        <div className="form-group"><label>Vehicle Type</label>
          <select name="vehicleType" value={form.vehicleType} onChange={handleChange}>
            {VEHICLES.map(v => <option key={v.id} value={v.id}>{v.label}</option>)}
          </select></div>
        <div className="form-group"><label>Vehicle Number</label>
          <input name="vehicleNumber" value={form.vehicleNumber} onChange={handleChange}
            maxLength={LIMITS.VEHICLE_NUMBER_MAX} required /></div>
        <button className="btn btn-block">Register</button>
        {err && <div className="error">{err}</div>}
        {msg && <div className="success">{msg}</div>}
      </form>
      <p className="muted" style={{ marginTop: 12 }}>
        Already a driver? <Link to="/driver/login">Login</Link>
      </p>
    </div>
  );
}
