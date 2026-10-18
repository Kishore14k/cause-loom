import { Component, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Icon } from '../../../../shared/components/icon/icon';

@Component({
  selector: 'app-investigation-form',
  imports: [FormsModule, Icon],
  templateUrl: './investigation-form.html',
  styleUrl: './investigation-form.css',
})
export class InvestigationForm {
  readonly loading = input(false);
  readonly submitted = output<string>();

  message =
    'Investigate the current alerts. Use the graph tools to find each root cause, affected services, severity and available responder. Create or reuse an incident for each confirmed root cause.';

  submit(): void {
    const message = this.message.trim();
    if (!this.loading() && message) this.submitted.emit(message);
  }
}
