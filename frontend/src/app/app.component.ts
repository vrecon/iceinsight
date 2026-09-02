import { afterNextRender, ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  IonApp,
  IonContent,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonMenu,
  IonRouterOutlet,
  IonTitle,
  IonToolbar,
  MenuController,
} from '@ionic/angular';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  imports: [
    RouterLink,
    IonApp,
    IonMenu,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonList,
    IonItem,
    IonIcon,
    IonLabel,
    IonRouterOutlet,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppComponent {
  private readonly menu = inject(MenuController);

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

  closeMenu(): void {
    void this.menu.close('main-menu');
  }
}
