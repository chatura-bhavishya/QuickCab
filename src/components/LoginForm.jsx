import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { LIMITS } from '../constants';
import { useForm } from '../hooks/useForm';
import { login } from '../services/authService';
import FormField from './forms/FormField';
import FormMessage from './FormMessage';

/** Shared login card for riders and drivers. */
export default function LoginForm({ role, title, redirectTo, registerPrompt, registerPath }) {
  const { values, handleChange } = useForm({ username: '', password: '' });
  const [err, setErr] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();
  const idPrefix = `${role.toLowerCase()}-login`;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErr('');
    setSubmitting(true);
    try {
      await login(role, values);
      navigate(redirectTo);
    } catch {
      setErr('Invalid username or password');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="card" style={{ maxWidth: 420, margin: '40px auto' }}>
      <h2>{title}</h2>
      <form onSubmit={handleSubmit}>
        <FormField id={`${idPrefix}-username`} label="Username" name="username" value={values.username}
          onChange={handleChange} maxLength={LIMITS.USERNAME_MAX} autoComplete="username" required />
        <FormField id={`${idPrefix}-password`} label="Password" name="password" type="password"
          value={values.password} onChange={handleChange} maxLength={LIMITS.PASSWORD_MAX}
          autoComplete="current-password" required />
        <button className="btn btn-block" disabled={submitting}>{submitting ? 'Logging in...' : 'Login'}</button>
        <FormMessage error={err} />
      </form>
      <p className="muted" style={{ marginTop: 12 }}>
        {registerPrompt} <Link to={registerPath}>Register</Link>
      </p>
    </div>
  );
}
