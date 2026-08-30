import { ChangeDetectionStrategy, Component, computed, input, linkedSignal } from '@angular/core';
import { ActivityLap } from '../api/models/activity-lap';
import { durationToMillis } from '../core/lap-time';

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
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LapChartComponent {
  readonly laps = input<ActivityLap[]>([]);

  readonly groups = computed(() => buildLapChart(this.laps()));
  readonly skateCount = computed(() => this.groups().reduce((n, g) => n + g.skateCount, 0));
  readonly restCount = computed(() => this.laps().filter((lap) => lap.rest).length);
  readonly selected = linkedSignal<LapBarView | null>(() => fastestBar(this.groups()));

  select(bar: LapBarView): void {
    this.selected.set(bar);
  }

  speedLabel(speed?: number): string {
    if (speed === undefined || speed === null || Number.isNaN(speed)) {
      return '';
    }
    return `${speed.toLocaleString('nl-NL', { maximumFractionDigits: 1 })} km/h`;
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

function fastestBar(groups: LapSessionGroup[]): LapBarView | null {
  for (const group of groups) {
    const hit = group.bars.find((bar) => bar.fastest);
    if (hit) {
      return hit;
    }
  }
  return groups[0]?.bars[0] ?? null;
}

function buildLapChart(laps: ActivityLap[]): LapSessionGroup[] {
  const skateMillis = laps
    .filter((lap) => !lap.rest)
    .map((lap) => durationToMillis(lap.duration))
    .filter((ms): ms is number => ms != null && ms > 0);
  const maxMs = skateMillis.length ? Math.max(...skateMillis) : 0;
  const minMs = skateMillis.length ? Math.min(...skateMillis) : 0;
  const floor = maxMs > 0 ? Math.max(0, Math.min(minMs * 0.65, minMs - 8000)) : 0;
  const span = Math.max(maxMs - floor, 1);

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
    const heightPct = rest
      ? 14
      : millis == null
        ? 8
        : 12 + ((millis - floor) / span) * 88;
    const avgPct =
      rest || avgMillis == null ? null : 12 + ((avgMillis - floor) / span) * 88;
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
