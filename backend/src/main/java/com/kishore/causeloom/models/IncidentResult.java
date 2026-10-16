package com.kishore.causeloom.models;

public record IncidentResult(boolean created, Incident incident, String message) {}