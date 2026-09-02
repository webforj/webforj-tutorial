import { expect, test } from './support/fixtures';
import { expectCustomerTable, openApplication } from './support/app';

test('renders seeded customer data and keeps the information dialog functional', async ({ page, browserErrors }) => {
  await openApplication(page);

  await expect(page.getByText('Tutorial App!', { exact: true })).toBeVisible();
  const table = await expectCustomerTable(page);
  await expect(table).toContainText('John');
  await expect(table).toContainText('Innovatech');

  await page.getByRole('button', { name: 'Info', exact: true }).click();
  await expect(page.getByText('This is a tutorial!', { exact: true })).toBeVisible();
  await page.getByRole('button', { name: 'OK' }).click();

  await expect(page.getByRole('dialog')).toBeHidden();
  await expect(page).toHaveScreenshot('customer-table.png');

  const firstNameHeader = page.getByRole('columnheader', { name: /^First Name/ });
  const rowTop = async (name: string) =>
    (await page.getByRole('cell', { name, exact: true }).boundingBox())?.y ?? Number.MAX_SAFE_INTEGER;

  await firstNameHeader.click();
  await expect.poll(async () => await rowTop('Emma') < await rowTop('John')).toBe(true);
  await firstNameHeader.click();
  await expect.poll(async () => await rowTop('John') < await rowTop('Emma')).toBe(true);

  expect(browserErrors).toEqual([]);
});
