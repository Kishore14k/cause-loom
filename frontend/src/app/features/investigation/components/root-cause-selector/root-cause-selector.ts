import { Component, input, output } from '@angular/core';

import { StatusBadge } from '../../../../shared/components/status-badge/status-badge';
import { Microservice } from '../../models/service-graph.model';

@Component({
  selector: 'app-root-cause-selector',
  imports: [StatusBadge],
  templateUrl: './root-cause-selector.html',
  styleUrl: './root-cause-selector.css',
})
export class RootCauseSelector {
  readonly roots = input.required<Microservice[]>();
  readonly selected = input('');
  readonly severity = input<string | null>(null);
  readonly disabled = input(false);
  readonly selectedChange = output<string>();

  select(event: Event): void {
    const element = event.target as HTMLSelectElement;
    const name = element.value;
    element.value = this.selected(); // the page confirms the change after loading
    this.selectedChange.emit(name);
  }
}
