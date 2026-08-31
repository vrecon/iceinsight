import { ActivitySummary } from '../api/models/activity-summary';
import { BestN, bestDuration } from './best-n';
import { durationToMillis } from './lap-time';

export const AMSTERDAM_TZ = 'Europe/Amsterdam';

export type DailyFastest = {
  date: string;
  duration: string;
  millis: number;
};

export function amsterdamIsoDate(value?: string | null): string | null {
  if (!value) {
    return null;
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return isoDatePrefix(value);
  }
  return date.toLocaleDateString('en-CA', { timeZone: AMSTERDAM_TZ });
}

export function isoDatePrefix(value?: string | null): string | null {
  if (!value) {
    return null;
  }
  const match = /^(\d{4}-\d{2}-\d{2})/.exec(value.trim());
  return match ? match[1] : null;
}

export function dailyFastestSeries(
  activities: ActivitySummary[],
  startDate: string | undefined,
  endDate: string | undefined,
  n: BestN,
): DailyFastest[] {
  const start = isoDatePrefix(startDate);
  const end = isoDatePrefix(endDate);
  if (!start || !end) {
    return [];
  }

  const bestByDay = new Map<string, DailyFastest>();
  for (const activity of activities) {
    const day = amsterdamIsoDate(activity.startTime);
    if (!day || day < start || day > end) {
      continue;
    }
    const duration = bestDuration(activity, n);
    const millis = durationToMillis(duration);
    if (!duration || millis == null) {
      continue;
    }
    const current = bestByDay.get(day);
    if (!current || millis < current.millis) {
      bestByDay.set(day, { date: day, duration, millis });
    }
  }

  return [...bestByDay.values()].sort((a, b) => a.date.localeCompare(b.date));
}
