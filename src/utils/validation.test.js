import { describe, expect, it } from 'vitest';
import {
  firstError, validateChatMessage, validateContact, validateEmail, validateLocation,
  validatePassword, validateUsername, validateVehicleNumber, validateVehicleType
} from './validation';

describe('validation', () => {
  it('accepts valid input', () => {
    expect(validateUsername('rider_01')).toBe('');
    expect(validateEmail('rider@example.com')).toBe('');
    expect(validateContact('+91 98765-43210')).toBe('');
    expect(validatePassword('secret12')).toBe('');
    expect(validateVehicleType('auto')).toBe('');
    expect(validateVehicleNumber('KA 01 AB 1234')).toBe('');
    expect(validateLocation('MG Road', 'Pickup')).toBe('');
  });

  it('rejects bad usernames', () => {
    expect(validateUsername('')).toMatch(/required/);
    expect(validateUsername('ab')).toMatch(/3-30/);
    expect(validateUsername('has space')).toMatch(/only contain/);
  });

  it('rejects bad emails and phone numbers', () => {
    expect(validateEmail('nope')).toBe('Enter a valid email address');
    expect(validateEmail('a@b')).toBe('Enter a valid email address');
    expect(validateContact('12345')).toBe('Enter a valid contact number');
    expect(validateContact('call-me')).toBe('Enter a valid contact number');
    expect(validateContact('1'.repeat(16))).toBe('Enter a valid contact number');
  });

  it('treats a blank password as "keep current" only when optional', () => {
    expect(validatePassword('')).toMatch(/required/);
    expect(validatePassword('', { optional: true })).toBe('');
    expect(validatePassword('123', { optional: true })).toMatch(/at least 6/);
    expect(validatePassword('x'.repeat(73))).toMatch(/too long/);
  });

  it('limits locations and chat messages', () => {
    expect(validateLocation('   ', 'Pickup location')).toBe('Pickup location is required');
    expect(validateLocation('x'.repeat(201), 'Drop location')).toBe('Drop location is too long');
    expect(validateChatMessage('x'.repeat(256))).toBe('Message is too long');
    expect(validateVehicleType('truck')).toMatch(/valid vehicle/);
  });

  it('firstError returns the first message', () => {
    expect(firstError('', 'second', 'third')).toBe('second');
    expect(firstError('', '')).toBe('');
  });
});
