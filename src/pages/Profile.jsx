import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import FormField from '../components/forms/FormField';
import VehicleSelect from '../components/forms/VehicleSelect';
import { DEFAULT_VEHICLE_TYPE, LIMITS, ROLES } from '../constants';
import { useForm } from '../hooks/useForm';
import { deleteMyAccount, updateMyAccount } from '../services/accountService';
import { getSession } from '../services/session';
import { getErrorMessage } from '../utils/errors';
import {
  firstError, validateContact, validateEmail, validatePassword,
  validateVehicleNumber, validateVehicleType
} from '../utils/validation';

/** Fields each kind of account may edit (the API accepts exactly these). */
function editableFields(isUser, form) {
  const common = { contact: form.contact, password: form.password };
  return isUser
    ? { ...common, email: form.email }
    : { ...common, vehicleType: form.vehicleType || DEFAULT_VEHICLE_TYPE, vehicleNumber: form.vehicleNumber };
}

function validateProfile(isUser, form) {
  const password = validatePassword(form.password, { optional: true });
  return isUser
    ? firstError(validateEmail(form.email), validateContact(form.contact), password)
    : firstError(
      validateContact(form.contact),
      validateVehicleType(form.vehicleType || DEFAULT_VEHICLE_TYPE),
      validateVehicleNumber(form.vehicleNumber),
      password);
}

export default function Profile() {
  const navigate = useNavigate();
  const [session] = useState(getSession);
  const role = session?.role;
  const isUser = role === ROLES.USER;

  // The password field always starts empty ("leave blank to keep").
  const { values: form, setValues: setForm, handleChange } = useForm({ ...(session?.account || {}), password: '' });
  const [msg, setMsg] = useState('');
  const [err, setErr] = useState('');

  if (!session) return <div className="card">Please login first.</div>;

  const update = async () => {
    setMsg(''); setErr('');
    const validationError = validateProfile(isUser, form);
    if (validationError) { setErr(validationError); return; }
    try {
      const updated = await updateMyAccount(role, editableFields(isUser, form));
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
      <FormField id="profile-username" label="Username (cannot change)" value={form.username || ''} disabled />
      {isUser && (
        <FormField id="profile-email" label="Email" name="email" type="email" value={form.email || ''}
          onChange={handleChange} maxLength={LIMITS.EMAIL_MAX} autoComplete="email" />
      )}
      <FormField id="profile-contact" label="Contact" name="contact" type="tel" value={form.contact || ''}
        onChange={handleChange} maxLength={LIMITS.CONTACT_MAX} autoComplete="tel" />
      {!isUser && (
        <>
          <FormField id="profile-vehicle-type" label="Vehicle Type">
            <VehicleSelect id="profile-vehicle-type" name="vehicleType"
              value={form.vehicleType || DEFAULT_VEHICLE_TYPE} onChange={handleChange} />
          </FormField>
          <FormField id="profile-vehicle-number" label="Vehicle Number" name="vehicleNumber"
            value={form.vehicleNumber || ''} onChange={handleChange} maxLength={LIMITS.VEHICLE_NUMBER_MAX} />
        </>
      )}
      <FormField id="profile-password" label="New Password (leave blank to keep)" name="password"
        type="password" value={form.password || ''} onChange={handleChange} maxLength={LIMITS.PASSWORD_MAX}
        autoComplete="new-password" />

      <div style={{ display: 'flex', gap: 10 }}>
        <button className="btn" onClick={update}>Update</button>
        <button className="btn btn-danger" onClick={remove}>Delete Account</button>
      </div>
      {err && <div className="error">{err}</div>}
      {msg && <div className="success">{msg}</div>}
    </div>
  );
}
