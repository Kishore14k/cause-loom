import { Component, computed, input } from '@angular/core';

const TONES: Record<string, string> = {
  critical: 'red',
  error: 'red',
  high: 'amber',
  open: 'amber',
  failing: 'amber',
  medium: 'blue',
  unknown: 'blue',
  warning: 'violet',
  degraded: 'violet',
  closed: 'green',
  healthy: 'green',
  returned: 'green',
};

@Component({
  selector: 'app-status-badge',
  templateUrl: './status-badge.html',
  styleUrl: './status-badge.css',
})
export class StatusBadge {
  readonly value = input.required<string>();
  readonly tone = computed(() => TONES[this.value().toLowerCase()] ?? 'blue');
}
