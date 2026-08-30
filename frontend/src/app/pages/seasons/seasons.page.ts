import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
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
import { SeasonSummary } from '../../api/models/season-summary';
import { SeasonsService } from '../../api/services/seasons.service';
import { apiErrorMessage } from '../../core/api-error';
import { displayDuration, formatDate } from '../../core/best-n';
import { EmptyStateComponent } from '../../shared/empty-state.component';

@Component({
  selector: 'app-seasons',
  templateUrl: './seasons.page.html',
  styleUrls: ['./seasons.page.scss'],
  imports: [
    RouterLink,
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
export class SeasonsPage {
  private readonly seasonsApi = inject(SeasonsService);

  readonly seasons = signal<SeasonSummary[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor() {
    this.reload();
  }

  reload(event?: CustomEvent): void {
    this.loading.set(true);
    this.error.set(null);
    this.seasonsApi.listCurrentUserSeasons().subscribe({
      next: (items) => {
        this.seasons.set(items);
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

  formatDate = formatDate;
  displayDuration = displayDuration;
}
