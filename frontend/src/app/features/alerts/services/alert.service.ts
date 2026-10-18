import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, timeout } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api.config';
import { Alert, AlertRequest, AlertResult } from '../models/alert.model';

@Injectable({ providedIn: 'root' })
export class AlertService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getActiveAlerts(): Observable<Alert[]> {
    return this.http.get<Alert[]>(`${this.apiBaseUrl}/alerts/active`).pipe(timeout(20000));
  }

  /** The same entry point a monitoring tool's webhook would call. */
  recordAlert(request: AlertRequest): Observable<AlertResult> {
    return this.http.post<AlertResult>(`${this.apiBaseUrl}/alerts`, request).pipe(timeout(20000));
  }
}
