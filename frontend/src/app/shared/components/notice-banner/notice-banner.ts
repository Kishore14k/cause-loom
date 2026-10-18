import { Component, input, output } from '@angular/core';

import { Notice } from '../../models/notice.model';
import { Icon } from '../icon/icon';

@Component({
  selector: 'app-notice-banner',
  imports: [Icon],
  templateUrl: './notice-banner.html',
  styleUrl: './notice-banner.css',
})
export class NoticeBanner {
  readonly notice = input<Notice | null>(null);
  readonly dismissed = output<void>();
}
