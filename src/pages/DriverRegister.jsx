import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/api';

export default function DriverRegister() {
  const [form, setForm] = useState({
    username: '', password: '', contact: '', vehicleType: 'car', vehicleNumber: ''
  });
  const [err, setErr] = useState('');
  const [msg, setMsg] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErr(''); setMsg('');
    try {
      await api.post('/drivers/register', form);
      setMsg('Registered! Redirecting to login...');
      setTimeout(() => navigate('/driver/login'), 1200);
    } catch (e) {
      setErr(e.response?.data || 'Registration failed');
    }
  };

  return (
    <div className="card" style={{ maxWidth: 460, margin: '40px auto' }}>
      <h2>Driver Register</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-group"><label>Username</label>
          <input name="username" value={form.username} onChange={handleChange} required /></div>
        <div className="form-group"><label>Password</label>
          <input name="password" type="password" value={form.password} onChange={handleChange} required /></div>
        <div className="form-group"><label>Contact</label>
          <input name="contact" value={form.contact} onChange={handleChange} required /></div>
        <div className="form-group"><label>Vehicle Type</label>
          <select name="vehicleType" value={form.vehicleType} onChange={handleChange}>
            <option value="car">Car</option>
            <option value="bike">Bike</option>
            <option value="auto">Auto</option>
          </select></div>
        <div className="form-group"><label>Vehicle Number</label>
          <input name="vehicleNumber" value={form.vehicleNumber} onChange={handleChange} required /></div>
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
