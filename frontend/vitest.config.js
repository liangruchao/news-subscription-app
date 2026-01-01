import { defineConfig } from 'vitest/config';
import path from 'path';

export default defineConfig({
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./test/setup.js'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      include: ['public/js/**/*.js'],
      exclude: ['test/**/*.js', '**/*.test.js'],
      all: true,
      lines: 60,
      functions: 60,
      branches: 60,
      statements: 60
    },
    alias: {
      '@': path.resolve(__dirname, './public/js')
    }
  }
});
