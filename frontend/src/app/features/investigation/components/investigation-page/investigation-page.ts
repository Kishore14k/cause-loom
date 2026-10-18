import { DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, catchError, finalize, forkJoin, map, of, switchMap, tap } from 'rxjs';

import { apiErrorMessage } from '../../../../core/utils/api-error';
import { Icon } from '../../../../shared/components/icon/icon';
import { NoticeBanner } from '../../../../shared/components/notice-banner/notice-banner';
import { PageHeader } from '../../../../shared/components/page-header/page-header';
import { Notice } from '../../../../shared/models/notice.model';
import { Alert } from '../../../alerts/models/alert.model';
import { AlertService } from '../../../alerts/services/alert.service';
import { DemoControls } from '../../../demo/components/demo-controls/demo-controls';
import { IncidentAction } from '../../../incidents/components/incident-action/incident-action';
import { Finding, InvestigationResult } from '../../models/investigation-result.model';
import { Microservice, TopologyGraph } from '../../models/service-graph.model';
import { InvestigationService } from '../../services/investigation.service';
import { OnCallService } from '../../services/on-call.service';
import { ServiceGraphService } from '../../services/service-graph.service';
import { HiddenVictims } from '../hidden-victims/hidden-victims';
import { ImpactGraphPanel } from '../impact-graph/impact-graph';
import { InvestigationForm } from '../investigation-form/investigation-form';
import { InvestigationMetrics } from '../investigation-metrics/investigation-metrics';
import { InvestigationResultPanel } from '../investigation-result/investigation-result';
import { ResponderPanel } from '../responder-panel/responder-panel';
import { RootCauseSelector } from '../root-cause-selector/root-cause-selector';
import { SeverityPanel } from '../severity-panel/severity-panel';

@Component({
  selector: 'app-investigation-page',
  imports: [
    DatePipe,
    PageHeader,
    NoticeBanner,
    Icon,
    InvestigationForm,
    InvestigationMetrics,
    RootCauseSelector,
    ImpactGraphPanel,
    ResponderPanel,
    SeverityPanel,
    HiddenVictims,
    InvestigationResultPanel,
    IncidentAction,
    DemoControls,
  ],
  templateUrl: './investigation-page.html',
  styleUrl: './investigation-page.css',
})
export class InvestigationPage implements OnInit {
  private readonly investigationService = inject(InvestigationService);
  private readonly graphService = inject(ServiceGraphService);
  private readonly onCallService = inject(OnCallService);
  private readonly alertService = inject(AlertService);
  private readonly destroyRef = inject(DestroyRef);

  readonly operation = signal<'refresh' | 'inspect' | 'investigate' | null>(null);
  readonly childBusy = signal(false);
  readonly busy = computed(() => this.operation() !== null || this.childBusy());

  readonly alerts = signal<Alert[]>([]);
  readonly roots = signal<Microservice[]>([]);
  readonly topology = signal<TopologyGraph | null>(null);
  readonly finding = signal<Finding | null>(null);
  readonly selectedRoot = signal('');

  readonly result = signal<InvestigationResult | null>(null);
  readonly receivedAt = signal<string | null>(null);
  readonly updatedAt = signal<string | null>(null);
  readonly stale = signal(false);
  readonly notice = signal<Notice | null>(null);

  ngOnInit(): void {
    this.refresh();
  }

  /** Reads graph evidence only. No LLM call is made. */
  refresh(): void {
    if (this.operation()) return;
    this.operation.set('refresh');
    this.notice.set(null);
    this.stale.set(!!this.result());
    this.readGraph()
      .pipe(
        switchMap((data) => {
          const root = data.roots.some((item) => item.name === this.selectedRoot())
            ? this.selectedRoot()
            : (data.roots[0]?.name ?? '');
          return root
            ? this.readFinding(root).pipe(map((finding) => ({ ...data, finding })))
            : of({ ...data, finding: null });
        }),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.operation.set(null)),
      )
      .subscribe({
        next: (data) => {
          this.alerts.set(data.alerts);
          this.roots.set(data.roots);
          this.topology.set(data.topology);
          this.finding.set(data.finding);
          this.selectedRoot.set(data.finding?.rootCause ?? '');
          this.updatedAt.set(new Date().toISOString());
        },
        error: (error) => this.showError(error, 'Could not refresh the graph.'),
      });
  }

  selectRoot(name: string): void {
    if (this.busy()) return;
    this.operation.set('inspect');
    this.notice.set(null);
    this.readFinding(name)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.operation.set(null)),
      )
      .subscribe({
        next: (finding) => {
          this.finding.set(finding);
          this.selectedRoot.set(name);
        },
        error: (error) => this.showError(error, 'Could not load this root cause.'),
      });
  }

  investigate(message: string): void {
    const request = message.trim();
    if (this.busy() || !request) return;
    this.operation.set('investigate');
    this.notice.set(null);
    this.stale.set(!!this.result());
    this.investigationService
      .investigate(request)
      .pipe(
        tap((result) => {
          this.result.set(result);
          this.receivedAt.set(new Date().toISOString());
          this.stale.set(false);
          this.finding.set(result.findings[0] ?? null);
          this.selectedRoot.set(result.findings[0]?.rootCause ?? '');
        }),
        switchMap((result) => this.readGraph().pipe(map((data) => ({ ...data, result })))),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.operation.set(null)),
      )
      .subscribe({
        next: (data) => {
          this.alerts.set(data.alerts);
          this.roots.set(data.roots);
          this.topology.set(data.topology);
          this.updatedAt.set(new Date().toISOString());
          const hasToolError = data.result.steps.some((step) => step.error);
          this.notice.set({
            kind: hasToolError ? 'info' : 'success',
            message: hasToolError
              ? 'The investigation finished, but a tool returned an error. Review the tool list.'
              : 'Investigation complete. Review the graph, the explanation and the incident.',
          });
        },
        error: (error) =>
          this.showError(
            error,
            'The investigation could not finish. It may have partly completed, so refresh before retrying.',
          ),
      });
  }

  onDemoChanged(action: 'simulate' | 'reset'): void {
    if (action === 'reset') {
      this.result.set(null);
      this.receivedAt.set(null);
    }
    this.refresh();
  }

  private readGraph() {
    return forkJoin({
      alerts: this.alertService.getActiveAlerts(),
      roots: this.graphService.getRootCauses(),
      // The whole-system view is optional, so a missing endpoint must not break the page.
      topology: this.graphService.getTopology().pipe(catchError(() => of(null))),
    });
  }

  private readFinding(name: string): Observable<Finding> {
    return forkJoin({
      impact: this.graphService.getImpact(name),
      severity: this.graphService.getSeverity(name),
      onCall: this.onCallService.getResponder(name),
    }).pipe(
      map((data) => ({
        ...data,
        rootCause: name,
        incident: this.result()?.findings.find((item) => item.rootCause === name)?.incident ?? null,
      })),
    );
  }

  private showError(error: unknown, fallback: string): void {
    this.notice.set({ kind: 'error', message: apiErrorMessage(error, fallback) });
  }
}
