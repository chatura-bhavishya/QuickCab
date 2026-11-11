import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import FormField from '../components/forms/FormField';
import { LIMITS, REDIRECT_DELAY_MS, ROLES } from '../constants';
import { useForm } from '../hooks/useForm';
import { register } from '../services/authService';
import { getErrorMessage } from '../utils/errors';
import {
  firstError, validateContact, validateEmail, validatePassword, validateUsername
} from '../utils/validation';

export default function UserRegister() {
  const { values: form, handleChange } = useForm({ username: '', email: '', password: '', contact: '' });
  const [msg, setMsg] = useState('');
  const [err, setErr] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErr(''); setMsg('');
    const validationError = firstError(
      validateUsername(form.username),
      validateEmail(form.email),
      validatePassword(form.password),
      validateContact(form.contact)
    );
    if (validationError) { setErr(validationError); return; }

    try {
      await register(ROLES.USER, form);
      setMsg('Registered! Redirecting to login...');
      setTimeout(() => navigate('/user/login'), REDIRECT_DELAY_MS);
    } catch (e) {
      setErr(getErrorMessage(e, 'Registration failed'));
    }
  };

  return (
    <div className="card" style={{ maxWidth: 460, margin: '40px auto' }}>
      <h2>User Register</h2>
      <form onSubmit={handleSubmit}>
        <FormField id="user-reg-username" label="Username" name="username" value={form.username}
          onChange={handleChange} maxLength={LIMITS.USERNAME_MAX} autoComplete="username" required />
        <FormField id="user-reg-email" label="Email" name="email" type="email" value={form.email}
          onChange={handleChange} maxLength={LIMITS.EMAIL_MAX} autoComplete="email" required />
        <FormField id="user-reg-password" label="Password" name="password" type="password" value={form.password}
          onChange={handleChange} maxLength={LIMITS.PASSWORD_MAX} autoComplete="new-password" required />
        <FormField id="user-reg-contact" label="Contact Number" name="contact" type="tel" value={form.contact}
          onChange={handleChange} maxLength={LIMITS.CONTACT_MAX} autoComplete="tel" required />
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
