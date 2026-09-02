import { afterEach, describe, expect, it, vi } from 'vitest';
import { accessTokenExpSeconds, isAccessTokenUsable } from './access-token';

function jwtWithExp(exp: number): string {
  const payload = btoa(JSON.stringify({ exp })).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
  return `eyJhbGciOiJub25lIn0.${payload}.sig`;
}

describe('access token', () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it('treats missing token as unusable', () => {
    expect(isAccessTokenUsable(null)).toBe(false);
    expect(isAccessTokenUsable('')).toBe(false);
  });

  it('treats opaque tokens as usable', () => {
    expect(isAccessTokenUsable('e2e')).toBe(true);
    expect(accessTokenExpSeconds('e2e')).toBeNull();
  });

  it('rejects JWT whose exp is in the past', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-09-02T10:00:00Z'));
    expect(isAccessTokenUsable(jwtWithExp(Math.floor(Date.parse('2026-09-02T09:00:00Z') / 1000)))).toBe(false);
  });

  it('accepts JWT whose exp is in the future', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-09-02T10:00:00Z'));
    expect(isAccessTokenUsable(jwtWithExp(Math.floor(Date.parse('2026-09-02T11:00:00Z') / 1000)))).toBe(true);
  });
});
