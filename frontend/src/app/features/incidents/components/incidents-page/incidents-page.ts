import { Component, DestroyRef, OnInit, computed, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize, switchMap } from 'rxjs';

import { apiErrorMessage } from '../../../../core/utils/api-error';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';
import { Icon } from '../../../../shared/components/icon/icon';
import { NoticeBanner } from '../../../../shared/components/notice-banner/notice-banner';
import { PageHeader } from '../../../../shared/components/page-header/page-header';
import { Notice } from '../../../../shared/models/notice.model';
import { Incident } from '../../models/incident.model';
import { IncidentService } from '../../services/incident.service';
import { IncidentList } from '../incident-list/incident-list';

@Component({
  selector: 'app-incidents-page',
  imports: [FormsModule, RouterLink, IncidentList, PageHeader, NoticeBanner, ConfirmDialog, Icon],
  templateUrl: './incidents-page.html',
  styleUrl: './incidents-page.css',
})
export class IncidentsPage implements OnInit {
  private readonly service = inject(IncidentService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly confirmation = viewChild.required(ConfirmDialog);

  readonly incidents = signal<Incident[]>([]);
  readonly search = signal('');
  readonly status = signal('all');
  readonly loading = signal(false);
  readonly pending = signal<Incident | null>(null);
  readonly notice = signal<Notice | null>(null);

  readonly records = computed(() => {
    const query = this.search().trim().toLowerCase();
    return this.incidents().filter(
      (incident) =>
        (this.status() === 'all' || incident.status === this.status()) &&
        `${incident.id} ${incident.title} ${incident.rootCause}`.toLowerCase().includes(query),
    );
  });

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    if (this.loading()) return;
    this.loading.set(true);
    this.service
      .list()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (incidents) => this.incidents.set(incidents),
        error: (error) =>
          this.notice.set({
            kind: 'error',
            message: apiErrorMessage(
              error,
              'Could not load incidents. The backend needs the GET /api/incidents endpoint.',
            ),
          }),
      });
  }

  requestResolution(incident: Incident): void {
    if (this.loading()) return;
    this.pending.set(incident);
    this.confirmation().open();
  }

  resolve(): void {
    const incident = this.pending();
    if (!incident || this.loading()) return;
    this.loading.set(true);
    this.notice.set(null);
    this.service
      .resolveIncident(incident.id)
      .pipe(
        switchMap((result) => {
          this.notice.set({ kind: result.resolved ? 'success' : 'info', message: result.message });
          return this.service.list();
        }),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (incidents) => this.incidents.set(incidents),
        error: (error) =>
          this.notice.set({ kind: 'error', message: apiErrorMessage(error, 'Could not resolve the incident.') }),
      });
  }
}
