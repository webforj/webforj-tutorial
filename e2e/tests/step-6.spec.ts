import { expect, test } from './support/fixtures';
import { customerTable, expectCustomerTable, openApplication } from './support/app';

test('provides application-layout navigation around the customer workflow', async ({ page, browserErrors }) => {
  await openApplication(page);
  await expectCustomerTable(page);
  await expect(page.getByRole('heading', { name: 'Customer Table', level: 1 })).toBeVisible();
  await expect(page).toHaveScreenshot('dashboard-layout.png');

  await page.getByText('About', { exact: true }).click();
  await expect(page).toHaveURL(/\/about$/);
  await expect(page.getByRole('heading', { name: 'About', level: 1 })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Customer Manager', level: 2 }).last()).toBeVisible();

  await page.getByText('Dashboard', { exact: true }).click();
  await expect(page).toHaveURL(/\/$/);
  await expect(customerTable(page)).toBeVisible();

  await page.getByRole('button', { name: 'Add Customer' }).click();
  await expect(page).toHaveURL(/\/customer$/);
  await expect(page.getByRole('heading', { name: 'Customer Form', level: 1 })).toBeVisible();
  await page.getByRole('button', { name: 'Cancel' }).click();
  await expect(page).toHaveURL(/\/$/);
  expect(browserErrors).toEqual([]);
});

test('keeps the application layout usable at a narrow viewport', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await openApplication(page);

  await expect(page.getByRole('heading', { name: 'Customer Table', level: 1 })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Add Customer' })).toBeVisible();
  await expect(page).toHaveScreenshot('dashboard-mobile.png');

  const dashboardLink = page.getByRole('link', { name: /Dashboard/ });
  await expect(dashboardLink).not.toBeInViewport();
  await page.getByRole('button', { name: /menu/i }).click();
  await expect(dashboardLink).toBeInViewport();
});
