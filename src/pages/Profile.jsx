import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { deleteMyAccount, updateMyAccount } from '../services/accountService';
import { getDriver, getUser } from '../services/session';
import { DEFAULT_VEHICLE_TYPE, LIMITS, ROLES, VEHICLES } from '../constants';
import { getErrorMessage } from '../utils/errors';
import {
  firstError, validateContact, validateEmail, validatePassword,
  validateVehicleNumber, validateVehicleType
} from '../utils/validation';

export default function Profile() {
  const navigate = useNavigate();
  const user = getUser();
  const driver = getDriver();
  const isUser = !!user;
  const me = user || driver;

  // The password field always starts empty ("leave blank to keep").
  const [form, setForm] = useState(() => ({ ...(me || {}), password: '' }));
  const [msg, setMsg] = useState('');
  const [err, setErr] = useState('');

  if (!me) return <div className="card">Please login first.</div>;

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });
  const role = isUser ? ROLES.USER : ROLES.DRIVER;

  const update = async () => {
    setMsg(''); setErr('');
    const validationError = isUser
      ? firstError(
        validateEmail(form.email),
        validateContact(form.contact),
        validatePassword(form.password, { optional: true }))
      : firstError(
        validateContact(form.contact),
        validateVehicleType(form.vehicleType || DEFAULT_VEHICLE_TYPE),
        validateVehicleNumber(form.vehicleNumber),
        validatePassword(form.password, { optional: true }));
    if (validationError) { setErr(validationError); return; }

    try {
      const payload = isUser ? form : { ...form, vehicleType: form.vehicleType || DEFAULT_VEHICLE_TYPE };
      const updated = await updateMyAccount(role, payload);
      setForm({ ...updated, password: '' });
      setMsg('Profile updated!');
    } catch (e) {
      setErr(getErrorMessage(e, 'Could not update profile. Please try again.'));
    }
  };

  const remove = async () => {
    if (!window.confirm('Delete your account permanently?')) return;
    setMsg(''); setErr('');
    try {
      await deleteMyAccount(role);
      navigate('/');
    } catch {
      setErr('Could not delete account. Please try again.');
    }
  };

  return (
    <div className="card" style={{ maxWidth: 500, margin: '20px auto' }}>
      <h2>My Profile</h2>
      <div className="form-group"><label>Username (cannot change)</label>
        <input value={form.username || ''} disabled /></div>
      {isUser && (
        <div className="form-group"><label>Email</label>
          <input name="email" type="email" value={form.email || ''} onChange={handleChange}
            maxLength={LIMITS.EMAIL_MAX} autoComplete="email" /></div>
      )}
      <div className="form-group"><label>Contact</label>
        <input name="contact" type="tel" value={form.contact || ''} onChange={handleChange}
          maxLength={LIMITS.CONTACT_MAX} autoComplete="tel" /></div>
      {!isUser && (
        <>
          <div className="form-group"><label>Vehicle Type</label>
            <select name="vehicleType" value={form.vehicleType || DEFAULT_VEHICLE_TYPE} onChange={handleChange}>
              {VEHICLES.map(v => <option key={v.id} value={v.id}>{v.label}</option>)}
            </select></div>
          <div className="form-group"><label>Vehicle Number</label>
            <input name="vehicleNumber" value={form.vehicleNumber || ''} onChange={handleChange}
              maxLength={LIMITS.VEHICLE_NUMBER_MAX} /></div>
        </>
      )}
      <div className="form-group"><label>New Password (leave blank to keep)</label>
        <input name="password" type="password" value={form.password || ''} onChange={handleChange}
          maxLength={LIMITS.PASSWORD_MAX} autoComplete="new-password" /></div>

      <div style={{ display: 'flex', gap: 10 }}>
        <button className="btn" onClick={update}>Update</button>
        <button className="btn btn-danger" onClick={remove}>Delete Account</button>
      </div>
      {err && <div className="error">{err}</div>}
      {msg && <div className="success">{msg}</div>}
    </div>
  );
}
