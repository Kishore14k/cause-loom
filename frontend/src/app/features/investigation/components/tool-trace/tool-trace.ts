import { Component, input } from '@angular/core';

import { StatusBadge } from '../../../../shared/components/status-badge/status-badge';
import { ToolStep } from '../../models/investigation-result.model';

@Component({
  selector: 'app-tool-trace',
  imports: [StatusBadge],
  templateUrl: './tool-trace.html',
  styleUrl: './tool-trace.css',
})
export class ToolTrace {
  readonly steps = input.required<ToolStep[]>();
}
