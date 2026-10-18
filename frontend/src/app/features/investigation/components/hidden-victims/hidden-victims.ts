import { Component, computed, input } from '@angular/core';

import { Icon } from '../../../../shared/components/icon/icon';
import { ImpactGraph } from '../../models/service-graph.model';

@Component({
  selector: 'app-hidden-victims',
  imports: [Icon],
  templateUrl: './hidden-victims.html',
  styleUrl: './hidden-victims.css',
})
export class HiddenVictims {
  readonly graph = input.required<ImpactGraph>();
  readonly nodes = computed(() => this.graph().nodes.filter((node) => node.role === 'hidden_victim'));
}
