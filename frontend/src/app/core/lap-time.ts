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

function padMillis(fraction?: string): number {
  if (!fraction) {
    return 0;
  }
  return Number((fraction + '000').slice(0, 3));
}
