import { DatePipe } from '@angular/common';
import { Component, input } from '@angular/core';

import { Icon } from '../../../../shared/components/icon/icon';
import { InvestigationResult } from '../../models/investigation-result.model';
import { ToolTrace } from '../tool-trace/tool-trace';

@Component({
  selector: 'app-investigation-result',
  imports: [DatePipe, ToolTrace, Icon],
  templateUrl: './investigation-result.html',
  styleUrl: './investigation-result.css',
})
export class InvestigationResultPanel {
  readonly result = input.required<InvestigationResult>();
  readonly receivedAt = input<string | null>(null);
  readonly stale = input(false);

  exportResult(): void {
    const blob = new Blob(
      [JSON.stringify({ receivedAt: this.receivedAt(), mayBeOutdated: this.stale(), ...this.result() }, null, 2)],
      { type: 'application/json' },
    );
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'causeloom-investigation.json';
    link.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }
}
