import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, timeout } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api.config';
import { InvestigationResult } from '../models/investigation-result.model';

@Injectable({ providedIn: 'root' })
export class InvestigationService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  /** The agent chooses and calls the graph tools, so this can take a while. */
  investigate(message: string): Observable<InvestigationResult> {
    return this.http
      .post<InvestigationResult>(`${this.apiBaseUrl}/agent/investigate`, { message })
      .pipe(timeout(300000));
  }
}
