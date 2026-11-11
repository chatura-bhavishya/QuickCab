import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import ErrorBoundary from './ErrorBoundary';

function Broken() {
  throw new Error('boom');
}

describe('ErrorBoundary', () => {
  it('shows a friendly message instead of crashing', () => {
    vi.spyOn(console, 'error').mockImplementation(() => {});
    render(<ErrorBoundary><Broken /></ErrorBoundary>);

    expect(screen.getByRole('alert')).toHaveTextContent('Something went wrong');
    expect(screen.queryByText('boom')).not.toBeInTheDocument();
  });

  it('renders children normally', () => {
    render(<ErrorBoundary><p>All good</p></ErrorBoundary>);
    expect(screen.getByText('All good')).toBeInTheDocument();
  });
});
