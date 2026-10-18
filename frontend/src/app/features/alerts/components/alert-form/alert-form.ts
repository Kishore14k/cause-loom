import { Component, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AlertRequest, AlertSeverity } from '../../models/alert.model';

@Component({
  selector: 'app-alert-form',
  imports: [FormsModule],
  templateUrl: './alert-form.html',
  styleUrl: './alert-form.css',
})
export class AlertForm {
  readonly loading = input(false);
  readonly submitted = output<AlertRequest>();
  readonly cancelled = output<void>();

  service = '';
  severity: AlertSeverity = 'critical';
  message = '';

  submit(): void {
    if (this.loading() || !this.service.trim() || !this.message.trim()) return;
    this.submitted.emit({
      service: this.service.trim(),
      severity: this.severity,
      message: this.message.trim(),
    });
  }
}
