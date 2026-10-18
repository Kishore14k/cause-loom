export type ServiceStatus = 'healthy' | 'degraded' | 'failing' | 'unknown';

export interface Microservice {
  name: string;
  tier: number;
  type: string;
  status: ServiceStatus;
}

export interface ImpactNode {
  name: string;
  status: ServiceStatus;
  role: string; // root_cause | failing | hidden_victim
  hops: number;
}

/** source depends on target (the DEPENDS_ON direction). */
export interface ImpactEdge {
  source: string;
  target: string;
  critical: boolean;
}

export interface ImpactGraph {
  origin: string;
  nodes: ImpactNode[];
  edges: ImpactEdge[];
}

export interface TopologyGraph {
  nodes: Microservice[];
  edges: ImpactEdge[];
}

export interface SeverityAssessment {
  level: string;
  rank: number;
  totalServices: number;
  score: number;
  affectedCount: number;
  hardAffectedCount: number;
  tier: number;
}
