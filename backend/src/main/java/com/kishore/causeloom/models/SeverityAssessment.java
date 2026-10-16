package com.kishore.causeloom.models;

public record SeverityAssessment(String level, int rank, int totalServices,
                                 double score, int affectedCount) {}