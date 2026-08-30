import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { BEST_N, BestDurations, bestDuration, displayDuration } from '../core/best-n';

@Component({
  selector: 'app-kpi-grid',
  template: `
    <div class="kpi-grid">
      @for (n of ns; track n) {
        <div class="kpi-card">
          <div class="n">Beste {{ n }}</div>
          <div class="value" [class.empty]="!bestDuration(model(), n)">
            {{ displayDuration(bestDuration(model(), n)) }}
          </div>
        </div>
      }
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KpiGridComponent {
  readonly model = input<BestDurations | null>(null);
  readonly ns = BEST_N;
  readonly bestDuration = bestDuration;
  readonly displayDuration = displayDuration;
}
