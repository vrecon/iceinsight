import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { IonIcon } from '@ionic/angular';

@Component({
  selector: 'app-empty-state',
  imports: [IonIcon],
  template: `
    <div class="empty">
      <ion-icon [name]="icon()"></ion-icon>
      <p class="title">{{ title() }}</p>
      @if (hint()) {
        <p class="muted">{{ hint() }}</p>
      }
    </div>
  `,
  styles: `
    .empty {
      text-align: center;
      padding: 48px 20px;
    }
    ion-icon {
      font-size: 40px;
      margin-bottom: 12px;
      color: var(--ion-color-medium);
    }
    .title {
      margin: 0 0 8px;
      color: var(--ion-text-color);
      font-weight: 500;
    }
    .muted {
      margin: 0;
      color: var(--ion-color-medium);
      font-size: 0.9rem;
      line-height: 1.5;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmptyStateComponent {
  readonly title = input.required<string>();
  readonly hint = input<string>('');
  readonly icon = input<string>('information-circle-outline');
}
