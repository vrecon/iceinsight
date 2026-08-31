import { ChangeDetectionStrategy, Component, computed, input, linkedSignal } from '@angular/core';
import { IonInput } from '@ionic/angular';
import { ActivityLap } from '../api/models/activity-lap';
import { durationToMillis } from '../core/lap-time';

export const DEFAULT_MIN_SEC = 25;
export const DEFAULT_MAX_SEC = 102;

export type LapBarView = {
  key: string;
  lap: ActivityLap;
  rest: boolean;
  millis: number | null;
  avgMillis: number | null;
  heightPct: number;
  avgPct: number | null;
  fastest: boolean;
};

export type LapSessionGroup = {
  sessionNr: number;
  bars: LapBarView[];
  skateCount: number;
};

@Component({
  selector: 'app-lap-chart',
  templateUrl: './lap-chart.component.html',
  styleUrls: ['./lap-chart.component.scss'],
  imports: [IonInput],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LapChartComponent {
  readonly laps = input<ActivityLap[]>([]);
  readonly minSec = input<number>(DEFAULT_MIN_SEC);
  readonly maxSec = input<number>(DEFAULT_MAX_SEC);

  readonly minSecValue = linkedSignal(() => this.minSec());
  readonly maxSecValue = linkedSignal(() => this.maxSec());

  readonly groups = computed(() => buildLapChart(this.laps(), this.minSecValue(), this.maxSecValue()));
  readonly skateCount = computed(() => this.groups().reduce((n, g) => n + g.skateCount, 0));
  readonly restCount = computed(() => this.laps().filter((lap) => lap.rest).length);
  readonly selected = linkedSignal<LapBarView | null>(() => fastestBar(this.groups()));

  select(bar: LapBarView): void {
    this.selected.set(bar);
  }

  onMinSec(event: CustomEvent): void {
    this.minSecValue.set(parseSec((event.detail as { value?: unknown }).value, this.minSecValue()));
  }

  onMaxSec(event: CustomEvent): void {
    this.maxSecValue.set(parseSec((event.detail as { value?: unknown }).value, this.maxSecValue()));
  }

  speedLabel(speed?: number): string {
    if (speed === undefined || speed === null || Number.isNaN(speed)) {
      return '';
    }
    return `${speed.toLocaleString('nl-NL', { maximumFractionDigits: 1 })} km/u`;
  }

  barLabel(bar: LapBarView): string {
    const nr = bar.lap.lapNr ?? '—';
    const sessie = bar.lap.sessionNr ?? '—';
    const tijd = bar.lap.duration || '—';
    if (bar.rest) {
      return `Rust ronde ${nr}, sessie ${sessie}, ${tijd}`;
    }
    return `Ronde ${nr}, sessie ${sessie}, ${tijd}`;
  }
}

export function parseSec(value: unknown, fallback: number): number {
  if (value === '' || value == null) {
    return fallback;
  }
  const n = typeof value === 'number' ? value : Number(String(value).replace(',', '.'));
  if (!Number.isFinite(n) || n < 0) {
    return fallback;
  }
  return n;
}

export function threshWindow(minSec: number, maxSec: number): { floorMs: number; ceilMs: number } {
  let lo = Number.isFinite(minSec) ? minSec : DEFAULT_MIN_SEC;
  let hi = Number.isFinite(maxSec) ? maxSec : DEFAULT_MAX_SEC;
  lo = Math.max(0, lo);
  if (hi < lo + 1) {
    hi = lo + 1;
  }
  return { floorMs: lo * 1000, ceilMs: hi * 1000 };
}

export function scaleToThreshPct(millis: number | null, minSec: number, maxSec: number): number {
  const { floorMs, ceilMs } = threshWindow(minSec, maxSec);
  const span = Math.max(ceilMs - floorMs, 1);
  if (millis == null) {
    return 8;
  }
  const clipped = Math.min(ceilMs, Math.max(floorMs, millis));
  return 12 + ((clipped - floorMs) / span) * 88;
}

function fastestBar(groups: LapSessionGroup[]): LapBarView | null {
  for (const group of groups) {
    const hit = group.bars.find((bar) => bar.fastest);
    if (hit) {
      return hit;
    }
  }
  return groups[0]?.bars[0] ?? null;
}

export function buildLapChart(laps: ActivityLap[], minSec = DEFAULT_MIN_SEC, maxSec = DEFAULT_MAX_SEC): LapSessionGroup[] {
  const skateMillis = laps
    .filter((lap) => !lap.rest)
    .map((lap) => durationToMillis(lap.duration))
    .filter((ms): ms is number => ms != null && ms > 0);
  const minMs = skateMillis.length ? Math.min(...skateMillis) : 0;

  const groups: LapSessionGroup[] = [];
  laps.forEach((lap, index) => {
    const sessionNr = lap.sessionNr ?? 0;
    let group = groups.find((item) => item.sessionNr === sessionNr);
    if (!group) {
      group = { sessionNr, bars: [], skateCount: 0 };
      groups.push(group);
    }
    const rest = !!lap.rest;
    const millis = durationToMillis(lap.duration);
    const avgMillis = durationToMillis(lap.movingAvgDuration);
    const fastest = !rest && millis != null && millis === minMs && minMs > 0;
    const heightPct = rest ? 14 : scaleToThreshPct(millis, minSec, maxSec);
    const avgPct = rest || avgMillis == null ? null : scaleToThreshPct(avgMillis, minSec, maxSec);
    group.bars.push({
      key: `${sessionNr}-${lap.lapNr ?? 'x'}-${index}`,
      lap,
      rest,
      millis,
      avgMillis,
      heightPct: Math.max(6, Math.min(100, heightPct)),
      avgPct: avgPct == null ? null : Math.max(0, Math.min(100, avgPct)),
      fastest,
    });
    if (!rest) {
      group.skateCount += 1;
    }
  });

  const firstFastest = groups.flatMap((g) => g.bars).find((bar) => bar.fastest);
  if (firstFastest) {
    for (const group of groups) {
      for (const bar of group.bars) {
        bar.fastest = bar === firstFastest;
      }
    }
  }
  return groups;
}
