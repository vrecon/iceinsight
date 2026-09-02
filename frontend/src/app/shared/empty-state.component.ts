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
      font-size: 32px;
      margin-bottom: 12px;
      color: var(--ii-rest);
    }
    .title {
      margin: 0 0 8px;
      color: var(--ii-ink);
      font-weight: 500;
      font-size: var(--ii-body);
    }
    .muted {
      margin: 0 auto;
      max-width: 28rem;
      color: var(--ii-ink-soft);
      font-size: var(--ii-meta);
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
