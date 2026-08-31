import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import {
  IonContent,
  IonHeader,
  IonItem,
  IonLabel,
  IonList,
  IonNote,
  IonRefresher,
  IonRefresherContent,
  IonSpinner,
  IonTitle,
  IonToolbar,
} from '@ionic/angular';
import { ActivitySummary } from '../../api/models/activity-summary';
import { ActivitiesService } from '../../api/services/activities.service';
import { apiErrorMessage } from '../../core/api-error';
import { displayDuration, formatDateTime, locationLabel } from '../../core/best-n';
import { EmptyStateComponent } from '../../shared/empty-state.component';

@Component({
  selector: 'app-activities',
  templateUrl: './activities.page.html',
  styleUrls: ['./activities.page.scss'],
  imports: [
    EmptyStateComponent,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonRefresher,
    IonRefresherContent,
    IonList,
    IonItem,
    IonLabel,
    IonNote,
    IonSpinner,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ActivitiesPage {
  private readonly activitiesApi = inject(ActivitiesService);
  private readonly router = inject(Router);

  readonly activities = signal<ActivitySummary[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor() {
    this.reload();
  }

  reload(event?: CustomEvent): void {
    this.loading.set(true);
    this.error.set(null);
    this.activitiesApi.listCurrentUserActivities().subscribe({
      next: (items) => {
        this.activities.set(items);
        this.loading.set(false);
        (event?.detail as { complete?: () => void } | undefined)?.complete?.();
      },
      error: (err) => {
        this.error.set(apiErrorMessage(err));
        this.loading.set(false);
        (event?.detail as { complete?: () => void } | undefined)?.complete?.();
      },
    });
  }

  openRit(id?: number): void {
    if (id == null) {
      return;
    }
    // Absolute URL on the root outlet. ion-item routerLink uses the closest
    // ion-router-outlet (the tabs outlet), which has no ritten/:id child and
    // bounced back to the list via the ** redirect.
    void this.router.navigateByUrl(`/ritten/${id}`);
  }

  formatDateTime = formatDateTime;
  displayDuration = displayDuration;
  locationLabel = locationLabel;
}
