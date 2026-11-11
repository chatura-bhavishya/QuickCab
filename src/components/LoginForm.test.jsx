import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { ROLES } from '../constants';
import { login } from '../services/authService';
import LoginForm from './LoginForm';

vi.mock('../services/authService', () => ({ login: vi.fn() }));

function renderLogin() {
  return render(
    <MemoryRouter initialEntries={['/login']}>
      <Routes>
        <Route path="/login" element={
          <LoginForm role={ROLES.USER} title="User Login" redirectTo="/book"
            registerPrompt="New here?" registerPath="/register" />
        } />
        <Route path="/book" element={<p>Booking page</p>} />
      </Routes>
    </MemoryRouter>
  );
}

describe('LoginForm', () => {
  it('logs in with the entered credentials and redirects', async () => {
    login.mockResolvedValue({ id: 1 });
    renderLogin();

    await userEvent.type(screen.getByLabelText('Username'), 'rider1');
    await userEvent.type(screen.getByLabelText('Password'), 'secret12');
    await userEvent.click(screen.getByRole('button', { name: 'Login' }));

    expect(login).toHaveBeenCalledWith(ROLES.USER, { username: 'rider1', password: 'secret12' });
    expect(await screen.findByText('Booking page')).toBeInTheDocument();
  });

  it('shows a friendly error when login fails', async () => {
    login.mockRejectedValue(new Error('401'));
    renderLogin();

    await userEvent.type(screen.getByLabelText('Username'), 'rider1');
    await userEvent.type(screen.getByLabelText('Password'), 'wrong');
    await userEvent.click(screen.getByRole('button', { name: 'Login' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Invalid username or password');
  });
});
