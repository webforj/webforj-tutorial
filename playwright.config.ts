import { defineConfig, devices } from '@playwright/test';

const step = process.env.E2E_STEP ?? '1';
const baseURL = process.env.E2E_BASE_URL ?? 'http://127.0.0.1:8080';

export default defineConfig({
  testDir: './e2e/tests',
  testMatch: `step-${step}.spec.ts`,
  outputDir: `test-results/step-${step}/playwright`,
  fullyParallel: false,
  workers: 1,
  retries: process.env.CI ? 1 : 0,
  forbidOnly: Boolean(process.env.CI),
  timeout: 45_000,
  expect: {
    timeout: 10_000,
    toHaveScreenshot: {
      animations: 'disabled',
      caret: 'hide',
      maxDiffPixels: 200,
      scale: 'css'
    }
  },
  reporter: [
    ['list'],
    ['html', { outputFolder: `playwright-report/step-${step}`, open: 'never' }]
  ],
  use: {
    baseURL,
    colorScheme: 'light',
    locale: 'en-US',
    timezoneId: 'UTC',
    viewport: { width: 1440, height: 900 },
    deviceScaleFactor: 1,
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
    video: 'retain-on-failure'
  },
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        viewport: { width: 1440, height: 900 }
      }
    }
  ]
});
