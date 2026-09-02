import { expect, test } from './support/fixtures';
import { customerTable, expectCustomerTable, openApplication } from './support/app';

test('binds and validates customer data before submission', async ({ page, browserErrors }) => {
  await openApplication(page);
  await expectCustomerTable(page);

  await page.getByRole('button', { name: 'Add Customer' }).click();
  const submit = page.getByRole('button', { name: 'Submit' });
  await expect(submit).toBeEnabled();

  await page.getByLabel('First Name').fill('Grace');
  await submit.click();
  await expect(page).toHaveURL(/\/customer$/);
  await expect(page.getByText('Customer last name is required', { exact: true })).toBeVisible();
  await page.getByLabel('Last Name').fill('Hopper');
  await page.getByLabel('Company').fill('Compiler Systems');
  await expect(page.getByText('Customer last name is required', { exact: true })).toBeHidden();
  await expect(submit).toBeEnabled();
  await expect(page).toHaveScreenshot('valid-customer-form.png');

  await submit.click();
  await expect(page).toHaveURL(/\/$/);
  await expect(customerTable(page)).toContainText('Grace');
  await expect(customerTable(page)).toContainText('Compiler Systems');
  expect(browserErrors).toEqual([]);
});

test('loads an existing customer into the binding context', async ({ page }) => {
  await openApplication(page, '/customer/1');
  await expect(page.getByLabel('First Name')).toHaveValue('Alice');
  await expect(page.getByLabel('Last Name')).toHaveValue('Smith');
  await expect(page.getByRole('button', { name: 'Submit' })).toBeEnabled();
});
