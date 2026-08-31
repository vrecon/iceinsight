import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./pages/login/login.page').then((m) => m.LoginPage),
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () => import('./pages/register/register.page').then((m) => m.RegisterPage),
  },
  {
    path: 'tabs',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/tabs.page').then((m) => m.TabsPage),
    children: [
      {
        path: 'ritten',
        loadComponent: () => import('./pages/activities/activities.page').then((m) => m.ActivitiesPage),
      },
      {
        path: 'seizoenen',
        loadComponent: () => import('./pages/seasons/seasons.page').then((m) => m.SeasonsPage),
      },
      {
        path: 'chips',
        loadComponent: () => import('./pages/chips/chips.page').then((m) => m.ChipsPage),
      },
      { path: '', redirectTo: 'ritten', pathMatch: 'full' },
    ],
  },
  {
    path: 'ritten/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/activity-detail/activity-detail.page').then((m) => m.ActivityDetailPage),
  },
  {
    path: 'seizoenen/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/season-detail/season-detail.page').then((m) => m.SeasonDetailPage),
  },
  { path: '', redirectTo: 'tabs/ritten', pathMatch: 'full' },
  { path: '**', redirectTo: 'tabs/ritten' },
];
