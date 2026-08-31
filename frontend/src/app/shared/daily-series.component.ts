import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { DailyFastest } from '../core/season-daily';

type PlottedPoint = DailyFastest & { x: number; y: number; label: string };

@Component({
  selector: 'app-daily-series',
  template: `
    @if (points().length) {
      <div class="series">
        <svg viewBox="0 0 320 140" role="img" [attr.aria-label]="caption()">
          <line class="axis" x1="40" y1="12" x2="40" y2="112"></line>
          <line class="axis" x1="40" y1="112" x2="308" y2="112"></line>
          <text class="tick" x="36" y="16" text-anchor="end">{{ yTop() }}</text>
          <text class="tick" x="36" y="112" text-anchor="end">{{ yBottom() }}</text>
          <polyline class="line" [attr.points]="polyline()" fill="none"></polyline>
          @for (point of plotted(); track point.date) {
            <circle class="dot" [attr.cx]="point.x" [attr.cy]="point.y" r="3">
              <title>{{ point.label }}</title>
            </circle>
          }
          <text class="tick" x="40" y="132" text-anchor="start">{{ xFirst() }}</text>
          <text class="tick" x="308" y="132" text-anchor="end">{{ xLast() }}</text>
        </svg>
        <p class="muted caption">{{ caption() }}</p>
      </div>
    }
  `,
  styles: `
    .series {
      margin: 4px 0 16px;
      background: #fff;
      border: 1px solid var(--ion-border-color);
      border-radius: 14px;
      padding: 12px 10px 4px;
    }
    svg {
      display: block;
      width: 100%;
      height: auto;
    }
    .axis {
      stroke: var(--ion-border-color);
      stroke-width: 1;
    }
    .line {
      stroke: var(--ion-color-primary);
      stroke-width: 2;
      stroke-linejoin: round;
      stroke-linecap: round;
    }
    .dot {
      fill: var(--ion-color-primary-shade);
    }
    .tick {
      fill: var(--ion-color-medium);
      font-size: 8px;
    }
    .caption {
      margin: 4px 6px 8px;
      font-size: 0.78rem;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DailySeriesComponent {
  readonly points = input<DailyFastest[]>([]);
  readonly n = input<number>(1);

  readonly plotted = computed(() => layoutPoints(this.points()));
  readonly polyline = computed(() => this.plotted().map((p) => `${p.x},${p.y}`).join(' '));
  readonly yTop = computed(() => slowestDuration(this.plotted()));
  readonly yBottom = computed(() => fastestDuration(this.plotted()));
  readonly xFirst = computed(() => shortDate(this.points()[0]?.date));
  readonly xLast = computed(() => shortDate(this.points()[this.points().length - 1]?.date));
  readonly caption = computed(
    () => `Snelste ${this.n()} per ijsdag · ${this.points().length} dag${this.points().length === 1 ? '' : 'en'}`,
  );
}

function layoutPoints(points: DailyFastest[]): PlottedPoint[] {
  if (!points.length) {
    return [];
  }
  const padL = 40;
  const padR = 12;
  const padT = 12;
  const padB = 28;
  const plotW = 320 - padL - padR;
  const plotH = 140 - padT - padB;
  const times = points.map((p) => Date.parse(`${p.date}T12:00:00+01:00`));
  const minT = Math.min(...times);
  const maxT = Math.max(...times);
  const spanT = Math.max(maxT - minT, 1);
  const minMs = Math.min(...points.map((p) => p.millis));
  const maxMs = Math.max(...points.map((p) => p.millis));
  const spanMs = Math.max(maxMs - minMs, 1);

  return points.map((point, index) => {
    const t = times[index];
    const x = points.length === 1 ? padL + plotW / 2 : padL + ((t - minT) / spanT) * plotW;
    const y = padT + ((maxMs - point.millis) / spanMs) * plotH;
    return { ...point, x, y, label: `${shortDate(point.date)} · ${point.duration}` };
  });
}

function shortDate(value?: string): string {
  if (!value) {
    return '';
  }
  const date = new Date(`${value}T12:00:00+01:00`);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleDateString('nl-NL', { day: 'numeric', month: 'short' });
}

function slowestDuration(points: PlottedPoint[]): string {
  if (!points.length) {
    return '';
  }
  return points.reduce((slow, point) => (point.millis > slow.millis ? point : slow)).duration;
}

function fastestDuration(points: PlottedPoint[]): string {
  if (!points.length) {
    return '';
  }
  return points.reduce((fast, point) => (point.millis < fast.millis ? point : fast)).duration;
}
