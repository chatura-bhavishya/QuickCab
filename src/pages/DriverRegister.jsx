import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import FormMessage from '../components/FormMessage';
import FormField from '../components/forms/FormField';
import VehicleSelect from '../components/forms/VehicleSelect';
import { DEFAULT_VEHICLE_TYPE, LIMITS, REDIRECT_DELAY_MS, ROLES } from '../constants';
import { useForm } from '../hooks/useForm';
import { register } from '../services/authService';
import { getErrorMessage } from '../utils/errors';
import {
  firstError, validateContact, validatePassword, validateUsername,
  validateVehicleNumber, validateVehicleType
} from '../utils/validation';

export default function DriverRegister() {
  const { values: form, handleChange } = useForm({
    username: '', password: '', contact: '', vehicleType: DEFAULT_VEHICLE_TYPE, vehicleNumber: ''
  });
  const [err, setErr] = useState('');
  const [msg, setMsg] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();

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

    setSubmitting(true);
    try {
      await register(ROLES.DRIVER, form);
      setMsg('Registered! Redirecting to login...');
      setTimeout(() => navigate('/driver/login'), REDIRECT_DELAY_MS);
    } catch (e) {
      setErr(getErrorMessage(e, 'Registration failed'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="card" style={{ maxWidth: 460, margin: '40px auto' }}>
      <h2>Driver Register</h2>
      <form onSubmit={handleSubmit}>
        <FormField id="driver-reg-username" label="Username" name="username" value={form.username}
          onChange={handleChange} maxLength={LIMITS.USERNAME_MAX} autoComplete="username" required />
        <FormField id="driver-reg-password" label="Password" name="password" type="password"
          value={form.password} onChange={handleChange} maxLength={LIMITS.PASSWORD_MAX}
          autoComplete="new-password" required />
        <FormField id="driver-reg-contact" label="Contact" name="contact" type="tel" value={form.contact}
          onChange={handleChange} maxLength={LIMITS.CONTACT_MAX} autoComplete="tel" required />
        <FormField id="driver-reg-vehicle-type" label="Vehicle Type">
          <VehicleSelect id="driver-reg-vehicle-type" name="vehicleType" value={form.vehicleType}
            onChange={handleChange} />
        </FormField>
        <FormField id="driver-reg-vehicle-number" label="Vehicle Number" name="vehicleNumber"
          value={form.vehicleNumber} onChange={handleChange} maxLength={LIMITS.VEHICLE_NUMBER_MAX} required />
        <button className="btn btn-block" disabled={submitting}>{submitting ? 'Registering...' : 'Register'}</button>
        <FormMessage error={err} success={msg} />
      </form>
      <p className="muted" style={{ marginTop: 12 }}>
        Already a driver? <Link to="/driver/login">Login</Link>
      </p>
    </div>
  );
}
