export function durationToMillis(raw?: string | null): number | null {
  if (!raw) {
    return null;
  }
  const value = raw.trim();
  if (!value) {
    return null;
  }
  const clock = /^(?:(\d+):)?(\d+):(\d+)(?:[.,](\d{1,3}))?$/.exec(value);
  if (clock) {
    const hours = clock[1] ? Number(clock[1]) : 0;
    const minutes = Number(clock[2]);
    const seconds = Number(clock[3]);
    return ((hours * 60 + minutes) * 60 + seconds) * 1000 + padMillis(clock[4]);
  }
  const secondsOnly = /^(\d+)(?:[.,](\d{1,3}))$/.exec(value);
  if (secondsOnly) {
    return Number(secondsOnly[1]) * 1000 + padMillis(secondsOnly[2]);
  }
  const asSeconds = Number(value.replace(',', '.'));
  if (!Number.isFinite(asSeconds)) {
    return null;
  }
  return Math.round(asSeconds * 1000);
}

export function formatMillis(ms: number | null | undefined): string {
  if (ms == null || !Number.isFinite(ms) || ms < 0) {
    return '—';
  }
  const totalSec = Math.round(ms / 1000);
  const hours = Math.floor(totalSec / 3600);
  const minutes = Math.floor((totalSec % 3600) / 60);
  const seconds = totalSec % 60;
  const pad = (n: number) => String(n).padStart(2, '0');
  if (hours > 0) {
    return `${hours}:${pad(minutes)}:${pad(seconds)}`;
  }
  return `${minutes}:${pad(seconds)}`;
}

function padMillis(fraction?: string): number {
  if (!fraction) {
    return 0;
  }
  return Number((fraction + '000').slice(0, 3));
}
