import { vi } from 'vitest';
import { cleanup } from '@vitest/coverage/v8';

// 每个测试后清理
afterEach(() => {
  cleanup();
});

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
};
global.localStorage = localStorageMock;

// Mock window.location
global.window = Object.create(window);
Object.defineProperty(window, 'location', {
  value: {
    href: 'http://localhost:8080/',
    pathname: '/',
    search: '',
    hash: '',
    assign: vi.fn(),
    replace: vi.fn(),
    reload: vi.fn(),
  },
  writable: true,
});

// Mock fetch
global.fetch = vi.fn();

// CSRF token mock
let mockCsrfToken = 'test-csrf-token-12345';

// Helper to reset mocks
global.resetMocks = () => {
  vi.clearAllMocks();
  mockCsrfToken = 'test-csrf-token-12345';
};
