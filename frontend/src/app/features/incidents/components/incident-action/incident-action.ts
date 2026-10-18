import { Component, DestroyRef, effect, inject, input, output, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';

import { apiErrorMessage } from '../../../../core/utils/api-error';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';
import { Icon } from '../../../../shared/components/icon/icon';
import { NoticeBanner } from '../../../../shared/components/notice-banner/notice-banner';
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge';
import { Notice } from '../../../../shared/models/notice.model';
import { Incident } from '../../models/incident.model';
import { IncidentService } from '../../services/incident.service';

@Component({
  selector: 'app-incident-action',
  imports: [ConfirmDialog, NoticeBanner, StatusBadge, Icon],
  templateUrl: './incident-action.html',
  styleUrl: './incident-action.css',
})
export class IncidentAction {
  private readonly service = inject(IncidentService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly confirmation = viewChild.required(ConfirmDialog);

  readonly rootCause = input.required<string>();
  readonly initialIncident = input<Incident | null>(null);
  readonly disabled = input(false);

  readonly busyChange = output<boolean>();
  readonly resolved = output<void>();
  readonly recorded = output<void>();

  readonly loading = signal(false);
  readonly incident = signal<Incident | null>(null);
  readonly notice = signal<Notice | null>(null);

  constructor() {
    // Start fresh whenever the selected root cause (or its incident) changes.
    effect(() => {
      this.rootCause();
      this.incident.set(this.initialIncident());
      this.notice.set(null);
    });
  }

  create(): void {
    if (this.disabled() || this.loading()) return;
    this.setLoading(true);
    this.notice.set(null);
    this.service
      .createIncident(this.rootCause())
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.setLoading(false)),
      )
      .subscribe({
        next: (result) => {
          if (result.incident) this.incident.set(result.incident);
          this.notice.set({ kind: result.created ? 'success' : 'info', message: result.message });
          this.recorded.emit();
        },
        error: (error) =>
          this.notice.set({ kind: 'error', message: apiErrorMessage(error, 'Could not create the incident.') }),
      });
  }

  requestResolution(): void {
    this.confirmation().open();
  }

  resolve(): void {
    const incident = this.incident();
    if (!incident || this.disabled() || this.loading()) return;
    this.setLoading(true);
    this.service
      .resolveIncident(incident.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.setLoading(false)),
      )
      .subscribe({
        next: (result) => {
          // resolved=false means the backend reports it was already closed.
          this.incident.set({ ...incident, status: 'closed' });
          this.notice.set({ kind: 'success', message: result.message });
          this.resolved.emit();
        },
        error: (error) =>
          this.notice.set({ kind: 'error', message: apiErrorMessage(error, 'Could not resolve the incident.') }),
      });
  }

  private setLoading(value: boolean): void {
    this.loading.set(value);
    this.busyChange.emit(value);
  }
}
