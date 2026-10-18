import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, timeout } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api.config';
import { Incident, IncidentResult, ResolveResult } from '../models/incident.model';

@Injectable({ providedIn: 'root' })
export class IncidentService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  list(): Observable<Incident[]> {
    return this.http.get<Incident[]>(`${this.apiBaseUrl}/incidents`).pipe(timeout(20000));
  }

  /** Sends only the root name. Duplicate checks and severity belong to the backend. */
  createIncident(rootCause: string): Observable<IncidentResult> {
    return this.http
      .post<IncidentResult>(`${this.apiBaseUrl}/incidents`, { rootCause })
      .pipe(timeout(20000));
  }

  resolveIncident(id: string): Observable<ResolveResult> {
    return this.http
      .post<ResolveResult>(`${this.apiBaseUrl}/incidents/${encodeURIComponent(id)}/resolve`, {})
      .pipe(timeout(20000));
  }
}
