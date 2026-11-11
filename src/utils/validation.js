// Client-side form validation. Each function returns an error message, or ''
// when the input is valid. The backend re-validates everything; these rules
// mirror backend/src/main/java/.../validation/InputValidator.java.
import { LIMITS, VEHICLES } from '../constants';

const USERNAME_RE = /^[A-Za-z0-9_.-]+$/;
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PHONE_RE = /^\+?[0-9][0-9 -]*[0-9]$/;
const PHONE_DIGITS_MIN = 7;
const PHONE_DIGITS_MAX = 15;
const VEHICLE_NUMBER_RE = /^[A-Za-z0-9 -]+$/;

const isBlank = (v) => !v || !String(v).trim();

export function validateUsername(v) {
  if (isBlank(v)) return 'Username is required';
  if (v.length < LIMITS.USERNAME_MIN || v.length > LIMITS.USERNAME_MAX)
    return `Username must be ${LIMITS.USERNAME_MIN}-${LIMITS.USERNAME_MAX} characters`;
  if (!USERNAME_RE.test(v)) return 'Username may only contain letters, numbers, dots, dashes and underscores';
  return '';
}

export function validatePassword(v, { optional = false } = {}) {
  if (isBlank(v)) return optional ? '' : 'Password is required';
  if (v.length < LIMITS.PASSWORD_MIN) return `Password must be at least ${LIMITS.PASSWORD_MIN} characters`;
  if (new TextEncoder().encode(v).length > LIMITS.PASSWORD_MAX) return 'Password is too long';
  return '';
}

export function validateEmail(v) {
  if (isBlank(v)) return 'Email is required';
  if (v.length > LIMITS.EMAIL_MAX || !EMAIL_RE.test(v)) return 'Enter a valid email address';
  return '';
}

export function validateContact(v) {
  if (isBlank(v)) return 'Contact number is required';
  const digits = v.replace(/\D/g, '').length;
  if (!PHONE_RE.test(v) || digits < PHONE_DIGITS_MIN || digits > PHONE_DIGITS_MAX)
    return 'Enter a valid contact number';
  return '';
}

export function validateVehicleType(v) {
  return VEHICLES.some(vehicle => vehicle.id === v) ? '' : 'Choose a valid vehicle type';
}

export function validateVehicleNumber(v) {
  if (isBlank(v)) return 'Vehicle number is required';
  if (v.length > LIMITS.VEHICLE_NUMBER_MAX || !VEHICLE_NUMBER_RE.test(v)) return 'Enter a valid vehicle number';
  return '';
}

export function validateLocation(v, label) {
  if (isBlank(v)) return `${label} is required`;
  if (v.length > LIMITS.LOCATION_MAX) return `${label} is too long`;
  return '';
}

export function validateChatMessage(v) {
  if (isBlank(v)) return 'Message cannot be empty';
  if (v.length > LIMITS.CHAT_MESSAGE_MAX) return 'Message is too long';
  return '';
}

/** Returns the first non-empty error message, or ''. */
export const firstError = (...errors) => errors.find(Boolean) || '';
