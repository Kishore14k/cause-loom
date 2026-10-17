package com.kishore.causeloom.models;

public record Finding(String rootCause,
                      SeverityAssessment severity,
                      OnCallResult onCall,
                      Incident incident,
                      ImpactGraph impact) {}