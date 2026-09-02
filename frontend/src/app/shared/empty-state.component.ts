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
      padding: calc(var(--space) * 12) calc(var(--space) * 5);
    }
    ion-icon {
      font-size: 28px;
      margin-bottom: calc(var(--space) * 3);
      color: var(--rest);
    }
    .title {
      margin: 0 0 calc(var(--space) * 2);
      color: var(--ink);
      font-weight: 500;
      font-size: var(--type-title-size);
    }
    .muted {
      margin: 0;
      color: var(--ink-soft);
      font-size: var(--type-meta-size);
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
