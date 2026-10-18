package com.kishore.causeloom.models;

import java.util.List;

public record TopologyGraph(List<Microservice> nodes, List<ImpactEdge> edges) {}