import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import VehiclePicker from './VehiclePicker';

describe('VehiclePicker', () => {
  it('shows the selected vehicle and selects on click', async () => {
    const onChange = vi.fn();
    render(<VehiclePicker value="car" onChange={onChange} />);

    expect(screen.getByRole('radio', { name: 'Car' })).toHaveAttribute('aria-checked', 'true');
    await userEvent.click(screen.getByRole('radio', { name: 'Bike' }));
    expect(onChange).toHaveBeenCalledWith('bike');
  });

  it('can be used with the keyboard', async () => {
    const onChange = vi.fn();
    render(<VehiclePicker value="car" onChange={onChange} />);

    screen.getByRole('radio', { name: 'Auto' }).focus();
    await userEvent.keyboard('{Enter}');
    expect(onChange).toHaveBeenCalledWith('auto');
  });
});
