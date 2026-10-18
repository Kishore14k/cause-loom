import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, timeout } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api.config';
import { OnCallResult } from '../models/on-call.model';

@Injectable({ providedIn: 'root' })
export class OnCallService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getResponder(name: string): Observable<OnCallResult> {
    return this.http
      .get<OnCallResult>(`${this.apiBaseUrl}/people/responder/${encodeURIComponent(name)}`)
      .pipe(timeout(20000));
  }
}
