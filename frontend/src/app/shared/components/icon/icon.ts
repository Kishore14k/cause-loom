import { Component, input } from '@angular/core';

const PATHS: Record<string, string> = {
  graph:
    'M6 5 18 12 6 19M6 5v14M3 5a3 3 0 1 0 6 0a3 3 0 1 0-6 0M15 12a3 3 0 1 0 6 0a3 3 0 1 0-6 0M3 19a3 3 0 1 0 6 0a3 3 0 1 0-6 0',
  grid: 'M3 3h7v7H3zM14 3h7v7h-7zM3 14h7v7H3zM14 14h7v7h-7z',
  menu: 'M4 6h16M4 12h16M4 18h16',
  alert: 'M12 3 2 21h20L12 3M12 9v5M12 17v1',
  incident: 'M7 5H4v16h16V5h-3M8 3h8v5H8zM8 12h8M8 16h5',
  arrow: 'M5 12h14M13 6l6 6-6 6',
  refresh: 'M20 8a8 8 0 0 0-14-3L3 8M3 3v5h5M4 16a8 8 0 0 0 14 3l3-3M21 21v-5h-5',
  play: 'm8 4 12 8-12 8V4',
  spark: 'm12 3 2.5 6.5L21 12l-6.5 2.5L12 21l-2.5-6.5L3 12l6.5-2.5L12 3',
  search: 'M10 3a7 7 0 1 0 0 14a7 7 0 1 0 0-14M15 15l6 6',
  plus: 'M12 5v14M5 12h14',
  close: 'm6 6 12 12M18 6 6 18',
  check: 'm5 12 4 4L19 6',
  person: 'M12 3a4 4 0 1 0 0 8a4 4 0 1 0 0-8M4 21v-3a8 5 0 0 1 16 0v3',
  layers: 'm12 3 10 5-10 5L2 8l10-5M2 12l10 5 10-5M2 16l10 5 10-5',
  eye: 'M2 12s3-7 10-7 10 7 10 7-3 7-10 7S2 12 2 12M9 12a3 3 0 1 0 6 0a3 3 0 1 0-6 0',
  fit: 'M9 3H3v6M15 3h6v6M3 15v6h6M21 15v6h-6',
  minus: 'M5 12h14',
  info: 'M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18M12 11v6M12 7v1',
  download: 'M12 3v12M7 10l5 5 5-5M4 17v4h16v-4',
  bolt: 'm13 2-9 12h7l-1 8 10-12h-7l0-8',
  chevron: 'm9 5 7 7-7 7',
};

@Component({
  selector: 'app-icon',
  templateUrl: './icon.html',
  styleUrl: './icon.css',
})
export class Icon {
  readonly name = input('graph');

  path(): string {
    return PATHS[this.name()] ?? PATHS['graph'];
  }
}
