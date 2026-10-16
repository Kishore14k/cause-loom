package com.kishore.causeloom.models;

public record Incident(String id, String title, String severity, String status,
                       String createdAt, String rootCause) {}