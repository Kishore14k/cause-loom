import { Component, input } from '@angular/core';

import { Icon } from '../../../../shared/components/icon/icon';
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge';
import { Alert } from '../../models/alert.model';

@Component({
  selector: 'app-alert-list',
  imports: [StatusBadge, Icon],
  templateUrl: './alert-list.html',
  styleUrl: './alert-list.css',
})
export class AlertList {
  readonly alerts = input.required<Alert[]>();
  readonly filtered = input(false);
}
