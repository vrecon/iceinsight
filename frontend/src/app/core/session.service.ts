import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { AuthService } from '../api/services/auth.service';
import { AuthResponse } from '../api/models/auth-response';
import { LoginRequest } from '../api/models/login-request';
import { RegisterRequest } from '../api/models/register-request';
import { User } from '../api/models/user';
import { isAccessTokenUsable } from './access-token';

const TOKEN_KEY = 'iceinsights.accessToken';
const REFRESH_KEY = 'iceinsights.refreshToken';
const USER_KEY = 'iceinsights.user';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly authApi = inject(AuthService);

  private readonly token = signal<string | null>(readString(TOKEN_KEY));
  private readonly refresh = signal<string | null>(readString(REFRESH_KEY));
  private readonly currentUser = signal<User | null>(readJson<User>(USER_KEY));

  readonly accessToken = this.token.asReadonly();
  readonly refreshToken = this.refresh.asReadonly();
  readonly user = this.currentUser.asReadonly();
  readonly isAuthenticated = computed(() => isAccessTokenUsable(this.token()));

  applyAuth(response: AuthResponse): void {
    this.token.set(response.token ?? null);
    this.refresh.set(response.refreshToken ?? null);
    this.currentUser.set(response.user ?? null);
    writeString(TOKEN_KEY, response.token);
    writeString(REFRESH_KEY, response.refreshToken);
    writeJson(USER_KEY, response.user ?? null);
  }

  clear(): void {
    this.token.set(null);
    this.refresh.set(null);
    this.currentUser.set(null);
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
    localStorage.removeItem(USER_KEY);
  }

  login(body: LoginRequest): Observable<AuthResponse> {
    return this.authApi.login({ body }).pipe(tap((response) => this.applyAuth(response)));
  }

  register(body: RegisterRequest): Observable<AuthResponse> {
    return this.authApi.register({ body }).pipe(tap((response) => this.applyAuth(response)));
  }

  refreshSession(): Observable<AuthResponse> {
    const refreshToken = this.refresh();
    return this.authApi
      .refreshToken(refreshToken ? { body: { refreshToken } } : {})
      .pipe(tap((response) => this.applyAuth(response)));
  }

  logout(): Observable<void> {
    return this.authApi.logout().pipe(
      tap({
        next: () => this.clear(),
        error: () => this.clear(),
      }),
    );
  }

  displayName(): string {
    const user = this.currentUser();
    if (!user) {
      return '';
    }
    const parts = [user.firstName, user.middleName, user.lastName].filter((part) => !!part);
    if (parts.length) {
      return parts.join(' ');
    }
    return user.username ?? user.email ?? '';
  }
}

function readString(key: string): string | null {
  try {
    return localStorage.getItem(key);
  } catch {
    return null;
  }
}

function writeString(key: string, value?: string | null): void {
  try {
    if (value) {
      localStorage.setItem(key, value);
    } else {
      localStorage.removeItem(key);
    }
  } catch {
    /* ignore quota / private mode */
  }
}

function readJson<T>(key: string): T | null {
  const raw = readString(key);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as T;
  } catch {
    return null;
  }
}

function writeJson(key: string, value: unknown): void {
  if (value === null || value === undefined) {
    writeString(key, null);
    return;
  }
  writeString(key, JSON.stringify(value));
}
