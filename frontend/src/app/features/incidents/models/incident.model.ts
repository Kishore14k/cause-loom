export interface Incident {
  id: string;
  title: string;
  severity: string;
  status: string;
  createdAt: string;
  rootCause: string;
}

export interface IncidentResult {
  created: boolean;
  incident: Incident | null;
  message: string;
}

export interface ResolveResult {
  resolved: boolean;
  incidentId: string;
  restoredServices: string[];
  message: string;
}
