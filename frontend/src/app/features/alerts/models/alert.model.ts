export type AlertSeverity = 'critical' | 'high' | 'warning';

export interface Alert {
  id: string;
  service: string;
  severity: string;
  message: string;
}

export interface AlertRequest {
  service: string;
  severity: AlertSeverity;
  message: string;
}

export interface AlertResult {
  created: boolean;
  alert: Alert | null;
  message: string;
}
