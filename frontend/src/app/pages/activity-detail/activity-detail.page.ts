import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  IonBackButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonNote,
  IonSpinner,
  IonTitle,
  IonToolbar,
} from '@ionic/angular';
import { ActivityLap } from '../../api/models/activity-lap';
import { ActivitySummary } from '../../api/models/activity-summary';
import { ActivitiesService } from '../../api/services/activities.service';
import { apiErrorMessage } from '../../core/api-error';
import { formatDateTime, locationLabel } from '../../core/best-n';
import { EmptyStateComponent } from '../../shared/empty-state.component';
import { KpiGridComponent } from '../../shared/kpi-grid.component';
import { LapChartComponent } from '../../shared/lap-chart.component';

@Component({
  selector: 'app-activity-detail',
  templateUrl: './activity-detail.page.html',
  styleUrls: ['./activity-detail.page.scss'],
  imports: [
    KpiGridComponent,
    EmptyStateComponent,
    LapChartComponent,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonBackButton,
    IonContent,
    IonNote,
    IonSpinner,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ActivityDetailPage {
  private readonly activitiesApi = inject(ActivitiesService);
  private readonly route = inject(ActivatedRoute);

  readonly activity = signal<ActivitySummary | null>(null);
  readonly laps = signal<ActivityLap[]>([]);
  readonly loading = signal(true);
  readonly lapsLoading = signal(true);
  readonly error = signal<string | null>(null);
  readonly lapsError = signal<string | null>(null);

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.loading.set(false);
      this.lapsLoading.set(false);
      this.error.set('Ongeldige rit.');
      return;
    }
    this.activitiesApi.getCurrentUserActivity({ id }).subscribe({
      next: (activity) => {
        this.activity.set(activity);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(apiErrorMessage(err));
        this.loading.set(false);
      },
    });
    this.activitiesApi.listCurrentUserActivityLaps({ id }).subscribe({
      next: (laps) => {
        this.laps.set(laps);
        this.lapsLoading.set(false);
      },
      error: (err) => {
        this.lapsError.set(lapsErrorMessage(err));
        this.lapsLoading.set(false);
      },
    });
  }

  formatDateTime = formatDateTime;
  locationLabel = locationLabel;
}

function lapsErrorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse && err.status === 404) {
    return 'Ronden niet gevonden voor deze rit.';
  }
  return apiErrorMessage(err);
}
