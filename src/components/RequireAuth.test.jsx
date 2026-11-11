import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { ROLES } from '../constants';
import { saveSession } from '../services/session';
import RequireAuth from './RequireAuth';

function renderAt(role) {
  return render(
    <MemoryRouter initialEntries={['/private']}>
      <Routes>
        <Route path="/private" element={<RequireAuth role={role}><p>Secret page</p></RequireAuth>} />
        <Route path="/user/login" element={<p>User login</p>} />
        <Route path="/driver/login" element={<p>Driver login</p>} />
      </Routes>
    </MemoryRouter>
  );
}

const loginAs = (role) => saveSession(role, { token: 't', expiresInSeconds: 60, account: { id: 1 } });

describe('RequireAuth', () => {
  it('redirects logged-out visitors to the matching login page', () => {
    renderAt('driver');
    expect(screen.getByText('Driver login')).toBeInTheDocument();
  });

  it('renders the page for the right role', () => {
    loginAs(ROLES.USER);
    renderAt('user');
    expect(screen.getByText('Secret page')).toBeInTheDocument();
  });

  it('blocks the wrong role', () => {
    loginAs(ROLES.DRIVER);
    renderAt('user');
    expect(screen.getByText('User login')).toBeInTheDocument();
  });

  it('"any" accepts either role', () => {
    loginAs(ROLES.DRIVER);
    renderAt('any');
    expect(screen.getByText('Secret page')).toBeInTheDocument();
  });
});
