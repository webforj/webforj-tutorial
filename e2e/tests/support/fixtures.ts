import { expect, test as base } from '@playwright/test';

type Diagnostics = {
  browserErrors: string[];
};

export const test = base.extend<Diagnostics>({
  browserErrors: [async ({ page }, use) => {
    const browserErrors: string[] = [];

    page.on('pageerror', error => {
      browserErrors.push(`pageerror: ${error.message}`);
    });
    page.on('console', message => {
      if (message.type() === 'error') {
        browserErrors.push(`console: ${message.text()}`);
      }
    });
    page.on('requestfailed', request => {
      browserErrors.push(
        `requestfailed: ${request.method()} ${request.url()} (${request.failure()?.errorText ?? 'unknown error'})`);
    });

    await use(browserErrors);

    expect.soft(browserErrors, 'The browser should not report runtime or network errors').toEqual([]);
  }, { auto: true }]
});

export { expect } from '@playwright/test';
