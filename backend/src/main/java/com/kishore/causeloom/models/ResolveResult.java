package com.kishore.causeloom.models;

import java.util.List;

public record ResolveResult(boolean resolved, String incidentId,
                            List<String> restoredServices, String message) {}