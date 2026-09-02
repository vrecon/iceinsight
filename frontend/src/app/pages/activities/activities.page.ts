import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonNote,
  IonSpinner,
  IonTitle,
  IonToolbar,
} from '@ionic/angular';
import { catchError, finalize, of, switchMap } from 'rxjs';
import { ActivitySummary } from '../../api/models/activity-summary';
import { ActivitiesService } from '../../api/services/activities.service';
import { SyncService } from '../../api/services/sync.service';
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
    IonButtons,
    IonButton,
    IonContent,
    IonNote,
    IonSpinner,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ActivitiesPage {
  private readonly activitiesApi = inject(ActivitiesService);
  private readonly syncApi = inject(SyncService);

  readonly activities = signal<ActivitySummary[]>([]);
  readonly loading = signal(true);
  readonly syncing = signal(false);
  readonly error = signal<string | null>(null);

  ionViewWillEnter(): void {
    this.reload({ quiet: this.activities().length > 0 });
  }

  reload(opts?: { quiet?: boolean }): void {
    if (!opts?.quiet) {
      this.loading.set(true);
    }
    this.error.set(null);
    this.activitiesApi.listCurrentUserActivities().subscribe({
      next: (items) => {
        this.activities.set(items);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(apiErrorMessage(err));
        this.loading.set(false);
      },
    });
  }

  syncAndReload(): void {
    if (this.syncing()) {
      return;
    }
    this.syncing.set(true);
    this.error.set(null);
    this.syncApi
      .syncCurrentUser()
      .pipe(
        catchError((err) => {
          this.error.set(apiErrorMessage(err));
          return of(void 0);
        }),
        switchMap(() => this.activitiesApi.listCurrentUserActivities()),
        finalize(() => this.syncing.set(false)),
      )
      .subscribe({
        next: (items) => {
          this.activities.set(items);
          this.loading.set(false);
        },
        error: (err) => this.error.set(apiErrorMessage(err)),
      });
  }

  formatDateTime = formatDateTime;
  displayDuration = displayDuration;
  locationLabel = locationLabel;
}
