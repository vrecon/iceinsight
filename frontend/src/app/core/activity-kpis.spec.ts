import { ActivityLap } from '../api/models/activity-lap';
import { computeActivityKpis } from './activity-kpis';

describe('computeActivityKpis', () => {
  const laps: ActivityLap[] = [
    { lapNr: 1, sessionNr: 1, duration: '50.000', rest: false, speedKph: 28.8, datetimeStart: '2026-01-15T13:00:00+01:00' },
    { lapNr: 2, sessionNr: 1, duration: '48.500', rest: false, speedKph: 29.7, datetimeStart: '2026-01-15T13:00:50+01:00' },
    { lapNr: 3, sessionNr: 1, duration: '180.000', rest: true, datetimeStart: '2026-01-15T13:01:40+01:00' },
    { lapNr: 4, sessionNr: 1, duration: '49.000', rest: false, datetimeStart: '2026-01-15T13:04:40+01:00' },
  ];

  it('uses active laps only for fastest, counts, distance and speeds', () => {
    const kpis = computeActivityKpis(laps, '2026-01-15T12:59:00+01:00', '2026-01-15T13:10:00+01:00');
    expect(kpis.fastestDuration).toBe('48.500');
    expect(kpis.activeCount).toBe(3);
    expect(kpis.restCount).toBe(1);
    expect(kpis.distanceKm).toBeCloseTo(1.2);
    expect(kpis.maxSpeedKph).toBeCloseTo(29.7);
    expect(kpis.avgSpeedKph).toBeCloseTo((28.8 + 29.7) / 2);
    expect(kpis.activeMillis).toBe(50000 + 48500 + 49000);
    expect(kpis.start).toBe('2026-01-15T13:00:00+01:00');
    expect(kpis.finish).toBe('2026-01-15T13:04:40+01:00');
  });

  it('falls back to activity start/end when laps have no datetime', () => {
    const kpis = computeActivityKpis([{ duration: '40.0', rest: false }], 'S', 'E');
    expect(kpis.start).toBe('S');
    expect(kpis.finish).toBe('E');
  });
});
