import { expect, type Page } from '@playwright/test';

export async function openApplication(page: Page, path = '/') {
  await page.goto(path, { waitUntil: 'domcontentloaded' });
  await expect(page.locator('body')).not.toBeEmpty();
}

export function customerTable(page: Page) {
  return page.locator('dwc-table');
}

export async function expectCustomerTable(page: Page) {
  const table = customerTable(page);
  await expect(table).toBeVisible();
  await expect(table).toContainText('First Name');
  await expect(table).toContainText('Last Name');
  await expect(table).toContainText('Company');
  await expect(table).toContainText('Country');
  await expect(table).toContainText('Alice');
  await expect(table).toContainText('TechCorp');
  return table;
}
