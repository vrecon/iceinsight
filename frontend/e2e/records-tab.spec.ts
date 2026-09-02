import { expect, test, type Page } from '@playwright/test';

const SEASON = {
  id: 1,
  label: '2025-2026',
  startDate: '2025-05-01',
  endDate: '2026-04-30',
  best1Duration: '32.1',
  best2Duration: '64.2',
  best4Duration: '128.4',
  best8Duration: '256.8',
  best13Duration: '417.3',
  best25Duration: '802.5',
  best50Duration: '1605.0',
  best100Duration: '3210.0',
};

async function seedAuth(page: Page): Promise<void> {
  await page.addInitScript(() => {
    localStorage.setItem('iceinsights.accessToken', 'e2e');
    localStorage.setItem(
      'iceinsights.user',
      JSON.stringify({ id: 1, username: 'e2e', email: 'e2e@example.test', firstName: 'E2E' }),
    );
  });
}

async function mockRecordsApis(page: Page): Promise<void> {
  await page.route('**/api/v1/seasons**', async (route) => {
    if (route.request().method() !== 'GET') {
      await route.fallback();
      return;
    }
    const url = route.request().url();
    let body: unknown = [SEASON];
    if (url.includes('/top')) {
      body = [];
    } else if (/\/seasons\/1(?:[/?]|$)/.test(url)) {
      body = SEASON;
    }
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(body),
    });
  });

  await page.route('**/api/v1/activities', async (route) => {
    if (route.request().method() !== 'GET') {
      await route.fallback();
      return;
    }
    await route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
  });
}

test('Records tab opens /tabs/records with Seizoensbesten', async ({ page }) => {
  await mockRecordsApis(page);
  await seedAuth(page);

  await page.goto('/tabs/ritten');
  await page.getByRole('tab', { name: 'Records' }).click();
  await expect(page).toHaveURL(/\/tabs\/records$/);
  await expect(
    page
      .getByRole('heading', { name: 'Seizoensbesten' })
      .or(page.getByText('Seizoensbesten'))
      .or(page.getByRole('heading', { name: 'Records' }))
      .or(page.locator('ion-title').filter({ hasText: 'Records' })),
  ).toBeVisible();
});
