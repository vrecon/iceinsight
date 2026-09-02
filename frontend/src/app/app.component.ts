import { afterNextRender, ChangeDetectionStrategy, Component } from '@angular/core';
import { IonApp, IonRouterOutlet } from '@ionic/angular';

@Component({
  selector: 'app-root',
  template: `<ion-app><ion-router-outlet></ion-router-outlet></ion-app>`,
  imports: [IonApp, IonRouterOutlet],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppComponent {
  constructor() {
    afterNextRender(() => {
      const splash = document.getElementById('boot-splash');
      if (!splash) {
        return;
      }
      const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
      const holdMs = reduce ? 0 : 800;
      window.setTimeout(() => {
        splash.classList.add('is-out');
        window.setTimeout(() => splash.remove(), reduce ? 0 : 400);
      }, holdMs);
    });
  }
}
