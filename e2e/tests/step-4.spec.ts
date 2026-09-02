import { expect, test } from './support/fixtures';
import { customerTable, expectCustomerTable, openApplication } from './support/app';

test('shows the routed customer form consistently', async ({ page }) => {
  await openApplication(page, '/customer/1');
  await expect(page.getByLabel('First Name')).toHaveValue('Alice');
  await expect(page.getByLabel('Last Name')).toHaveValue('Smith');
  await expect(page.getByLabel('Company')).toHaveValue('TechCorp');
  await expect(page).toHaveScreenshot('edit-customer-route.png');
});

test('loads a customer through its route parameter and saves edits', async ({ page, browserErrors }) => {
  await openApplication(page);
  await expectCustomerTable(page);

  await page.getByText('Alice', { exact: true }).click();
  await expect(page).toHaveURL(/\/customer\/1$/);
  await expect(page.getByLabel('First Name')).toHaveValue('Alice');
  await expect(page.getByLabel('Last Name')).toHaveValue('Smith');

  const company = page.getByLabel('Company');
  await company.click();
  await company.press('ControlOrMeta+A');
  await company.pressSequentially('Updated TechCorp', { delay: 25 });
  await expect(company).toHaveValue('Updated TechCorp');
  await page.getByRole('button', { name: 'Submit' }).click();
  await expect(page).toHaveURL(/\/$/);
  await expect(page.getByRole('cell', { name: 'Updated TechCorp', exact: true })).toBeVisible();

  await openApplication(page, '/customer/999999');
  await expect(page).toHaveURL(/\/$/);
  await expect(customerTable(page)).toBeVisible();
  expect(browserErrors).toEqual([]);
});
