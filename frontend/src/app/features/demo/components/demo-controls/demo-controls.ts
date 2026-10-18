import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, input, output, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';

import { apiErrorMessage } from '../../../../core/utils/api-error';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';
import { Icon } from '../../../../shared/components/icon/icon';
import { NoticeBanner } from '../../../../shared/components/notice-banner/notice-banner';
import { Notice } from '../../../../shared/models/notice.model';
import { DemoService } from '../../services/demo.service';

@Component({
  selector: 'app-demo-controls',
  imports: [ConfirmDialog, NoticeBanner, Icon],
  templateUrl: './demo-controls.html',
  styleUrl: './demo-controls.css',
})
export class DemoControls {
  private readonly service = inject(DemoService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly confirmation = viewChild.required(ConfirmDialog);

  readonly disabled = input(false);
  readonly changed = output<'simulate' | 'reset'>();
  readonly busyChange = output<boolean>();

  readonly loading = signal(false);
  readonly notice = signal<Notice | null>(null);

  simulate(): void {
    if (this.disabled() || this.loading()) return;
    this.setLoading(true);
    this.notice.set(null);
    this.service
      .simulateOutage()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.setLoading(false)),
      )
      .subscribe({
        next: (results) => {
          const created = results.filter((result) => result.created).length;
          this.notice.set({
            kind: 'success',
            message: `${created} alerts recorded, ${results.length - created} already existed. Press Investigate to call the agent.`,
          });
          this.changed.emit('simulate');
        },
        error: (error) => this.showError(error),
      });
  }

  requestReset(): void {
    this.confirmation().open();
  }

  reset(): void {
    if (this.disabled() || this.loading()) return;
    this.setLoading(true);
    this.notice.set(null);
    this.service
      .reset()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.setLoading(false)),
      )
      .subscribe({
        next: (result) => {
          this.notice.set({ kind: 'success', message: result.message });
          this.changed.emit('reset');
        },
        error: (error) => this.showError(error),
      });
  }

  private showError(error: unknown): void {
    this.notice.set({
      kind: 'error',
      message:
        error instanceof HttpErrorResponse && error.status === 404
          ? 'Demo endpoints are off. Set causeloom.demo.enabled=true in Spring Boot and restart it.'
          : apiErrorMessage(error, 'The demo action could not finish. Refresh before retrying.'),
    });
  }

  private setLoading(value: boolean): void {
    this.loading.set(value);
    this.busyChange.emit(value);
  }
}
