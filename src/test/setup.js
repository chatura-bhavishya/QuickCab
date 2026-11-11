// Vitest setup: DOM matchers and a clean DOM/localStorage between tests.
import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach } from 'vitest';

// Newer Node versions ship their own (disabled-by-default) localStorage global,
// which stops jsdom from installing its own. Provide an in-memory one if needed.
if (typeof globalThis.localStorage?.clear !== 'function') {
  const store = new Map();
  const memoryStorage = {
    getItem: (key) => (store.has(key) ? store.get(key) : null),
    setItem: (key, value) => store.set(key, String(value)),
    removeItem: (key) => store.delete(key),
    clear: () => store.clear(),
    key: (index) => [...store.keys()][index] ?? null,
    get length() { return store.size; }
  };
  Object.defineProperty(globalThis, 'localStorage', { value: memoryStorage, configurable: true, writable: true });
}

afterEach(() => {
  cleanup();
  localStorage.clear();
});
