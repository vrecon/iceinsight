import { HttpErrorResponse } from '@angular/common/http';

export function apiErrorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'Kan de server niet bereiken. Draait de API op http://localhost:8086?';
    }
    const body = err.error as { message?: string } | string | null;
    if (typeof body === 'string' && body.trim()) {
      return body;
    }
    if (body && typeof body === 'object' && typeof body.message === 'string' && body.message.trim()) {
      return body.message;
    }
    if (err.status === 401) {
      return 'Niet ingelogd of sessie verlopen.';
    }
    if (err.status === 403) {
      return 'Geen toegang.';
    }
    if (err.status === 404) {
      return 'Niet gevonden.';
    }
    return err.statusText ? `Fout ${err.status}: ${err.statusText}` : `Fout ${err.status}`;
  }
  return 'Er ging iets mis.';
}
