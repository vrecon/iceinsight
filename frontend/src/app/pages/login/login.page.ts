import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import {
  IonButton,
  IonContent,
  IonHeader,
  IonInput,
  IonItem,
  IonList,
  IonNote,
  IonSpinner,
  IonTitle,
  IonToolbar,
} from '@ionic/angular';
import { apiErrorMessage } from '../../core/api-error';
import { SessionService } from '../../core/session.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
  imports: [
    ReactiveFormsModule,
    RouterLink,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonList,
    IonItem,
    IonInput,
    IonButton,
    IonNote,
    IonSpinner,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage {
  private readonly fb = inject(FormBuilder);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  readonly busy = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  submit(): void {
    if (this.form.invalid || this.busy()) {
      this.form.markAllAsTouched();
      return;
    }
    this.busy.set(true);
    this.error.set(null);
    this.session.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.busy.set(false);
        void this.router.navigate(['/tabs/ritten']);
      },
      error: (err) => {
        this.busy.set(false);
        this.error.set(apiErrorMessage(err));
      },
    });
  }
}
