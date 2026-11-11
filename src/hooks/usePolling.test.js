import { renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { usePolling } from './usePolling';

describe('usePolling', () => {
  beforeEach(() => vi.useFakeTimers());
  afterEach(() => vi.useRealTimers());

  it('runs immediately, then on every interval, and stops on unmount', () => {
    const callback = vi.fn();
    const { unmount } = renderHook(() => usePolling(callback, 1000));

    expect(callback).toHaveBeenCalledTimes(1);
    vi.advanceTimersByTime(3000);
    expect(callback).toHaveBeenCalledTimes(4);

    unmount();
    vi.advanceTimersByTime(3000);
    expect(callback).toHaveBeenCalledTimes(4);
  });

  it('does nothing while disabled', () => {
    const callback = vi.fn();
    renderHook(() => usePolling(callback, 1000, false));

    vi.advanceTimersByTime(5000);
    expect(callback).not.toHaveBeenCalled();
  });
});
