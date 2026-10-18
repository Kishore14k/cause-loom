import { Component, input } from '@angular/core';

import { Icon } from '../../../../shared/components/icon/icon';
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge';
import { OnCallResult } from '../../models/on-call.model';

@Component({
  selector: 'app-responder-panel',
  imports: [Icon, StatusBadge],
  templateUrl: './responder-panel.html',
  styleUrl: './responder-panel.css',
})
export class ResponderPanel {
  readonly result = input<OnCallResult | null>(null);

  initials(name: string): string {
    return name
      .split(/\s+/)
      .map((part) => part[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  }
}
