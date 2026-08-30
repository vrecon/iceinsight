import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonInput,
  IonItem,
  IonItemOption,
  IonItemOptions,
  IonItemSliding,
  IonLabel,
  IonList,
  IonNote,
  IonRefresher,
  IonRefresherContent,
  IonSpinner,
  IonTitle,
  IonToolbar,
} from '@ionic/angular';
import { ChipDto } from '../../api/models/chip-dto';
import { ChipsService } from '../../api/services/chips.service';
import { apiErrorMessage } from '../../core/api-error';
import { formatDateTime } from '../../core/best-n';
import { SessionService } from '../../core/session.service';
import { EmptyStateComponent } from '../../shared/empty-state.component';

@Component({
  selector: 'app-chips',
  templateUrl: './chips.page.html',
  styleUrls: ['./chips.page.scss'],
  imports: [
    ReactiveFormsModule,
    EmptyStateComponent,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonContent,
    IonRefresher,
    IonRefresherContent,
    IonList,
    IonItem,
    IonItemSliding,
    IonItemOptions,
    IonItemOption,
    IonLabel,
    IonNote,
    IonInput,
    IonSpinner,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChipsPage {
  private readonly chipsApi = inject(ChipsService);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly chips = signal<ChipDto[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly linking = signal(false);
  readonly userName = this.session.displayName();

  readonly form = this.fb.nonNullable.group({
    chipCode: ['', Validators.required],
  });

  constructor() {
    this.reload();
  }

  reload(event?: CustomEvent): void {
    this.loading.set(true);
    this.error.set(null);
    this.chipsApi.getCurrentUserChips().subscribe({
      next: (chips) => {
        this.chips.set(chips);
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

  link(): void {
    if (this.form.invalid || this.linking()) {
      this.form.markAllAsTouched();
      return;
    }
    this.linking.set(true);
    this.error.set(null);
    this.chipsApi.linkChipToCurrentUser({ chipCode: this.form.controls.chipCode.value.trim() }).subscribe({
      next: () => {
        this.linking.set(false);
        this.form.reset();
        this.reload();
      },
      error: (err) => {
        this.linking.set(false);
        this.error.set(apiErrorMessage(err));
      },
    });
  }

  unlink(chip: ChipDto): void {
    if (!chip.chipCode) {
      return;
    }
    this.chipsApi.unlinkChipFromCurrentUser({ chipCode: chip.chipCode }).subscribe({
      next: () => this.reload(),
      error: (err) => this.error.set(apiErrorMessage(err)),
    });
  }

  logout(): void {
    this.session.logout().subscribe({
      next: () => void this.router.navigate(['/login']),
      error: () => void this.router.navigate(['/login']),
    });
  }

  formatDateTime = formatDateTime;
}
