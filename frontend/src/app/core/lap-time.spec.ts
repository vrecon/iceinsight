import { durationToMillis, formatMillis } from './lap-time';

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

describe('formatMillis', () => {
  it('formats minutes and hours', () => {
    expect(formatMillis(50839)).toBe('0:51');
    expect(formatMillis(63173)).toBe('1:03');
    expect(formatMillis(3723000)).toBe('1:02:03');
  });

  it('returns em dash for empty input', () => {
    expect(formatMillis(null)).toBe('—');
    expect(formatMillis(undefined)).toBe('—');
  });
});
