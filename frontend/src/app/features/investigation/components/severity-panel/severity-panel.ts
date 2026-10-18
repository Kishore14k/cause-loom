import { DecimalPipe } from '@angular/common';
import { Component, input } from '@angular/core';

import { Icon } from '../../../../shared/components/icon/icon';
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge';
import { SeverityAssessment } from '../../models/service-graph.model';

@Component({
  selector: 'app-severity-panel',
  imports: [DecimalPipe, Icon, StatusBadge],
  templateUrl: './severity-panel.html',
  styleUrl: './severity-panel.css',
})
export class SeverityPanel {
  readonly assessment = input<SeverityAssessment | null>(null);
}
