import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import {
  IonBackButton,
  IonButtons,
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
  selector: 'app-register',
  templateUrl: './register.page.html',
  styleUrls: ['./register.page.scss'],
  imports: [
    ReactiveFormsModule,
    RouterLink,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonBackButton,
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
export class RegisterPage {
  private readonly fb = inject(FormBuilder);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  readonly busy = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    firstName: ['', Validators.required],
    middleName: [''],
    lastName: ['', Validators.required],
    username: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  submit(): void {
    if (this.form.invalid || this.busy()) {
      this.form.markAllAsTouched();
      return;
    }
    this.busy.set(true);
    this.error.set(null);
    const raw = this.form.getRawValue();
    this.session
      .register({
        firstName: raw.firstName,
        lastName: raw.lastName,
        username: raw.username,
        email: raw.email,
        password: raw.password,
        ...(raw.middleName ? { middleName: raw.middleName } : {}),
      })
      .subscribe({
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
