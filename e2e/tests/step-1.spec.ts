import { expect, test } from './support/fixtures';
import { openApplication } from './support/app';

test('renders the basic application and opens the information dialog', async ({ page, browserErrors }) => {
  await openApplication(page);

  await expect(page.getByText('Tutorial App!', { exact: true })).toBeVisible();

  const infoButton = page.getByRole('button', { name: 'Info', exact: true });
  await expect(infoButton).toBeVisible();
  await infoButton.click();

  const dialog = page.getByRole('dialog');
  await expect(dialog).toBeVisible();
  await expect(page.getByText('This is a tutorial!', { exact: true })).toBeVisible();
  await expect(page).toHaveScreenshot('basic-application-dialog.png');

  expect(browserErrors).toEqual([]);
});
