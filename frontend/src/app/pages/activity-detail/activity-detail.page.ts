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
import { ActivitySummary } from '../../api/models/activity-summary';
import { ActivitiesService } from '../../api/services/activities.service';
import { apiErrorMessage } from '../../core/api-error';
import { formatDateTime, locationLabel } from '../../core/best-n';
import { KpiGridComponent } from '../../shared/kpi-grid.component';

@Component({
  selector: 'app-activity-detail',
  templateUrl: './activity-detail.page.html',
  styleUrls: ['./activity-detail.page.scss'],
  imports: [
    KpiGridComponent,
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
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.loading.set(false);
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
  }

  formatDateTime = formatDateTime;
  locationLabel = locationLabel;
}
