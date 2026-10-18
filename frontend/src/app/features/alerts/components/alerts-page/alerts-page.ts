import { DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize, map, switchMap } from 'rxjs';

import { apiErrorMessage } from '../../../../core/utils/api-error';
import { Icon } from '../../../../shared/components/icon/icon';
import { NoticeBanner } from '../../../../shared/components/notice-banner/notice-banner';
import { PageHeader } from '../../../../shared/components/page-header/page-header';
import { Notice } from '../../../../shared/models/notice.model';
import { Alert, AlertRequest } from '../../models/alert.model';
import { AlertService } from '../../services/alert.service';
import { AlertForm } from '../alert-form/alert-form';
import { AlertList } from '../alert-list/alert-list';

@Component({
  selector: 'app-alerts-page',
  imports: [DatePipe, FormsModule, RouterLink, AlertForm, AlertList, PageHeader, NoticeBanner, Icon],
  templateUrl: './alerts-page.html',
  styleUrl: './alerts-page.css',
})
export class AlertsPage implements OnInit {
  private readonly service = inject(AlertService);
  private readonly destroyRef = inject(DestroyRef);

  readonly alerts = signal<Alert[]>([]);
  readonly loading = signal(false);
  readonly showForm = signal(false);
  readonly notice = signal<Notice | null>(null);
  readonly search = signal('');
  readonly updatedAt = signal<string | null>(null);

  readonly filteredAlerts = computed(() => {
    const query = this.search().trim().toLowerCase();
    return this.alerts().filter((alert) =>
      `${alert.service} ${alert.message} ${alert.severity} ${alert.id}`.toLowerCase().includes(query),
    );
  });

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    if (this.loading()) return;
    this.loading.set(true);
    this.notice.set(null);
    this.service
      .getActiveAlerts()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (alerts) => {
          this.alerts.set(alerts);
          this.updatedAt.set(new Date().toISOString());
        },
        error: (error) =>
          this.notice.set({ kind: 'error', message: apiErrorMessage(error, 'Could not load alerts.') }),
      });
  }

  record(request: AlertRequest): void {
    if (this.loading()) return;
    this.loading.set(true);
    this.notice.set(null);
    this.service
      .recordAlert(request)
      .pipe(
        switchMap((result) =>
          this.service.getActiveAlerts().pipe(map((alerts) => ({ result, alerts }))),
        ),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: ({ result, alerts }) => {
          this.alerts.set(alerts);
          this.updatedAt.set(new Date().toISOString());
          this.showForm.set(false);
          this.notice.set({ kind: result.created ? 'success' : 'info', message: result.message });
        },
        error: (error) =>
          this.notice.set({
            kind: 'error',
            message: apiErrorMessage(error, 'The alert request could not finish. Refresh before retrying.'),
          }),
      });
  }
}
