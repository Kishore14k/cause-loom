import { Routes } from '@angular/router';

import { Layout } from './shared/components/layout/layout';

export const routes: Routes = [
  {
    path: '',
    component: Layout,
    children: [
      { path: '', redirectTo: 'investigation', pathMatch: 'full' },
      {
        path: 'investigation',
        loadChildren: () =>
          import('./features/investigation/investigation.route').then(
            (m) => m.INVESTIGATION_ROUTES,
          ),
      },
      {
        path: 'alerts',
        loadChildren: () => import('./features/alerts/alerts.route').then((m) => m.ALERTS_ROUTES),
      },
      {
        path: 'incidents',
        loadChildren: () =>
          import('./features/incidents/incidents.route').then((m) => m.INCIDENTS_ROUTES),
      },
      { path: '**', redirectTo: 'investigation' },
    ],
  },
];
