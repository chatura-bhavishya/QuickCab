import { describe, expect, it } from 'vitest';
import { getErrorMessage } from './errors';

const apiError = (data) => ({ response: { data } });

describe('getErrorMessage', () => {
  it('shows the API error message', () => {
    expect(getErrorMessage(apiError({ status: 409, error: 'Username already exists' }), 'fallback'))
      .toBe('Username already exists');
    expect(getErrorMessage(apiError('Plain text message'), 'fallback')).toBe('Plain text message');
  });

  it('falls back for anything unsafe or unknown', () => {
    expect(getErrorMessage(new Error('Network Error'), 'fallback')).toBe('fallback');
    expect(getErrorMessage(apiError('<html>Gateway</html>'), 'fallback')).toBe('fallback');
    expect(getErrorMessage(apiError({ trace: 'java.lang...' }), 'fallback')).toBe('fallback');
    expect(getErrorMessage(apiError('x'.repeat(500)), 'fallback')).toBe('fallback');
  });
});
