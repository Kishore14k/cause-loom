import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
  computed,
  effect,
  input,
  signal,
  untracked,
  viewChild,
} from '@angular/core';
import cytoscape, { type Core, type ElementDefinition } from 'cytoscape';

import { Icon } from '../../../../shared/components/icon/icon';
import type { ImpactGraph, TopologyGraph } from '../../models/service-graph.model';
import { dependencyPath, edgeOnPath } from '../../utils/dependency-path';

@Component({
  selector: 'app-impact-graph',
  imports: [Icon],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './impact-graph.html',
  styleUrl: './impact-graph.css',
})
export class ImpactGraphPanel implements AfterViewInit, OnDestroy {
  /** Blast radius of the selected root cause, from the graph API. */
  readonly graph = input.required<ImpactGraph>();
  /** The whole system, shown faded behind the impact. Optional. */
  readonly topology = input<TopologyGraph | null>(null);

  readonly showAll = signal(true);
  readonly canShowAll = computed(() => (this.topology()?.nodes.length ?? 0) > 0);
  readonly selectedName = signal('');
  readonly path = computed(() =>
    this.selectedName() ? dependencyPath(this.graph(), this.selectedName()) : [],
  );

  private readonly canvas = viewChild.required<ElementRef<HTMLDivElement>>('canvas');
  private readonly ready = signal(false);
  private cy?: Core;
  private resize?: ResizeObserver;
  private frame = 0;

  constructor() {
    effect(() => {
      const graph = this.graph();
      const topology = this.showAll() && this.canShowAll() ? this.topology() : null;
      if (this.ready()) untracked(() => this.render(graph, topology));
    });
  }

  ngAfterViewInit(): void {
    this.ready.set(true);
    this.resize = new ResizeObserver(() => {
      cancelAnimationFrame(this.frame);
      this.frame = requestAnimationFrame(() => {
        this.cy?.resize();
        this.fit();
      });
    });
    this.resize.observe(this.canvas().nativeElement);
  }

  private render(graph: ImpactGraph, topology: TopologyGraph | null): void {
    this.cy?.destroy();
    this.selectedName.set('');

    const roles = new Map(graph.nodes.map((node) => [node.name, node.role]));
    const impactEdges = new Set(graph.edges.map((edge) => `${edge.source}->${edge.target}`));

    const nodes = topology
      ? topology.nodes.map((node) => ({
          name: node.name,
          role: roles.get(node.name) ?? node.status,
          faded: !roles.has(node.name) && node.status === 'healthy',
        }))
      : graph.nodes.map((node) => ({ name: node.name, role: node.role, faded: false }));
    const edges = topology ? topology.edges : graph.edges;

    const elements: ElementDefinition[] = [
      ...nodes.map((node) => ({
        data: { id: `n:${node.name}`, name: node.name, label: node.name, role: node.role },
        classes: node.faded ? 'faded' : '',
      })),
      ...edges.map((edge, index) => ({
        data: { id: `e:${index}`, source: `n:${edge.source}`, target: `n:${edge.target}` },
        classes: [
          edge.critical ? 'critical' : 'soft',
          topology && !impactEdges.has(`${edge.source}->${edge.target}`) ? 'faded' : '',
        ]
          .join(' ')
          .trim(),
      })),
    ];

    this.cy = cytoscape({
      container: this.canvas().nativeElement,
      elements,
      layout: { name: 'breadthfirst', directed: true, spacingFactor: 1.2, padding: 30, animate: false },
      minZoom: 0.3,
      maxZoom: 2.5,
      wheelSensitivity: 0.2,
      style: [
        {
          selector: 'node',
          style: {
            label: 'data(label)',
            color: '#e8eefb',
            'font-family': 'Instrument Sans, Segoe UI, sans-serif',
            'font-size': 12,
            'text-valign': 'center',
            'text-halign': 'center',
            shape: 'round-rectangle',
            width: 'label',
            height: 32,
            padding: '12px',
            'background-color': '#0f1b36',
            'border-width': 1.5,
            'border-color': '#1f6b46',
            'overlay-opacity': 0,
          },
        },
        { selector: 'node[role = "degraded"]', style: { 'border-color': '#a78bfa', 'background-color': '#1b1740' } },
        { selector: 'node[role = "failing"]', style: { 'border-color': '#f59e0b', 'background-color': '#33200a' } },
        {
          selector: 'node[role = "hidden_victim"]',
          style: { 'border-color': '#eab308', 'border-style': 'dashed', 'background-color': '#2c2909' },
        },
        {
          selector: 'node[role = "root_cause"]',
          style: {
            'border-color': '#ef4444',
            'border-width': 3,
            'background-color': '#3a1216',
            'font-weight': 700,
            height: 38,
          },
        },
        {
          selector: 'edge',
          style: {
            width: 1.6,
            'curve-style': 'bezier',
            'line-color': '#2a3f6d',
            'target-arrow-color': '#2a3f6d',
            'target-arrow-shape': 'triangle',
            'arrow-scale': 0.9,
            'overlay-opacity': 0,
          },
        },
        { selector: 'edge.soft', style: { 'line-style': 'dashed' } },
        { selector: '.faded', style: { opacity: 0.3 } },
        { selector: '.dimmed', style: { opacity: 0.18 } },
        {
          selector: 'edge.highlighted',
          style: { width: 3.2, 'line-color': '#ff6b6b', 'target-arrow-color': '#ff6b6b', 'z-index': 5 },
        },
        { selector: 'node.highlighted', style: { 'border-width': 3.5 } },
      ],
    });

    this.cy.on('tap', 'node', (event) => {
      const name = String(event.target.data('name'));
      this.select(roles.has(name) ? name : '');
    });
    this.cy.on('tap', (event) => {
      if (event.target === this.cy) this.select('');
    });
  }

  select(name: string): void {
    this.selectedName.set(name);
    const path = this.path();
    const cy = this.cy;
    if (!cy) return;

    cy.elements().removeClass('dimmed highlighted');
    if (!name || !path.length) return;

    cy.elements().addClass('dimmed');
    for (const node of path) {
      cy.getElementById(`n:${node}`).removeClass('dimmed').addClass('highlighted');
    }
    const edges = this.showAll() && this.canShowAll() ? this.topology()!.edges : this.graph().edges;
    edges.forEach((edge, index) => {
      if (edgeOnPath(edge, path)) cy.getElementById(`e:${index}`).removeClass('dimmed').addClass('highlighted');
    });
  }

  toggleAll(): void {
    this.showAll.update((value) => !value);
  }

  fit(): void {
    this.cy?.fit(undefined, 30);
  }

  zoom(multiplier: number): void {
    const cy = this.cy;
    if (cy) {
      cy.zoom({ level: cy.zoom() * multiplier, renderedPosition: { x: cy.width() / 2, y: cy.height() / 2 } });
    }
  }

  ngOnDestroy(): void {
    cancelAnimationFrame(this.frame);
    this.resize?.disconnect();
    this.cy?.destroy();
  }
}
