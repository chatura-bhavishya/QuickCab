import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/api';

export default function DriverLogin() {
  const [form, setForm] = useState({ username: '', password: '' });
  const [err, setErr] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErr('');
    try {
      const res = await api.post('/drivers/login', form);
      localStorage.setItem('driver', JSON.stringify(res.data));
      navigate('/driver/dashboard');
    } catch {
      setErr('Invalid username or password');
    }
  };

  return (
    <div className="card" style={{ maxWidth: 420, margin: '40px auto' }}>
      <h2>Driver Login</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-group"><label>Username</label>
          <input name="username" value={form.username} onChange={handleChange} required /></div>
        <div className="form-group"><label>Password</label>
          <input name="password" type="password" value={form.password} onChange={handleChange} required /></div>
        <button className="btn btn-block">Login</button>
        {err && <div className="error">{err}</div>}
      </form>
      <p className="muted" style={{ marginTop: 12 }}>
        New driver? <Link to="/driver/register">Register</Link>
      </p>
    </div>
  );
}
