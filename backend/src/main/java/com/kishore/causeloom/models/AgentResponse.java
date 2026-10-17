package com.kishore.causeloom.models;

import java.util.List;

public record AgentResponse(String explanation, List<Finding> findings, List<ToolStep> steps) {}