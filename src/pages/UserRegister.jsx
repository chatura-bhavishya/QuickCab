import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/api';

export default function UserRegister() {
  const [form, setForm] = useState({ username: '', email: '', password: '', contact: '' });
  const [msg, setMsg] = useState('');
  const [err, setErr] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErr(''); setMsg('');
    try {
      await api.post('/users/register', form);
      setMsg('Registered! Redirecting to login...');
      setTimeout(() => navigate('/user/login'), 1200);
    } catch (e) {
      setErr(e.response?.data || 'Registration failed');
    }
  };

  return (
    <div className="card" style={{ maxWidth: 460, margin: '40px auto' }}>
      <h2>User Register</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-group"><label>Username</label>
          <input name="username" value={form.username} onChange={handleChange} required /></div>
        <div className="form-group"><label>Email</label>
          <input name="email" type="email" value={form.email} onChange={handleChange} required /></div>
        <div className="form-group"><label>Password</label>
          <input name="password" type="password" value={form.password} onChange={handleChange} required /></div>
        <div className="form-group"><label>Contact Number</label>
          <input name="contact" value={form.contact} onChange={handleChange} required /></div>
        <button className="btn btn-block">Register</button>
        {err && <div className="error">{err}</div>}
        {msg && <div className="success">{msg}</div>}
      </form>
      <p className="muted" style={{ marginTop: 12 }}>
        Already have an account? <Link to="/user/login">Login</Link>
      </p>
    </div>
  );
}
