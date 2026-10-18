import type { ImpactEdge, ImpactGraph } from '../models/service-graph.model';

/** Follow only real API edges in the DEPENDS_ON direction. Never infer edges from the narrative. */
export function dependencyPath(graph: ImpactGraph, start: string): string[] {
  const names = new Set(graph.nodes.map((node) => node.name));
  if (!names.has(start) || !names.has(graph.origin)) return [];

  const queue: string[] = [start];
  const parent = new Map<string, string | null>([[start, null]]);
  for (let head = 0; head < queue.length; head++) {
    const current = queue[head];
    if (current === graph.origin) {
      const result: string[] = [];
      let cursor: string | null = current;
      while (cursor !== null) {
        result.push(cursor);
        cursor = parent.get(cursor) ?? null;
      }
      return result.reverse();
    }
    for (const edge of graph.edges) {
      if (edge.source === current && names.has(edge.target) && !parent.has(edge.target)) {
        parent.set(edge.target, current);
        queue.push(edge.target);
      }
    }
  }
  return [];
}

export function edgeOnPath(edge: ImpactEdge, path: readonly string[]): boolean {
  return path.some((node, index) => node === edge.source && path[index + 1] === edge.target);
}
