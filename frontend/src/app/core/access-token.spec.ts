import { accessTokenExpSeconds, isAccessTokenUsable } from './access-token';

function jwtWithExp(exp: number): string {
  const payload = btoa(JSON.stringify({ exp })).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
  return `eyJhbGciOiJub25lIn0.${payload}.sig`;
}

describe('access token', () => {
  it('treats missing token as unusable', () => {
    expect(isAccessTokenUsable(null)).toBe(false);
    expect(isAccessTokenUsable('')).toBe(false);
  });

  it('treats opaque tokens as usable', () => {
    expect(isAccessTokenUsable('e2e')).toBe(true);
    expect(accessTokenExpSeconds('e2e')).toBeNull();
  });

  it('rejects JWT whose exp is in the past', () => {
    const exp = Math.floor(Date.now() / 1000) - 60;
    expect(isAccessTokenUsable(jwtWithExp(exp))).toBe(false);
  });

  it('accepts JWT whose exp is in the future', () => {
    const exp = Math.floor(Date.now() / 1000) + 3600;
    expect(isAccessTokenUsable(jwtWithExp(exp))).toBe(true);
  });
});
