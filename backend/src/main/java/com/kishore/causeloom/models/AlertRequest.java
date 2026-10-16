package com.kishore.causeloom.models;

public record AlertRequest(String service, String severity, String message) {}