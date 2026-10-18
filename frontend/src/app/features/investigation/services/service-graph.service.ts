import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, timeout } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api.config';
import {
  ImpactGraph,
  Microservice,
  SeverityAssessment,
  TopologyGraph,
} from '../models/service-graph.model';

@Injectable({ providedIn: 'root' })
export class ServiceGraphService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getRootCauses(): Observable<Microservice[]> {
    return this.http.get<Microservice[]>(`${this.apiBaseUrl}/services/root-cause`).pipe(timeout(20000));
  }

  getImpact(name: string): Observable<ImpactGraph> {
    return this.http
      .get<ImpactGraph>(`${this.apiBaseUrl}/services/${encodeURIComponent(name)}/impact`)
      .pipe(timeout(20000));
  }

  getSeverity(name: string): Observable<SeverityAssessment> {
    return this.http
      .get<SeverityAssessment>(`${this.apiBaseUrl}/services/${encodeURIComponent(name)}/severity`)
      .pipe(timeout(20000));
  }

  getTopology(): Observable<TopologyGraph> {
    return this.http.get<TopologyGraph>(`${this.apiBaseUrl}/services/topology`).pipe(timeout(20000));
  }
}
