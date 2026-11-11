import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import PinPicker from './PinPicker';

// Leaflet needs real layout, which jsdom doesn't have. A button stands in
// for "the user tapped the map at this spot".
vi.mock('./LazyRouteMap', () => ({
  default: ({ onPick }) => <button type="button" onClick={() => onPick([12.9, 77.6])}>tap map</button>
}));

const noEstimate = { estimate: null, loading: false, error: '' };

describe('PinPicker', () => {
  it('sets the pickup first, then moves on to the drop', async () => {
    const onPin = vi.fn();
    const { rerender } = render(
      <PinPicker pickupPin={null} dropPin={null} onPin={onPin} onClear={vi.fn()} estimate={noEstimate} />
    );

    await userEvent.click(screen.getByText('tap map'));
    expect(onPin).toHaveBeenLastCalledWith('pickup', [12.9, 77.6]);

    rerender(<PinPicker pickupPin={[12.9, 77.6]} dropPin={null} onPin={onPin} onClear={vi.fn()} estimate={noEstimate} />);
    await userEvent.click(screen.getByText('tap map'));
    expect(onPin).toHaveBeenLastCalledWith('drop', [12.9, 77.6]);
  });

  it('shows the estimated distance and fare', () => {
    render(
      <PinPicker pickupPin={[1, 2]} dropPin={[3, 4]} onPin={vi.fn()} onClear={vi.fn()}
        estimate={{ estimate: { distanceKm: 4.24, fare: 113.4 }, loading: false, error: '' }} />
    );
    expect(screen.getByText('4.2 km')).toBeInTheDocument();
    expect(screen.getByText('₹113')).toBeInTheDocument();
  });

  it('can clear the pins', async () => {
    const onClear = vi.fn();
    render(<PinPicker pickupPin={[1, 2]} dropPin={null} onPin={vi.fn()} onClear={onClear} estimate={noEstimate} />);

    await userEvent.click(screen.getByRole('button', { name: 'Clear pins' }));
    expect(onClear).toHaveBeenCalled();
  });
});
