package com.kishore.causeloom.models;

public record OnCallResult(String service, String team, Person onCall,
                           Person responder, boolean escalated, String message) {}