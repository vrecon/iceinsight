import { expect, test, type Page } from '@playwright/test';

async function seedAuth(page: Page): Promise<void> {
  await page.addInitScript(() => {
    localStorage.setItem('iceinsights.accessToken', 'e2e');
    localStorage.setItem(
      'iceinsights.user',
      JSON.stringify({ id: 1, username: 'e2e', email: 'e2e@example.test', firstName: 'E2E' }),
    );
  });
}

async function mockNavApis(page: Page): Promise<void> {
  await page.route('**/api/v1/seasons', async (route) => {
    if (route.request().method() !== 'GET') {
      await route.fallback();
      return;
    }
    await route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
  });

  await page.route('**/api/v1/users/chips', async (route) => {
    if (route.request().method() !== 'GET') {
      await route.fallback();
      return;
    }
    await route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
  });

  await page.route('**/api/v1/activities', async (route) => {
    if (route.request().method() !== 'GET') {
      await route.fallback();
      return;
    }
    await route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
  });
}

async function openMainMenu(page: Page): Promise<void> {
  await page.getByRole('button', { name: /menu/i }).click();
  await expect(page.locator('ion-menu[menu-id="main-menu"]')).toBeVisible();
}

async function clickMenuDestination(page: Page, name: string): Promise<void> {
  const menu = page.locator('ion-menu[menu-id="main-menu"]');
  await menu
    .getByRole('link', { name })
    .or(menu.getByRole('menuitem', { name }))
    .or(menu.getByRole('button', { name }))
    .click();
}

test('hamburger Seizoenen opens /seizoenen with heading', async ({ page }) => {
  await mockNavApis(page);
  await seedAuth(page);

  await page.goto('/tabs/ritten');
  await openMainMenu(page);
  await clickMenuDestination(page, 'Seizoenen');
  await expect(page).toHaveURL(/\/seizoenen$/);
  await expect(
    page.getByRole('heading', { name: 'Seizoenen' }).or(page.locator('ion-title').filter({ hasText: 'Seizoenen' })),
  ).toBeVisible();
});

test('hamburger Chips opens /chips with heading', async ({ page }) => {
  await mockNavApis(page);
  await seedAuth(page);

  await page.goto('/tabs/ritten');
  await openMainMenu(page);
  await clickMenuDestination(page, 'Chips');
  await expect(page).toHaveURL(/\/chips$/);
  await expect(
    page.getByRole('heading', { name: 'Chips' }).or(page.locator('ion-title').filter({ hasText: 'Chips' })),
  ).toBeVisible();
});

test('hamburger Account shows display name and Uitloggen', async ({ page }) => {
  await mockNavApis(page);
  await seedAuth(page);

  await page.goto('/tabs/ritten');
  await openMainMenu(page);
  await clickMenuDestination(page, 'Account');
  await expect(page).toHaveURL(/\/account$/);
  await expect(
    page.getByRole('heading', { name: 'Account' }).or(page.locator('ion-title').filter({ hasText: 'Account' })),
  ).toBeVisible();
  await expect(page.getByRole('button', { name: 'Uitloggen' }).or(page.getByText('Uitloggen'))).toBeVisible();
  await expect(page.getByText('E2E', { exact: true }).or(page.getByText('e2e', { exact: true }))).toBeVisible();
});
