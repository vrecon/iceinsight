import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import {
  IonButtons,
  IonContent,
  IonHeader,
  IonMenuButton,
  IonNote,
  IonSpinner,
  IonTitle,
  IonToolbar,
} from '@ionic/angular';
import { catchError, forkJoin, of } from 'rxjs';
import { SeasonSummary } from '../../api/models/season-summary';
import { SeasonTopEntry } from '../../api/models/season-top-entry';
import { SeasonsService } from '../../api/services/seasons.service';
import { apiErrorMessage } from '../../core/api-error';
import { displayDuration, formatDate, formatDateTime, locationLabel } from '../../core/best-n';
import { EmptyStateComponent } from '../../shared/empty-state.component';
import { KpiGridComponent } from '../../shared/kpi-grid.component';

@Component({
  selector: 'app-records',
  templateUrl: './records.page.html',
  styleUrls: ['./records.page.scss'],
  imports: [
    EmptyStateComponent,
    KpiGridComponent,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonMenuButton,
    IonContent,
    IonNote,
    IonSpinner,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RecordsPage {
  private readonly seasonsApi = inject(SeasonsService);

  readonly season = signal<SeasonSummary | null>(null);
  readonly top = signal<SeasonTopEntry[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ionViewWillEnter(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.seasonsApi.listCurrentUserSeasons().subscribe({
      next: (items) => {
        const picked = pickCurrentOrLatestSeason(items);
        const id = picked?.id;
        if (!id) {
          this.season.set(null);
          this.top.set([]);
          this.loading.set(false);
          return;
        }
        forkJoin({
          season: this.seasonsApi.getCurrentUserSeason({ id }),
          top: this.seasonsApi.listCurrentUserSeasonTop({ id, n: 13, limit: 15 }).pipe(
            catchError(() => of([] as SeasonTopEntry[])),
          ),
        }).subscribe({
          next: ({ season, top }) => {
            this.season.set(season);
            this.top.set(top);
            this.loading.set(false);
          },
          error: (err) => {
            this.error.set(apiErrorMessage(err));
            this.season.set(picked);
            this.loading.set(false);
          },
        });
      },
      error: (err) => {
        this.error.set(apiErrorMessage(err));
        this.loading.set(false);
      },
    });
  }

  formatDate = formatDate;
  formatDateTime = formatDateTime;
  displayDuration = displayDuration;
  locationLabel = locationLabel;
}

export function pickCurrentOrLatestSeason(seasons: SeasonSummary[], now = new Date()): SeasonSummary | null {
  if (!seasons.length) {
    return null;
  }
  const today = toIsoDate(now);
  const current = seasons.find((season) => {
    const start = season.startDate;
    const end = season.endDate;
    return !!start && !!end && start <= today && today <= end;
  });
  if (current) {
    return current;
  }
  return [...seasons].sort((a, b) => {
    const endCmp = (b.endDate ?? '').localeCompare(a.endDate ?? '');
    if (endCmp) {
      return endCmp;
    }
    return (b.id ?? 0) - (a.id ?? 0);
  })[0];
}

function toIsoDate(value: Date): string {
  const y = value.getFullYear();
  const m = String(value.getMonth() + 1).padStart(2, '0');
  const d = String(value.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}
