import { Component, ElementRef, input, output, viewChild } from '@angular/core';

import { Icon } from '../icon/icon';

@Component({
  selector: 'app-confirm-dialog',
  imports: [Icon],
  templateUrl: './confirm-dialog.html',
  styleUrl: './confirm-dialog.css',
})
export class ConfirmDialog {
  private static nextId = 0;

  readonly headingId = `confirmation-${ConfirmDialog.nextId++}`;
  private readonly element = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  readonly title = input.required<string>();
  readonly message = input.required<string>();
  readonly confirmLabel = input('Confirm');
  readonly destructive = input(false);
  readonly confirmed = output<void>();

  open(): void {
    this.element().nativeElement.showModal();
  }

  close(): void {
    this.element().nativeElement.close();
  }

  confirm(): void {
    this.close();
    this.confirmed.emit();
  }
}
