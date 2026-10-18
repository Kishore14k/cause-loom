import { Incident } from '../../incidents/models/incident.model';
import { OnCallResult } from './on-call.model';
import { ImpactGraph, SeverityAssessment } from './service-graph.model';

export interface Finding {
  rootCause: string;
  severity: SeverityAssessment;
  onCall: OnCallResult;
  incident: Incident | null;
  impact: ImpactGraph;
}

export interface ToolStep {
  step: number;
  tool: string;
  input: string;
  error: boolean;
}

export interface InvestigationResult {
  explanation: string;
  findings: Finding[];
  steps: ToolStep[];
}
