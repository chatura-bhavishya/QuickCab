import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api';

export default function Profile() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || 'null');
  const driver = JSON.parse(localStorage.getItem('driver') || 'null');
  const isUser = !!user;
  const me = user || driver;

  const [form, setForm] = useState(me || {});
  const [msg, setMsg] = useState('');

  if (!me) return <div className="card">Please login first.</div>;

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const update = async () => {
    const url = isUser ? `/users/${me.id}` : `/drivers/${me.id}`;
    const res = await api.put(url, form);
    localStorage.setItem(isUser ? 'user' : 'driver', JSON.stringify(res.data));
    setMsg('Profile updated!');
  };

  const remove = async () => {
    if (!window.confirm('Delete your account permanently?')) return;
    const url = isUser ? `/users/${me.id}` : `/drivers/${me.id}`;
    await api.delete(url);
    localStorage.clear();
    navigate('/');
  };

  return (
    <div className="card" style={{ maxWidth: 500, margin: '20px auto' }}>
      <h2>My Profile</h2>
      <div className="form-group"><label>Username (cannot change)</label>
        <input value={form.username || ''} disabled /></div>
      {isUser && (
        <div className="form-group"><label>Email</label>
          <input name="email" value={form.email || ''} onChange={handleChange} /></div>
      )}
      <div className="form-group"><label>Contact</label>
        <input name="contact" value={form.contact || ''} onChange={handleChange} /></div>
      {!isUser && (
        <>
          <div className="form-group"><label>Vehicle Type</label>
            <select name="vehicleType" value={form.vehicleType || 'car'} onChange={handleChange}>
              <option value="car">Car</option><option value="bike">Bike</option><option value="auto">Auto</option>
            </select></div>
          <div className="form-group"><label>Vehicle Number</label>
            <input name="vehicleNumber" value={form.vehicleNumber || ''} onChange={handleChange} /></div>
        </>
      )}
      <div className="form-group"><label>New Password (leave blank to keep)</label>
        <input name="password" type="password" value={form.password || ''} onChange={handleChange} /></div>

      <div style={{ display: 'flex', gap: 10 }}>
        <button className="btn" onClick={update}>Update</button>
        <button className="btn btn-danger" onClick={remove}>Delete Account</button>
      </div>
      {msg && <div className="success">{msg}</div>}
    </div>
  );
}
