import { ActivityLap } from '../api/models/activity-lap';
import { durationToMillis, formatMillis } from './lap-time';

export const LAP_DISTANCE_KM = 0.4;

export type ActivityKpis = {
  fastestDuration: string | null;
  maxSpeedKph: number | null;
  avgSpeedKph: number | null;
  activeCount: number;
  restCount: number;
  distanceKm: number;
  activeMillis: number | null;
  start: string | null;
  finish: string | null;
};

export function computeActivityKpis(
  laps: ActivityLap[],
  fallbackStart?: string,
  fallbackEnd?: string,
): ActivityKpis {
  const active = laps.filter((lap) => !lap.rest);
  const restCount = laps.length - active.length;

  let fastestDuration: string | null = null;
  let fastestMs = Number.POSITIVE_INFINITY;
  let activeMillis = 0;
  let hasActiveDuration = false;
  const speeds: number[] = [];

  for (const lap of active) {
    const ms = durationToMillis(lap.duration);
    if (ms != null && ms > 0) {
      hasActiveDuration = true;
      activeMillis += ms;
      if (ms < fastestMs) {
        fastestMs = ms;
        fastestDuration = lap.duration ?? null;
      }
    }
    if (lap.speedKph != null && Number.isFinite(lap.speedKph)) {
      speeds.push(lap.speedKph);
    }
  }

  const starts = laps
    .map((lap) => lap.datetimeStart)
    .filter((value): value is string => !!value)
    .sort();

  return {
    fastestDuration,
    maxSpeedKph: speeds.length ? Math.max(...speeds) : null,
    avgSpeedKph: speeds.length ? speeds.reduce((sum, value) => sum + value, 0) / speeds.length : null,
    activeCount: active.length,
    restCount,
    distanceKm: active.length * LAP_DISTANCE_KM,
    activeMillis: hasActiveDuration ? activeMillis : null,
    start: starts[0] ?? fallbackStart ?? null,
    finish: starts.length ? starts[starts.length - 1] : (fallbackEnd ?? null),
  };
}

export function formatSpeedKph(speed: number | null | undefined): string {
  if (speed == null || Number.isNaN(speed)) {
    return '—';
  }
  return `${speed.toLocaleString('nl-NL', { maximumFractionDigits: 1 })} km/u`;
}

export function formatDistanceKm(km: number): string {
  return `${km.toLocaleString('nl-NL', { maximumFractionDigits: 1 })} km`;
}

export function formatActiveDuration(ms: number | null | undefined): string {
  return formatMillis(ms);
}

export function formatStartFinish(start?: string | null, finish?: string | null): string {
  if (!start && !finish) {
    return '—';
  }
  if (!finish || start === finish) {
    return formatClock(start);
  }
  if (sameAmsterdamDay(start, finish)) {
    return `${formatClock(start)} – ${formatClock(finish)}`;
  }
  return `${formatClock(start, true)} – ${formatClock(finish, true)}`;
}

function sameAmsterdamDay(a?: string | null, b?: string | null): boolean {
  if (!a || !b) {
    return false;
  }
  return amsterdamClockDate(a) === amsterdamClockDate(b);
}

function amsterdamClockDate(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value.slice(0, 10);
  }
  return date.toLocaleDateString('en-CA', { timeZone: 'Europe/Amsterdam' });
}

function formatClock(value?: string | null, withDate = false): string {
  if (!value) {
    return '—';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  if (withDate) {
    return date.toLocaleString('nl-NL', {
      timeZone: 'Europe/Amsterdam',
      day: 'numeric',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
  return date.toLocaleTimeString('nl-NL', {
    timeZone: 'Europe/Amsterdam',
    hour: '2-digit',
    minute: '2-digit',
  });
}
