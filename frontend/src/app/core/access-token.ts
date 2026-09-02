export function accessTokenExpSeconds(token: string): number | null {
  const parts = token.split('.');
  if (parts.length < 2) {
    return null;
  }
  try {
    const padded = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const json = atob(padded);
    const payload = JSON.parse(json) as { exp?: unknown };
    return typeof payload.exp === 'number' ? payload.exp : null;
  } catch {
    return null;
  }
}

/** Missing token is out. JWT with exp in the past is out. Opaque tokens (e2e) stay usable. */
export function isAccessTokenUsable(token: string | null | undefined): boolean {
  if (!token) {
    return false;
  }
  const exp = accessTokenExpSeconds(token);
  if (exp == null) {
    return true;
  }
  return exp * 1000 > Date.now();
}
