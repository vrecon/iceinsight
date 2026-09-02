import { expect, test, type Page } from '@playwright/test';

const ACTIVITY = {
  id: 42,
  name: 'PRACTICE',
  startTime: '2026-01-15T10:00:00',
  endTime: '2026-01-15T11:00:00',
  locationId: 1,
  chipId: 7,
  best1Duration: '32.1',
};

const LAP = {
  lapNr: 1,
  sessionNr: 1,
  duration: '32.100',
  rest: false,
  speedKph: 45.2,
  datetimeStart: '2026-01-15T10:00:00',
};

async function mockApis(page: Page): Promise<void> {
  await page.route('**/api/v1/auth/login', async (route) => {
    if (route.request().method() !== 'POST') {
      await route.fallback();
      return;
    }
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        token: 'e2e',
        user: { id: 1, username: 'e2e', email: 'e2e@example.test' },
      }),
    });
  });

  await page.route('**/api/v1/activities/42/laps', async (route) => {
    if (route.request().method() !== 'GET') {
      await route.fallback();
      return;
    }
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([LAP]),
    });
  });

  await page.route('**/api/v1/activities/42', async (route) => {
    if (route.request().method() !== 'GET') {
      await route.fallback();
      return;
    }
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(ACTIVITY),
    });
  });

  await page.route('**/api/v1/activities', async (route) => {
    if (route.request().method() !== 'GET') {
      await route.fallback();
      return;
    }
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([ACTIVITY]),
    });
  });
}

test('Ritten list opens native PRACTICE href to detail Ronden', async ({ page }) => {
  await mockApis(page);
  await page.addInitScript(() => {
    localStorage.setItem('iceinsights.accessToken', 'e2e');
    localStorage.setItem(
      'iceinsights.user',
      JSON.stringify({ id: 1, username: 'e2e', email: 'e2e@example.test', firstName: 'E2E' }),
    );
  });

  await page.goto('/tabs/ritten');
  await page.getByRole('link', { name: 'PRACTICE' }).click();
  await expect(page).toHaveURL(/\/ritten\/42$/);
  await expect(page.getByRole('heading', { name: 'Ronden' }).or(page.getByText('Ronden'))).toBeVisible();
});
