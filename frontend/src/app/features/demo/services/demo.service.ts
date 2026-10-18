import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, timeout } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api.config';
import { AlertResult } from '../../alerts/models/alert.model';

@Injectable({ providedIn: 'root' })
export class DemoService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  simulateOutage(): Observable<AlertResult[]> {
    return this.http
      .post<AlertResult[]>(`${this.apiBaseUrl}/demo/simulate-outage`, {})
      .pipe(timeout(30000));
  }

  reset(): Observable<{ message: string }> {
    return this.http
      .post<{ message: string }>(`${this.apiBaseUrl}/demo/reset`, {})
      .pipe(timeout(30000));
  }
}
