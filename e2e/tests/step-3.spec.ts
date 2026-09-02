import { expect, test } from './support/fixtures';
import { customerTable, expectCustomerTable, openApplication } from './support/app';

test('navigates to the customer form, creates a customer, and returns to the table', async ({ page, browserErrors }) => {
  await openApplication(page);
  await expectCustomerTable(page);

  await page.getByRole('button', { name: 'Add Customer' }).click();
  await expect(page).toHaveURL(/\/customer$/);
  await expect(page.getByRole('button', { name: 'Submit' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Cancel' })).toBeVisible();

  await page.getByLabel('First Name').fill('Ada');
  await page.getByLabel('Last Name').fill('Lovelace');
  await page.getByLabel('Company').fill('Analytical Engines');
  await expect(page).toHaveScreenshot('completed-customer-form.png');

  await page.getByRole('button', { name: 'Submit' }).click();
  await expect(page).toHaveURL(/\/$/);
  await expect(customerTable(page)).toContainText('Ada');
  await expect(customerTable(page)).toContainText('Analytical Engines');

  await page.getByRole('button', { name: 'Add Customer' }).click();
  await page.getByRole('button', { name: 'Cancel' }).click();
  await expect(page).toHaveURL(/\/$/);
  expect(browserErrors).toEqual([]);
});
