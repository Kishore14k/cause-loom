import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Icon } from '../../../../shared/components/icon/icon';
import { Finding } from '../../models/investigation-result.model';

@Component({
  selector: 'app-investigation-metrics',
  imports: [Icon, RouterLink],
  templateUrl: './investigation-metrics.html',
  styleUrl: './investigation-metrics.css',
})
export class InvestigationMetrics {
  readonly alertCount = input<number | null>(null);
  readonly rootCount = input<number | null>(null);
  readonly finding = input<Finding | null>(null);

  readonly hiddenCount = computed(
    () => this.finding()?.impact.nodes.filter((node) => node.role === 'hidden_victim').length ?? null,
  );
}
