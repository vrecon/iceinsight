import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { SessionService } from './session.service';

function isAuthUrl(url: string): boolean {
  return (
    url.includes('/api/v1/auth/login') ||
    url.includes('/api/v1/auth/register') ||
    url.includes('/api/v1/auth/refresh-token') ||
    url.includes('/api/v1/auth/forgot-password') ||
    url.includes('/api/v1/auth/reset-password')
  );
}

let refreshInFlight = false;

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionService);
  const router = inject(Router);
  const token = session.accessToken();

  let headers = req.headers;
  if (token && !headers.has('Authorization') && !req.url.includes('/api/v1/auth/login') && !req.url.includes('/api/v1/auth/register')) {
    headers = headers.set('Authorization', `Bearer ${token}`);
  }

  const authed = req.clone({
    headers,
    withCredentials: true,
  });

  return next(authed).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse) || error.status !== 401 || isAuthUrl(req.url) || refreshInFlight) {
        return throwError(() => error);
      }
      refreshInFlight = true;
      return session.refreshSession().pipe(
        switchMap(() => {
          refreshInFlight = false;
          const retryToken = session.accessToken();
          const retry = authed.clone({
            setHeaders: retryToken ? { Authorization: `Bearer ${retryToken}` } : {},
            withCredentials: true,
          });
          return next(retry);
        }),
        catchError((refreshError) => {
          refreshInFlight = false;
          session.clear();
          void router.navigate(['/login']);
          return throwError(() => refreshError);
        }),
      );
    }),
  );
};
