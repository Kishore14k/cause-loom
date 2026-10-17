package com.kishore.causeloom.models;

import java.util.List;

public record ImpactGraph(String origin, List<ImpactNode> nodes, List<ImpactEdge> edges) {}