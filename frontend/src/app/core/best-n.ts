export const BEST_N = [1, 2, 4, 8, 13, 25, 50, 100] as const;
export type BestN = (typeof BEST_N)[number];

export type BestDurations = {
  best1Duration?: string;
  best2Duration?: string;
  best4Duration?: string;
  best8Duration?: string;
  best13Duration?: string;
  best25Duration?: string;
  best50Duration?: string;
  best100Duration?: string;
};

export function bestDuration(model: BestDurations | null | undefined, n: BestN): string | undefined {
  if (!model) {
    return undefined;
  }
  switch (n) {
    case 1:
      return model.best1Duration;
    case 2:
      return model.best2Duration;
    case 4:
      return model.best4Duration;
    case 8:
      return model.best8Duration;
    case 13:
      return model.best13Duration;
    case 25:
      return model.best25Duration;
    case 50:
      return model.best50Duration;
    case 100:
      return model.best100Duration;
  }
}

export function formatDateTime(value?: string): string {
  if (!value) {
    return '—';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString('nl-NL', { dateStyle: 'medium', timeStyle: 'short' });
}

export function formatDate(value?: string): string {
  if (!value) {
    return '—';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleDateString('nl-NL', { dateStyle: 'medium' });
}

export function displayDuration(value?: string | null): string {
  if (!value) {
    return '—';
  }
  return value;
}

export function locationLabel(locationId?: number): string {
  if (locationId === undefined || locationId === null) {
    return 'Onbekende baan';
  }
  return `Baan #${locationId}`;
}
