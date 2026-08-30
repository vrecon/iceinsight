import { ApplicationConfig, provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, RouteReuseStrategy, withPreloading, PreloadAllModules } from '@angular/router';
import { IonicRouteStrategy, provideIonicAngular } from '@ionic/angular';
import { provideApiConfiguration } from './api/api-configuration';
import { authInterceptor } from './core/auth.interceptor';
import { routes } from './app.routes';
import { environment } from '../environments/environment';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZonelessChangeDetection(),
    provideIonicAngular({ mode: 'md' }),
    provideRouter(routes, withPreloading(PreloadAllModules)),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideApiConfiguration(environment.apiBaseUrl),
    { provide: RouteReuseStrategy, useClass: IonicRouteStrategy },
  ],
};
