import { ActivityLap } from '../api/models/activity-lap';
import { buildLapChart, scaleToThreshPct } from './lap-chart.component';

describe('scaleToThreshPct', () => {
  it('maps the thresh window to the bar scale and clips outside', () => {
    expect(scaleToThreshPct(25_000, 25, 102)).toBeCloseTo(12);
    expect(scaleToThreshPct(102_000, 25, 102)).toBeCloseTo(100);
    expect(scaleToThreshPct(20_000, 25, 102)).toBeCloseTo(12);
    expect(scaleToThreshPct(120_000, 25, 102)).toBeCloseTo(100);
    const mid = scaleToThreshPct(63_500, 25, 102);
    expect(mid).toBeGreaterThan(12);
    expect(mid).toBeLessThan(100);
  });
});

describe('buildLapChart thresh', () => {
  const laps: ActivityLap[] = [
    { lapNr: 1, sessionNr: 1, duration: '20.000', rest: false },
    { lapNr: 2, sessionNr: 1, duration: '50.000', rest: false },
    { lapNr: 3, sessionNr: 1, duration: '180.000', rest: true },
    { lapNr: 4, sessionNr: 1, duration: '130.000', rest: false },
  ];

  it('keeps every lap on the timeline and clips active bars to 25–102s', () => {
    const groups = buildLapChart(laps, 25, 102);
    expect(groups).toHaveLength(1);
    expect(groups[0].bars).toHaveLength(4);
    expect(groups[0].skateCount).toBe(3);

    const [tooFast, inside, rest, tooSlow] = groups[0].bars;
    expect(tooFast.heightPct).toBeCloseTo(12);
    expect(inside.heightPct).toBeGreaterThan(12);
    expect(inside.heightPct).toBeLessThan(100);
    expect(rest.rest).toBe(true);
    expect(rest.heightPct).toBe(14);
    expect(tooSlow.heightPct).toBeCloseTo(100);
    expect(tooFast.fastest).toBe(true);
  });
});
