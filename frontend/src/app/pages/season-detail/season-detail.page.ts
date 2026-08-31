import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  IonBackButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonItem,
  IonLabel,
  IonList,
  IonNote,
  IonSegment,
  IonSegmentButton,
  IonSpinner,
  IonTitle,
  IonToolbar,
} from '@ionic/angular';
import { ActivitySummary } from '../../api/models/activity-summary';
import { SeasonSummary } from '../../api/models/season-summary';
import { SeasonTopEntry } from '../../api/models/season-top-entry';
import { ActivitiesService } from '../../api/services/activities.service';
import { SeasonsService } from '../../api/services/seasons.service';
import { apiErrorMessage } from '../../core/api-error';
import { BEST_N, BestN, displayDuration, formatDate, formatDateTime, locationLabel } from '../../core/best-n';
import { dailyFastestSeries } from '../../core/season-daily';
import { DailySeriesComponent } from '../../shared/daily-series.component';
import { KpiGridComponent } from '../../shared/kpi-grid.component';
import { EmptyStateComponent } from '../../shared/empty-state.component';

@Component({
  selector: 'app-season-detail',
  templateUrl: './season-detail.page.html',
  styleUrls: ['./season-detail.page.scss'],
  imports: [
    RouterLink,
    KpiGridComponent,
    EmptyStateComponent,
    DailySeriesComponent,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonBackButton,
    IonContent,
    IonNote,
    IonSpinner,
    IonSegment,
    IonSegmentButton,
    IonList,
    IonItem,
    IonLabel,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SeasonDetailPage {
  private readonly seasonsApi = inject(SeasonsService);
  private readonly activitiesApi = inject(ActivitiesService);
  private readonly route = inject(ActivatedRoute);

  readonly season = signal<SeasonSummary | null>(null);
  readonly activities = signal<ActivitySummary[]>([]);
  readonly top = signal<SeasonTopEntry[]>([]);
  readonly n = signal<BestN>(13);
  readonly loading = signal(true);
  readonly topLoading = signal(false);
  readonly activitiesLoading = signal(true);
  readonly error = signal<string | null>(null);
  readonly ns = BEST_N;

  readonly daily = computed(() => {
    const season = this.season();
    if (!season) {
      return [];
    }
    return dailyFastestSeries(this.activities(), season.startDate, season.endDate, this.n());
  });

  private readonly seasonId = Number(this.route.snapshot.paramMap.get('id'));

  constructor() {
    if (!this.seasonId) {
      this.loading.set(false);
      this.activitiesLoading.set(false);
      this.error.set('Ongeldig seizoen.');
      return;
    }
    this.seasonsApi.getCurrentUserSeason({ id: this.seasonId }).subscribe({
      next: (season) => {
        this.season.set(season);
        this.loading.set(false);
        this.loadTop(this.n());
      },
      error: (err) => {
        this.error.set(apiErrorMessage(err));
        this.loading.set(false);
      },
    });
    this.activitiesApi.listCurrentUserActivities().subscribe({
      next: (items) => {
        this.activities.set(items);
        this.activitiesLoading.set(false);
      },
      error: () => {
        this.activitiesLoading.set(false);
      },
    });
  }

  onNChange(event: CustomEvent): void {
    const value = Number(event.detail.value) as BestN;
    if (!BEST_N.includes(value)) {
      return;
    }
    this.n.set(value);
    this.loadTop(value);
  }

  private loadTop(n: BestN): void {
    this.topLoading.set(true);
    this.seasonsApi.listCurrentUserSeasonTop({ id: this.seasonId, n, limit: 15 }).subscribe({
      next: (entries) => {
        this.top.set(entries);
        this.topLoading.set(false);
      },
      error: (err) => {
        this.error.set(apiErrorMessage(err));
        this.topLoading.set(false);
      },
    });
  }

  formatDate = formatDate;
  formatDateTime = formatDateTime;
  displayDuration = displayDuration;
  locationLabel = locationLabel;
}
