import { durationToMillis } from './lap-time';

describe('durationToMillis', () => {
  it('parses Speedhive clock and seconds strings', () => {
    expect(durationToMillis('50.839')).toBe(50839);
    expect(durationToMillis('1:03.173')).toBe(63173);
    expect(durationToMillis('00:00:32.100')).toBe(32100);
  });

  it('returns null for empty input', () => {
    expect(durationToMillis(undefined)).toBeNull();
    expect(durationToMillis('  ')).toBeNull();
  });
});
