import { DatePipe } from '@angular/common';
import { Component, input, output } from '@angular/core';

import { Icon } from '../../../../shared/components/icon/icon';
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge';
import { Incident } from '../../models/incident.model';

@Component({
  selector: 'app-incident-list',
  imports: [DatePipe, StatusBadge, Icon],
  templateUrl: './incident-list.html',
  styleUrl: './incident-list.css',
})
export class IncidentList {
  readonly incidents = input.required<Incident[]>();
  readonly loading = input(false);
  readonly resolveRequested = output<Incident>();
}
