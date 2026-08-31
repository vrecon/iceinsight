import { ActivitySummary } from '../api/models/activity-summary';
import { amsterdamIsoDate, dailyFastestSeries } from './season-daily';

describe('amsterdamIsoDate', () => {
  it('uses Europe/Amsterdam calendar date', () => {
    expect(amsterdamIsoDate('2026-01-15T23:30:00+01:00')).toBe('2026-01-15');
    expect(amsterdamIsoDate('2026-01-15T23:30:00Z')).toBe('2026-01-16');
  });
});

describe('dailyFastestSeries', () => {
  const seasonStart = '2025-05-01';
  const seasonEnd = '2026-04-30';

  const activities: ActivitySummary[] = [
    { id: 1, startTime: '2025-04-30T18:00:00+02:00', best1Duration: '40.000', best13Duration: '9:00.000' },
    { id: 2, startTime: '2025-11-02T20:00:00+01:00', best1Duration: '42.000', best13Duration: '9:20.000' },
    { id: 3, startTime: '2025-11-02T21:00:00+01:00', best1Duration: '41.500', best13Duration: '9:10.000' },
    { id: 4, startTime: '2026-01-15T13:00:00+01:00', best1Duration: '40.200' },
    { id: 5, startTime: '2026-05-01T10:00:00+02:00', best1Duration: '39.000', best13Duration: '8:50.000' },
  ];

  it('keeps one point per ice-day inside 1 May–30 Apr and takes min duration for N', () => {
    const series1 = dailyFastestSeries(activities, seasonStart, seasonEnd, 1);
    expect(series1.map((p) => p.date)).toEqual(['2025-11-02', '2026-01-15']);
    expect(series1[0].duration).toBe('41.500');
    expect(series1[1].duration).toBe('40.200');

    const series13 = dailyFastestSeries(activities, seasonStart, seasonEnd, 13);
    expect(series13.map((p) => p.date)).toEqual(['2025-11-02']);
    expect(series13[0].duration).toBe('9:10.000');
  });

  it('skips days whose selected N is missing', () => {
    const series = dailyFastestSeries(
      [{ startTime: '2026-01-15T13:00:00+01:00', best1Duration: '40.0' }],
      seasonStart,
      seasonEnd,
      13,
    );
    expect(series).toEqual([]);
  });
});
